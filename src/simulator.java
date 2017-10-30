import java.awt.List;
import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class simulator {
	public static int FP_adder;
	public static int[] Add_arr; 
	public static int Add_exe;
	public static int FP_mult;
	public static int[] Mult_arr;
	public static int Mult_exe;
	public static int FP_div;
	public static int[] Div_arr;
	public static int Div_exe;
	public static int I_cache1;
	public static int I_cache;
	public static int[] DataArray;
	public static String result_filename;
	
	public static void main(String[] args) throws Exception
	{
		String instArray[] = Instr.parse(args[0]);
		DataArray = Data.parse(args[1]);
		int configArray[] = Config.parse(args[2]);
		result_filename = args[3];
        
        //Printing configArray
		 FP_adder = configArray[0];
		 Add_arr = new int[FP_adder];
		 Add_exe = configArray[1];
		 FP_mult = configArray[2];
		 Mult_arr = new int[FP_mult];
		 Mult_exe = configArray[3];
		 FP_div = configArray[4];
		 Div_arr = new int[FP_div];
		 Div_exe = configArray[5];
		 I_cache1 = configArray[6];
		 I_cache = configArray[7];
		          
        //Calling Simulator
         Simul.sim(instArray);
        
	}
}

