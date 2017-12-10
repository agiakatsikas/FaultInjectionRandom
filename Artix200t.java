package random;

import java.util.ArrayList;
import java.util.Arrays;

/*

Address Type Bit Index Description

Block Type [25:23] : Valid block types are CLB, I/O, CLK (000), block RAM content
(001), and CFG_CLB (010). A normal bitstream does not include type 011.

Top/Bottom Bit 22 	: Select between top-half rows (0) and bottom-half rows (1).
Row Address [21:17] : Selects the current row. The row addresses increment from center
to top and then reset and increment from center to bottom.

Column Address [16:7]: Selects a major column, such as a column of CLBs. Column
addresses start at 0 on the left and increase to the right.

Minor Address [6:0] :  Selects a frame within a major column.

*/

public class Artix200t {
	private static final ArrayList<Integer> BRAMColumns = new ArrayList<>(Arrays.asList(6, 17, 28, 40, 51, 58, 69, 88, 99));
	private static final ArrayList<Integer> DSPColumns  = new ArrayList<>(Arrays.asList(9, 14, 31, 43, 48, 61, 66, 91, 96));
	private static final ArrayList<Integer> IOColumns = new ArrayList<>(Arrays.asList(0, 105));
	private static final ArrayList<Integer> ClockInterColumns = new ArrayList<>(Arrays.asList(55)); //Clock interconnection resources
	private static final ArrayList<Integer> ConfigColumns = new ArrayList<>(Arrays.asList(24)); // ICAP etc.
	private static final ArrayList<Integer> ClockManagementColumns = new ArrayList<>(Arrays.asList(1,104)); // CMT etc.
	public static final int wordInFrame = 101;
	public static final int bitsInFrame = wordInFrame * 32;
	public static final int rows = 5;
	public static final int columns = 106;
	
	public static enum BlockType {
	    CLB_IO_CLK, BRAM_CONTENT, CFG_CLB, UNKNOWN
	}
	
	public static enum TopBottom {
	    TOP, BOTTOM
	}
	
	public static enum ColumnType {
	    CLB, DSP, BRAM, IOB, CLOCK_INTER, FPGA_CONFIG, CMT
	}
	
	
	public static  TopBottom getTopBottom (int frameAddress) {
		int mask = ( frameAddress >> 22 ) & 0x00000001;
		if (mask == 0) {
			return TopBottom.TOP;
		} else {
			return TopBottom.BOTTOM;
		} 	
	}
	
	public  static BlockType getBlockType(int frameAddress) {
		int masked = (frameAddress  >> 23) & 0x00000007;
		if (masked == 0) {
			return BlockType.CLB_IO_CLK;
		} else if (masked == 1) {
			return BlockType.BRAM_CONTENT;
		} else if (masked == 2) {
			return BlockType.CFG_CLB;
		}
		return BlockType.UNKNOWN;	
	}
	
	public static  ColumnType getColumnType (int frameAddress) {
		int masked = (frameAddress  >> 7) & 0x00003FF;
		if (BRAMColumns.contains(masked)) {
			return ColumnType.BRAM;
		} else if (DSPColumns.contains(masked)) {
			return ColumnType.DSP;
		} else if (IOColumns.contains(masked)) {
			return ColumnType.IOB;
		} else if (ClockInterColumns.contains(masked)) {
			return ColumnType.CLOCK_INTER;
		} else if (ConfigColumns.contains(masked)) {
			return ColumnType.FPGA_CONFIG;
		} else if (ClockManagementColumns.contains(masked)) {
			return ColumnType.CMT;
		} else {
			return ColumnType.CLB;
		}		

	}
	
	public static  int getColumnNumber (int frameAddress) {
		int masked = (frameAddress  >> 7) & 0x00003FF;
		return masked;

	}
	
	public static  int getRowNumber (int frameAddress) {
		int masked = (frameAddress  >> 17) & 0x000001F;
		return masked;

	}
	
	
}
