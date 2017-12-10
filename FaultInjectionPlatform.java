package random;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import edu.byu.ece.rapidSmith.bitstreamTools.examples.support.BitstreamOptionParser;
import joptsimple.OptionSet;
import test.SerialRXTXFAST;

//VERSION 01

public class FaultInjectionPlatform {
	
		private static String benchmarkName;
		
		private final static String FULL_BITSTREAM = "b";
		private final static String FULL_BITSTREAM_HELP =
				"Path to the full bitstream.";
		private final static String TOP_BITSTREAM = "t";
		private final static String TOP_BITSTREAM_HELP =
				"Path to the full bitstream.";
		private final static String MASK_BITSTREAM = "m";
		private final static String MASK_BITSTREAM_HELP =
				"Path to the mask bitstream.";
		private final static String BOARD_ID = "bi";
		private final static String BOARD_ID_HELP =
				"Board ID.";
		private final static String RANDOM_INJECTION_OPTION = "r";
		private final static String RANDOM_INJECTION_OPTION_HELP = 
			    "# of random faults";
		private final static String SERIAL_PORT_OPTION_A = "comA";
		private final static String SERIAL_PORT_OPTION_HELP_A = 
				"The serial port that will be used for fault injection.";
		private final static String EXPECTED_OPTION = "e";
		private final static String EXPECTED_OPTION_HELP = 
			    "The expected result of the circuit";
		private final static String FMAX_OPTION = "f";
		private final static String FMAX_OPTION_HELP = 
			    "The max frequency of the circuit given in MHz";
		private final static String LATENCY_OPTION = "l";
		private final static String LATENCY_OPTION_HELP = 
			    "The latency of the circuit given in # of cycles";
		private final static String DATE_OPTION = "d";
		private final static String DATE_OPTION_HELP = 
			    "The date that the benchmark was created";
		private final static String BENCHMARKNAME_OPTION = "n";
		private final static String BENCHMARKNAME_OPTION_HELP = 
			    "The date that the benchmark was created";
		private final static String VIVADOPATH_OPTION = "v";
		private final static String VIVADOPATH_OPTION_HELP = 
			    "Viavado path";
		private final static String JTAG_OPTION = "j";
		private final static String JTAG_OPTION_HELP = 
				"configure with jtag the bitstream";
		private final static String GUI_OPTION = "gui";
		private final static String GUI_OPTION_HELP = 
				"Enable GUI";
		private final static String PERCENT_OPTION = "p";
		private final static String PERCENT_OPTION_HELP = 
				"percent of essential bits to inject";
		private static Calendar cal = Calendar.getInstance();
		
		public static void main(String[] args) {
		
		// Setup Options Parser
		BitstreamOptionParser cmdLineParser = new BitstreamOptionParser();
		cmdLineParser.addInputBitstreamOption();
		cmdLineParser.addPartNameOption();
		cmdLineParser.addHelpOption();
		cmdLineParser.accepts(FULL_BITSTREAM, FULL_BITSTREAM_HELP).withRequiredArg().ofType(String.class);
		cmdLineParser.accepts(TOP_BITSTREAM, TOP_BITSTREAM_HELP).withRequiredArg().ofType(String.class);
		cmdLineParser.accepts(MASK_BITSTREAM, MASK_BITSTREAM_HELP).withRequiredArg().ofType(String.class);
		cmdLineParser.accepts(BOARD_ID, BOARD_ID_HELP).withOptionalArg().ofType(String.class);
		cmdLineParser.accepts(PERCENT_OPTION, PERCENT_OPTION_HELP).withOptionalArg().ofType(String.class);
		cmdLineParser.accepts(RANDOM_INJECTION_OPTION, RANDOM_INJECTION_OPTION_HELP).withOptionalArg().ofType(String.class);
		cmdLineParser.accepts(SERIAL_PORT_OPTION_A, SERIAL_PORT_OPTION_HELP_A).withRequiredArg().ofType(String.class);
		cmdLineParser.accepts(EXPECTED_OPTION, EXPECTED_OPTION_HELP).withRequiredArg().ofType(String.class);
		cmdLineParser.accepts(FMAX_OPTION, FMAX_OPTION_HELP).withRequiredArg().ofType(String.class);
		cmdLineParser.accepts(LATENCY_OPTION, LATENCY_OPTION_HELP).withRequiredArg().ofType(String.class);
		cmdLineParser.accepts(DATE_OPTION, DATE_OPTION_HELP).withRequiredArg().ofType(String.class);
		cmdLineParser.accepts(BENCHMARKNAME_OPTION, BENCHMARKNAME_OPTION_HELP).withRequiredArg().ofType(String.class);
		cmdLineParser.accepts(VIVADOPATH_OPTION, VIVADOPATH_OPTION_HELP).withRequiredArg().ofType(String.class);
		cmdLineParser.accepts(JTAG_OPTION, JTAG_OPTION_HELP).withOptionalArg().ofType(String.class);
		cmdLineParser.accepts(GUI_OPTION, GUI_OPTION_HELP).withOptionalArg().ofType(String.class);
		OptionSet options = cmdLineParser.parseArgumentsExitOnError(args);
		cmdLineParser.checkHelpOptionExitOnHelpMessage(options);
		
		boolean random = false;
		int numberInject = 0;
		boolean percentBoolean = false;
		if (options.has(RANDOM_INJECTION_OPTION) == true) {
			numberInject = cmdLineParser.getIntegerStringExitOnError(options, RANDOM_INJECTION_OPTION, 10, 10);
			random = true;
		} else if (options.has(PERCENT_OPTION) == true) {
			percentBoolean = true;
			random = true;
		} else {
			numberInject = 100;
			random = false;
		}
			
		
		int comA = cmdLineParser.getIntegerStringExitOnError(options, SERIAL_PORT_OPTION_A, 10, 1);
		int expected = cmdLineParser.getIntegerStringExitOnError(options, EXPECTED_OPTION, 10, 10);
		int fmax = cmdLineParser.getIntegerStringExitOnError(options, FMAX_OPTION, 10, 10);
		int latency = cmdLineParser.getIntegerStringExitOnError(options, LATENCY_OPTION, 10, 10);
		int percent = cmdLineParser.getIntegerStringExitOnError(options, PERCENT_OPTION, 10, 10);
		
		boolean jtag = false;
		if (options.has(JTAG_OPTION) == true) {
			jtag = true;
		}
		
		boolean guiEnable = false;
		if (options.has(GUI_OPTION) == true) {
			guiEnable = true;
		} 
		
		String fullBitstream = cmdLineParser.getStringExitOnError(options, FULL_BITSTREAM, "");
		String topBitstream = cmdLineParser.getStringExitOnError(options, TOP_BITSTREAM, "");
		String boardID = cmdLineParser.getStringExitOnError(options, BOARD_ID, "");
		String date = cmdLineParser.getStringExitOnError(options, DATE_OPTION, "");
		String vivadoPath = cmdLineParser.getStringExitOnError(options, VIVADOPATH_OPTION, "");
		setBenchmarkName(cmdLineParser.getStringExitOnError(options, BENCHMARKNAME_OPTION, ""));
		
		String maskBitstream = cmdLineParser.getStringExitOnError(options, MASK_BITSTREAM, "");
		//String configID = cmdLineParser.getStringExitOnError(options, CONFIG_ID_OPTION, "");
		
		FrameExtractor extractor = new FrameExtractor();
		ArrayList<Integer> frameTop = extractor.getFrames(topBitstream);
		System.out.println(frameTop.size());
		ArrayList<Integer> framesMask = extractor.getFrames(maskBitstream);
		frameTop.removeAll(framesMask);
		ArrayList<Integer> framesInjection = extractor.getInjectionFrames(frameTop);		
		
		// Setup serial
		SerialRXTXFAST serialA = setupSerial(comA,920600);
		// Inject
		createDownloadTCL(fullBitstream, boardID);
		Logger logger = new Logger(fullBitstream.replace(".bit", "_"+ getBenchmarkName()));
		logger.filePrint("\ninjectionDate = " + cal.getTime()); 
		logger.filePrint("\nbenchmarkName = " + getBenchmarkName());
		logger.filePrint("\nbenchmarkDate = " + date);
		logger.filePrint("\nbenchmarkExpectedResult = " + expected);
		logger.filePrint("\nbenchmarkFmax = " + fmax + " MHz");
		logger.filePrint("\nbenchmarkLatency = " + latency + " clock cycles\n");
		
		System.out.print("\nInjectionDate = " + cal.getTime()); 
		System.out.print("\nBENCHMARK = " + getBenchmarkName());
		System.out.print("\nDATE = " + date);
		System.out.print("\nbenchmarkExpectedResult = " + expected);
		System.out.print("\nbenchmarkFmax = " + fmax + " MHz");
		System.out.print("\nbenchmarkLatency = " + latency + " clock cycles\n");
		
		RobustFaultInjector injector = new RobustFaultInjector(percent, percentBoolean, numberInject, framesInjection,  serialA,  logger,  expected, fmax, latency, jtag, guiEnable, vivadoPath);
		
		if (random == true) {	
			long startTime = System.currentTimeMillis();
			injector.randomInjectionCycle();
			long endTime = System.currentTimeMillis();
			System.out.println("\nTotal Time Interval " + (endTime - startTime)/60000.0 + " Minutes");
		} else {
			System.err.println("ERROR -- Only random injection is supported.");
		}			
	}
	
	public static SerialRXTXFAST setupSerial(int com, int baudrate) {
		SerialRXTXFAST serial = new SerialRXTXFAST();
		serial.connect("COM" + com, baudrate);
		return serial;
	}
		
	private static void createDownloadTCL(String bitstreamPath, String boardID){
		PrintWriter pw = null;
		try {
			FileWriter fw = new FileWriter("Download.tcl", false);
			pw = new PrintWriter(fw);
			pw.println("open_hw");
			pw.println("connect_hw_server");
			pw.println("open_hw_target");
			pw.println("set_property PROGRAM.FILE {" + bitstreamPath + "} [get_hw_devices xc7a200t_0]");
			pw.println("current_hw_device [get_hw_devices xc7a200t_0]");
			pw.println("refresh_hw_device -update_hw_probes false [lindex [get_hw_devices xc7a200t_0] 0]");
			pw.println("program_hw_devices [get_hw_devices xc7a200t_0]");
			pw.println("exit");
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}
	
	
	@SuppressWarnings("unused")
	private static void createDownloadBatch(String bitstreamPath, String boardID){
		PrintWriter pw = null;
		try {
			FileWriter fw = new FileWriter("Download.cmd", false);
			pw = new PrintWriter(fw);
			pw.println("setMode -bs");
			pw.println("setCable -port auto");
			//pw.println("setCable -target \"digilent_plugin DEVICE=SN:" + boardID + " FREQUENCY=-1\"");
			pw.println("Identify -inferir");
			pw.println("identifyMPM");
			//String newFile = bitstreamPath.replaceAll("fie", "download");
			pw.println("assignFile -p 1 -file " + "\"" + bitstreamPath + "\"");
			pw.println("setCable -port usb21 -baud 66000000");
			pw.println("Program -p 1");
			pw.println("exit");
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}
	
	public static class readStream extends Thread{
		private BufferedReader processOutput;
		private Process pr;
		
		public readStream(Process pr) {
		       this.pr = pr;
		}
		
		public void run(){
			processOutput = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            while(true)
            {
                String line = null;
				try {
					line = processOutput.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                if(line == null) break;
                System.out.println(line);
            }
            try {
				processOutput.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            try {
				pr.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}		
	}
	
	public static class openShell implements Runnable {
		
		private String program;
		//private BufferedReader processOutput;
		private static BufferedWriter processInput;
		
		public openShell(String program) {
		       this.program = program;
		}
		
	    public void run() {
	    	try
	        {
	            Runtime rt = Runtime.getRuntime();

	            Process pr = rt.exec(program);

	            //processOutput = new BufferedReader(new InputStreamReader(pr.getInputStream()));
	            processInput = new BufferedWriter(new OutputStreamWriter(pr.getOutputStream()));
	            
	            Runnable read = new readStream(pr);
	            new Thread(read).start();          
	            
	            /*while(true)
	            {
	                String line = processOutput.readLine();
	                if(line == null) break;
	                System.out.println(line);
	            }*/

	            //processInput.close();
	            //processOutput.close();
	            pr.waitFor();
	        }
	        catch(Exception x)
	        {
	            x.printStackTrace();
	        }
	    }
	    
	    @SuppressWarnings("unused")
		private static void sendCommand(String command) {
            try {
				processInput.write(command);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            try {
				processInput.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }

	}
	
	public static ArrayList<Integer> setupFramesSubtract(String fullBitstream, String maskBitstream) {
		FrameExtractor extractor = new FrameExtractor();
		ArrayList<Integer> frames = extractor.getFrames(fullBitstream);
		extractor.subtractFrames(frames, extractor.getFrames(maskBitstream));
		return frames;
	}
	
	public static ArrayList<String> setupVoterList(String voterListPath) {
		FrameExtractor extractor = new FrameExtractor();
		return extractor.readVoterList(voterListPath);
	}

	public static String getBenchmarkName() {
		return benchmarkName;
	}

	public static void setBenchmarkName(String benchmarkName) {
		FaultInjectionPlatform.benchmarkName = benchmarkName;
	}

}
