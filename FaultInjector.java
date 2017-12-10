package random;

import java.util.ArrayList;
import org.uncommons.maths.random.MersenneTwisterRNG;

import test.SerialRXTXFAST;


public class FaultInjector {
	private final int RESET_COUNT = 100000; // schedule a reset after RESET_COUNT injections
	private final int COUNT = 20; //  Reset after COUNT continuous faults have been detected
	private long numToInject;
	private int numInjected;
	private int totalTries;
	private int dutTotalErrors;
	private int unrecoverableErrors;
	private  int noResponse;
	private int expected;
	private int timeout;
	private int counter;
	private ArrayList<Integer> frames;
	protected SerialRXTXFAST injectionSerial;
	private MersenneTwisterRNG PRG;
	protected Logger logger;
	private char[] noerror_reply = {'X', 'Y', 'Z'};
	private char[] error_reply = {'U', 'V', 'W'};
	private char[] harderror_reply = {'R', 'S', 'T'};
	private int reply_cycle;
	public CountTimerGUI gui;
	long tStart = System.currentTimeMillis();
	boolean guiInterface;
	
	public FaultInjector(
						int percent, 
						boolean percentBoolean,
						int numberInject,
						ArrayList<Integer> frames,
						SerialRXTXFAST injectionSerial,
						Logger logger,
						int expected,
						int fmax,
						int latency,
						boolean guiInterface
			) {
		this.expected = expected;
		this.numToInject = 0;
		this.numInjected = 0;
		this.totalTries = 0;
		this.dutTotalErrors = 0;
		this.unrecoverableErrors = 0;
		this.noResponse = 0;
		this.frames = frames;
		this.injectionSerial = injectionSerial;
		this.PRG = new MersenneTwisterRNG();
		this.PRG.setSeed(System.currentTimeMillis());
		this.logger = logger;
		this.numToInject = numberInject; 
		this.timeout = (int) ( ((1.0 / (double) fmax ) * latency ) + 5);
		logger.filePrint("\nInjecting in " + numberInject + " DUT sensitive bits = " + numToInject);
		logger.filePrint("\nMicroblaze timeout = " + timeout + " us");
		System.out.print("\nInjecting in " + numberInject + " DUT sensitive bits = " + numToInject);
		System.out.print("\nMicroblaze timeout = " + timeout + " us");
		this.guiInterface = guiInterface;
		if (guiInterface) {
			gui = new CountTimerGUI();
			gui.setBenchmarkLabel(FaultInjectionPlatform.getBenchmarkName());
			gui.setFault(0);
			gui.setInjected(0);
		}
		this.counter = 0;
	}
	
	
	public boolean randomInjectionCycle() {
		String progress = "";
		this.reply_cycle = 0;
		
		injectionSerial.writeString("I");
		injectionSerial.writeDataInteger(expected);
		injectionSerial.writeDataInteger(timeout);
		
		if (parseSerialForFeedback("INIT") == false) {
			// If hard error or no response, immediately return false
			return false;
		}
		// Ready to inject
		injectionSerial.writeString("I");
		
		int reset_count = 0;
		
		
		while (numInjected < numToInject) {
			
			int frameAddress = frames.get(PRG.nextInt(frames.size()-1));
			int bitPosition = PRG.nextInt(Artix200t.bitsInFrame);
			long num = (((long) frameAddress) << 12) | bitPosition;
			
			String address = "";
	    	if( Artix200t.getColumnType(frameAddress) == Artix200t.ColumnType.CLB ) {
	    		address = String.format("M" + "%010X", num); // Sent to microblaze the frame and bitposition to inject
	    		//System.out.println(address);
	    	} else {
	    		address = String.format("N" + "%010X", num); // Sent to microblaze the frame and bitposition to inject
	    		//System.out.println(address);
	    	}
	        		
			inject(address);
	    	numInjected++;
	    	
	    	if ((numInjected % 5) == 1) {
				
			}
	    	if ((numInjected % 100) == 1) {
	    		
			}
			if ((numInjected % 500) == 1) {
				if (this.guiInterface) {
					gui.setInjected(numInjected);
				}
				
				long tEnd = System.currentTimeMillis();
				
				this.PRG.setSeed(tEnd);
				
				long tDelta = tEnd - tStart;
				double elapsedSeconds = tDelta / 1000.0;
				double estimatedTime =  ( (numToInject - numInjected) * elapsedSeconds) / numInjected;
				int hours = (int) (estimatedTime / 3600);
				int minutes = (int) ((estimatedTime-hours*3600)/60);
				progress = String.format('\r' + "Remaining: " + "%.2f%% | %02d hrs : %02d min", 100-percentage(numInjected, numToInject), hours, minutes);
				if (this.guiInterface) {
					gui.setProgressLabel(progress);	
				}
					
				System.out.print(progress);			
				hours = (int) (elapsedSeconds / 3600);
				minutes = (int) ((elapsedSeconds - hours*3600)/60);
				progress = String.format("\rElapsed time: " + "%02d hrs : %02d min", hours, minutes);
				if (this.guiInterface) {
					gui.setElapsedTime(progress);
				}
			}
			
			if( (numInjected % 20000) == 1){
				this.PRG.setSeed(System.currentTimeMillis());
			}
			
			if (parseSerialForFeedback(address) == false) {
				// If hard error or no response, immediately return false
				//System.out.println(((frameAddress >> 23) & 0x7));
				return false;
			}
			
			reset_count++;
			if (reset_count == RESET_COUNT) {
				System.out.println("Scheduled reset");
				return false;
			}
			
			System.out.print("");
		}
		
		// Stop Microblaze
		injectionSerial.writeString("S");
		
		// REPORT
		logger.filePrintln("Row data");
		logger.filePrintln(progress);
		logger.filePrintln("Injected  = " + numInjected);
		logger.filePrintln("dutTotalErrors = " + dutTotalErrors);
		logger.filePrintln("noResponse = " + noResponse);
		logger.filePrintln("unrecoverableErrors = " + unrecoverableErrors);
		
		System.out.println("Row data");
		System.out.println("Injected = " + numInjected);
		System.out.println("dutTotalErrors = " + dutTotalErrors);
		System.out.println("noResponse = " + noResponse);
		System.out.println("unrecoverableErrors = " + unrecoverableErrors);	
		
		int totalErrors = dutTotalErrors + noResponse;
		logger.filePrintln("Analyzed data");
		logger.filePrintln("DUT Errors = dutTotalErrors - unrecoverableErrors = " 
				+ dutTotalErrors + " - " + unrecoverableErrors +" = " + (dutTotalErrors - unrecoverableErrors));
		logger.filePrintln("Total Errors = dutTotalErrors + noResponse = " 
				+ dutTotalErrors + " + " + noResponse +" = " + totalErrors);	
		logger.filePrintln("SES = Total Errors / Injected = " +  totalErrors + " / " + numInjected +" = " +				
				(( (double) (totalErrors) / (double) numInjected)) );
		System.out.println("Analyzed data");
		System.out.println("DUT Errors = dutTotalErrors - unrecoverableErrors = " 
				+ dutTotalErrors + " - " + unrecoverableErrors +" = " + (dutTotalErrors - unrecoverableErrors));
		System.out.println("Total Errors = dutTotalErrors + noResponse = " 
				+ dutTotalErrors + " + " + noResponse +" = " + totalErrors);	
		System.out.println("SES = Total Errors / Injected = " +  totalErrors + " / " + numInjected +" = " +				
				(( (double) (totalErrors) / (double) numInjected)) );	
		System.out.println(progress);
		

		if (this.guiInterface) {
			gui.close();
		}
		
		return true;
	}
	
	public void inject(String address) {
		injectionSerial.writeString(address);
	}
	
	// Returns true if one of the command words are found
	private boolean parseSerialForFeedback(String address) {
		boolean xFound = false;
		while (true) {
			test.BufferTuple bufferTuple = injectionSerial.readBuffer();
			
			if (bufferTuple.timeout == true) {
				System.out.println("");
				System.out.println("No Response");
				logger.filePrintln("No Response");
				noResponse++;
				return false;
			}
			String buffer = bufferTuple.string;
			for (char c : buffer.toCharArray()) {
				if (c == noerror_reply[reply_cycle]) {
					xFound = true;
					counter = 0;
					this.reply_cycle = (this.reply_cycle+1)%3;
				} else if (c == error_reply[reply_cycle]) {
					System.out.println("");
					System.out.println("--ERROR DETECTED--");
					System.out.println(address);
					logger.filePrintln("Error: " + address + "        On Try: " + totalTries);
					dutTotalErrors++;
					int totalErrors = dutTotalErrors + noResponse + unrecoverableErrors;
					if (this.guiInterface) {
						gui.setPercentageLabel( ((double) totalErrors/ numInjected));
						gui.setFault(totalErrors);
					}
					counter++;
					if (counter > COUNT) {
						counter = 0;
						return false;
					}
				} else if (c == harderror_reply[reply_cycle]) {
					System.out.println("");
					System.out.println("--ERROR = UNRECOVERABLE ERROR--");
					System.out.println(address);
					logger.filePrintln("Unrecoverable Error: " + address + "        On Try: " + totalTries);
					unrecoverableErrors++;
					return false;
				} else {
					System.out.print(c);
				}
			}
			if (xFound == true) {
				return true;
			}
		}
	}
	
	private float percentage(long value, long total) {
		return ( ((float) value * 100.0f) / (float) total);  
	}	

}
