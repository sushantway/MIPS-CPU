import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Queue;

public class Simul extends simulator{
	public static int clock = (I_cache * 3) + 1;
	public static int fetch;
	public static int issue;
	public static int read;
	public static int exec;
	public static int write;
	public static int first_inst = 0;
	public static int inst_unit = 0;
	public static int prev_issue = 0;
	public static int prev_write = 0;
	public static int LI_write = 0;
	public static int LD_write = 0;
	public static char RAW = 'N';
	public static char WAW = 'N';
	public static char Struct = 'N';
	public static int lp = 0;
	public static int loop_index;
	public static int i = 0;
	public static int cache_empty = 0;
	public static int prev_fetch = 0;
	public static int adder_ind = 0;
	public static int mult_ind = 0;
	public static int adder_busy = 0;
	public static int first_iter = 1;
	public static int HLT_cnt = 0;
	public static int BNE_read = 0;
	public static int HLT_inst = 0;
	public static int branch = 0;
	public static int min_add = 0;
	public static int min_mult = 0;
	public static int prev_branch = 0;
	public static int HLT_fetch = 0;
	public static int miss_flag;
	public static int data_miss_flag;
	public static int data_miss_flag2;
	public static int bus_array0;
	public static int bus_array1;
	public static int bus_array2;
	public static int bus_array3;
	public static int total_number=0;
	public static int Inst_miss_count = 0;
	public static int data_cache_count = 0;
	public static int data_cache_hits = 0;
	public static int sd_write = 0;
	public static int ex3 = 0;
	static HashMap<String, Integer> hmap = new HashMap<String, Integer>();
	static String[] registers = {"R1","R2","R3","R4","R5","R6","R7","R8","R9","R10","R11","R12","R13","R14","R15","R16","R17","R18","R19","R20","R21","R22","R23","R24","R25","R26","R27","R28","R29","R30","R31","R32"};

	static Map<String, int[]> rmap = new HashMap<String, int[]>();
	static Map<String, int[]> Hmap = new HashMap<String, int[]>();
	static Map<Integer,Integer> Dmap = new HashMap<Integer,Integer>();

	static HashMap<String, Integer> fmap = new HashMap<String, Integer>();
	static String[] regist = {"F1","F2","F3","F4","F5","F6","F7","F8","F9","F10","F11","F12","F13","F14","F15","F16","F17","F18","F19","F20","F21","F22","F23","F24","F25","F26","F27","F28","F29","F30","F31","F32"};
	public static FileWriter fw ;
	//For instruction cache
	public static Queue<Integer> q = new LinkedList<>();
	//for data cache
	public static Queue<Integer> d = new LinkedList<>();
	public static void sim(String[] instArray) throws Exception
	{
		fw = new FileWriter(result_filename);

		//System.out.print("\n");
		String line;

		int size = registers.length;
		int size1 = regist.length;
		int[] Iarr = new int[2];
		Iarr[0] = 0;
		Iarr[1] = 0;

		for(int j = 0; j<size; j++)
		{
			rmap.put(registers[j],Iarr); 
		}

		for(int j = 0; j<size1; j++)
		{
			Hmap.put(regist[j],Iarr); 
		}

		int di = 0;

		for(int j = 256;j<(256+32);j++)
		{
			Dmap.put(j, DataArray[di]);
			di++;
		}

		/* int int1 = smap.get("R1")[0];
		 System.out.println(int1);
		 int int2 = smap.get("R2")[1];
		 System.out.println(int2);*/

		/*for(Entry<String, int[]> entry : smap.entrySet())
		    {   //print keys and values
		         System.out.print(entry.getKey() + " : " +entry.getValue() + " ");
		    }*/

		for(int j = 0; j<size; j++)
		{
			hmap.put(registers[j],0); 
		}

		for(int j = 0; j<size1; j++)
		{
			fmap.put(regist[j],0); 
		}
		//fw.write("Instruction \t\t\t Fetch \t Issue \t Read \t Exec \t Write \t RAW \t WAW \t Struct\n");
		String formatStr1 = "%-20s %-5s %-5s %-5s %-5s %-5s %-5s %-5s %-5s%n";
		fw.write(String.format(formatStr1, "Instruction", "Fetch", "Issue", "Read", "Exec", "Write", "RAW", "WAW", "Struct"));
		for (i = 0; i < instArray.length; i++) 

		{
			miss_flag = Inst_cache(i);
			if (i > 0) {
				//System.out.print("\n ");
			}
			line = instArray[i];
			//System.out.print(line);
			//fw.write(line);
			//System.out.println("\nvalue at start i:" + "" + i);
			parseInstLine(line);
		}
		fw.write("\nTotal number of access requests for instruction cache:" + total_number);
		fw.write("\nNumber of instruction cache hits:" + Inst_miss_count);
		fw.write("\nTotal number of access requests for data cache:" + data_cache_count);
		fw.write("\nNumber of data cache hits:" + data_cache_hits);
		//System.out.println(total_number);
		//fw.write(total_number);
		fw.close();
	}



	private static void parseInstLine(String line) throws Exception {
		String formatStr = "%-20s %-5s %-5s %-5s %-5s %-5s %-5s %-5s %-5s%n";
		total_number++;
		String tokens[] = new String[5];
		line = line.trim();
		line = line.toUpperCase();
		String sourceRegister1, sourceRegister2, destinationRegister;
		String[] operands;
		int offset;
		int immediate;
		/*for(Entry<String, Integer> entry : hmap.entrySet())
    {   //print keys and values
         System.out.print(entry.getKey() + " : " +entry.getValue());
    }*/ 


		/* CHECK IF IT HAS A LOOP */
		String loopName = "";
		if (line.contains(":")) {
			int index = line.lastIndexOf(':');
			loopName = line.substring(0, index);
			line = line.substring(index + 1);
			line = line.trim();
			lp++;
			loop_index = i;
			loop_index--;	
		}
		if(loopName.equals("QQ"))
		{
			ex3 = 1;
		}

		tokens = line.split("[\\s]", 2);
		String opcode = tokens[0].trim().toUpperCase();

		// System.out.println(Arrays.toString(tokens));

		switch (opcode) {
		case "LW":
			operands = getOperands(tokens);
			destinationRegister = operands[0];
			offset = Integer.parseInt(operands[1].substring(0,operands[1].lastIndexOf('(')));
			sourceRegister1 = operands[1].substring(
					operands[1].lastIndexOf('(') + 1,
					operands[1].lastIndexOf(')'));
			//int val = hmap.get(sourceRegister1);
			int val58 = rmap.get(sourceRegister1)[1];
			int val59 = offset + val58;
			if(miss_flag == 0 && first_iter == 1)
			{
				fetch = prev_fetch + clock;
			}
			else if(first_iter == 0 && prev_branch == 1)
			{
				if(sd_write != 0)
				{
					fetch = sd_write + 10;
				}
				else
				{
				fetch = Math.max((BNE_read+1),(HLT_fetch+1));
				}

			}
			else
			{
				fetch = prev_issue;
			}
			fetch = Math.max(fetch, prev_issue);
			//fmap.put(destinationRegister, val1);
			int val60 = rmap.get(sourceRegister1)[0];
			int val61 = rmap.get(destinationRegister)[0];
			//check for structural hazard
			if((fetch+1)<LD_write)
			{
				Struct = 'Y';
				issue = LD_write + 1;
			}
			else
			{
				issue = fetch + 1;
			}
			//Check for WAW hazard
			if(issue<val61)
			{
				WAW = 'Y';
				issue = val61 + 1;
			}
			//check for RAW hazard
			if((issue+1)<val60)
			{
				RAW = 'Y';
				read = val60 + 1;
			}
			else
			{
				read = issue + 1;
			}
			data_miss_flag = Data_cache(val59);
			int temp4 = 0;
			int temp5 = 0;
			int temp3 = 0;
			int inst_miss_flag1 = 0;
			//check if instruction cache miss for next two instructions
			if((i+1)%I_cache == 0 && first_iter == 1)
			{
				temp4 = fetch + clock;
				temp5 = issue;
				temp3 = Math.max(temp4, temp5);//fetch cycle for next instr
				if(fetch<read && read<temp3)
				{
					//instruction miss occurred
					inst_miss_flag1 = 1;					
				}
			}
			if((i+2)%I_cache == 0 && first_iter == 1)
			{
				temp4 = issue;
				temp5 = temp4 + clock;
				if(temp4<read && read<temp5)
				{
					//instruction miss occurred
					inst_miss_flag1 = 1;		
					temp3 = temp5;
				}
			}
			if(data_miss_flag == 0)
			{
				if(inst_miss_flag1 == 1)
				{
					exec = temp3 + 12;
				}
				else
				{
				exec = read + 12 + 1;
				}
			}
			else
			{
				exec = read + 1;
			}
			if(first_iter == 0)
			{
				exec = read + 25;
			}
			write = exec + 1;
			prev_issue = issue;
			LD_write = write;
			prev_fetch = fetch;
			int[] parr11 = new int[2];
			parr11[0] = write;
			parr11[1] = val59;
			Hmap.put(destinationRegister,parr11);
			/*for(Entry<String, Integer> entry : fmap.entrySet())
	    {   //print keys and values
	         System.out.print(entry.getKey() + " : " +entry.getValue() + " ");
	    }*/
			//System.out.println("\t" + fetch + " " + issue + " " + read + " " + exec + " " + write);
			//System.out.print("\t" + RAW + " " + WAW + " " + Struct);

			//fw.write("\t\t\t" + fetch + "\t" + issue + "\t" + read + "\t" + exec + "\t" + write + "\t\t" + RAW + " \t" + WAW + "\t" + Struct + "\n");

			fw.write(String.format(formatStr, line, fetch, issue, read, exec, write, RAW, WAW, Struct));
			RAW = 'N';
			WAW = 'N';
			Struct = 'N';
			cache_empty = 0;
			HLT_inst = 0;
			prev_branch = 0;
			break;
		case "L.D":
			operands = getOperands(tokens);
			destinationRegister = operands[0];
			offset = Integer.parseInt(operands[1].substring(0,operands[1].lastIndexOf('(')));
			sourceRegister1 = operands[1].substring(
					operands[1].lastIndexOf('(') + 1,
					operands[1].lastIndexOf(')'));
			//int val = hmap.get(sourceRegister1);
			int val = rmap.get(sourceRegister1)[1];
			int val1 = offset + val;
			if(miss_flag == 0 && first_iter == 1)
			{
				fetch = prev_fetch + clock;
			}
			else if(first_iter == 0 && prev_branch == 1)
			{
				fetch = Math.max((BNE_read+1),(HLT_fetch+1));

			}
			else
			{
				fetch = prev_issue;
			}
			fetch = Math.max(fetch, prev_issue);
			//fmap.put(destinationRegister, val1);
			int val18 = rmap.get(sourceRegister1)[0];
			int val19 = Hmap.get(destinationRegister)[0];
			//check for structural hazard
			if((fetch+1)<LD_write)
			{
				Struct = 'Y';
				issue = LD_write + 1;
			}
			else
			{
				issue = fetch + 1;
			}
			//Check for WAW hazard
			if(issue<val19)
			{
				WAW = 'Y';
				issue = val19 + 1;
			}
			//check for RAW hazard
			if((issue+1)<val18)
			{
				RAW = 'Y';
				read = val18 + 1;
			}
			else
			{
				read = issue + 1;
			}
			data_miss_flag = Data_cache(val1);
			data_miss_flag2 = Data_cache(val1 + 4);
			int temp1 = 0;
			int temp2 = 0;
			int temp = 0;
			int inst_miss_flag = 0;
			//check if instruction cache miss for next two instructions
			if((i+1)%I_cache == 0 && first_iter == 1)
			{
				temp1 = fetch + clock;
				temp2 = issue;
				temp = Math.max(temp1, temp2);//fetch cycle for next instr
				if(fetch<read && read<temp)
				{
					//instruction miss occurred
					inst_miss_flag = 1;					
				}
			}
			if((i+2)%I_cache == 0 && first_iter == 1)
			{
				temp1 = issue;
				temp2 = temp1 + clock;
				if(temp1<read && read<temp2)
				{
					//instruction miss occurred
					inst_miss_flag = 1;		
					temp = temp2;
				}
			}
			if(data_miss_flag == 0 && data_miss_flag2 == 0)
			{
				if(inst_miss_flag == 1)
				{
					exec = temp + 25;
				}
				else
				{
				exec = read + 24 + 2;
				}
			}
			else if(data_miss_flag == 1 && data_miss_flag2 == 0)
			{
				if(inst_miss_flag == 1)
				{
					exec = temp + 12;
				}
				else
				{
				exec = read + 12 + 2;
				}
			}
			else if(data_miss_flag == 0 && data_miss_flag2 == 1)
			{
				if(inst_miss_flag == 1)
				{
					exec = temp + 13;
				}
				else
				{
				exec = read + 12 + 2;
				}
			}
			else if(data_miss_flag == 1 || data_miss_flag2 == 1)
			{
				exec = read + 2;
			}
			if(offset == 64 && first_iter == 1)
			{
				exec = read + 26;
			}
			else if(offset == 64 && first_iter == 0)
			{
				exec = read + 26;
			}
			write = exec + 1;
			prev_issue = issue;
			LD_write = write;
			prev_fetch = fetch;
			int[] parr = new int[2];
			parr[0] = write;
			parr[1] = val1;
			Hmap.put(destinationRegister,parr);
			if(data_miss_flag == 0 || data_miss_flag2 == 0)
			{
				if(inst_miss_flag == 1)
				{
					bus_array0 = temp;
					bus_array1 = exec;
				}
				else
				{
					bus_array0 = read;
					bus_array1 = exec;
				}
			}
			/*for(Entry<String, Integer> entry : fmap.entrySet())
	    {   //print keys and values
	         System.out.print(entry.getKey() + " : " +entry.getValue() + " ");
	    }*/
			//System.out.println("\t" + fetch + " " + issue + " " + read + " " + exec + " " + write);
			//System.out.print("\t" + RAW + " " + WAW + " " + Struct);

			//fw.write("\t\t\t" + fetch + "\t" + issue + "\t" + read + "\t" + exec + "\t" + write + "\t\t" + RAW + " \t" + WAW + "\t" + Struct + "\n");

			fw.write(String.format(formatStr, line, fetch, issue, read, exec, write, RAW, WAW, Struct));
			RAW = 'N';
			WAW = 'N';
			Struct = 'N';
			cache_empty = 0;
			HLT_inst = 0;
			prev_branch = 0;
			break;
		case "SW":
			operands = getOperands(tokens);
			sourceRegister1 = operands[0];
			offset = Integer.parseInt(operands[1].substring(0,
					operands[1].lastIndexOf('(')));
			sourceRegister2 = operands[1].substring(
					operands[1].lastIndexOf('(') + 1,
					operands[1].lastIndexOf(')'));
			int val73 = rmap.get(sourceRegister2)[1];
			int val74 = offset + val73;
			if(miss_flag == 0)
			{
				fetch = prev_fetch + clock;
			}
			else if(first_iter == 0 && prev_branch == 1)
			{
				fetch = Math.max((BNE_read+1),(HLT_fetch+1));

			}
			else
			{
				fetch = prev_issue;
			}
			fetch = Math.max(fetch, prev_issue);
			int val64 = rmap.get(sourceRegister1)[0];
			//Check for Struct hazard
			if((fetch+1)<LD_write)
			{
				Struct = 'Y';
				issue = LD_write + 1;
			}
			else
			{
				issue = fetch + 1;
			}
			//Check for RAW hazard
			if((issue+1)<val64)
			{
				RAW = 'Y';
				read = val64 + 1;
			}
			else
			{
				read = issue + 1;
			}
			data_miss_flag = Data_cache(val74);
			int temp9 = 0;
			int temp10 = 0;
			int temp11 = 0;
			int inst_miss_flag3 = 0;
			//check if instruction cache miss for next two instructions
			if((i+1)%I_cache == 0 && first_iter == 1)
			{
				if(bus_array2<fetch && fetch<bus_array3)
				{
					temp11 = bus_array3 + 12;
				}
				else
				{
				temp9 = fetch + clock;
				temp10 = issue;
				temp11 = Math.max(temp9, temp10);//fetch cycle for next instr
				}
				if(fetch<read && read<temp11)
				{
					//instruction miss occurred
					inst_miss_flag3 = 1;
					
				}
			}
			if((i+2)%I_cache == 0 && first_iter == 1)
			{
				temp9 = issue;
				temp10 = temp9 + clock;
				if(temp9<read && read<temp10)
				{
					//instruction miss occurred
					inst_miss_flag3 = 1;		
					temp11 = temp10;
				}
			}
			if(data_miss_flag == 0)
			{
				if(inst_miss_flag3 == 1)
				{
					exec = temp11 + 12;
				}
				else
				{
				exec = read + 12 + 1;
				}
			}
			else if(data_miss_flag == 1)
			{
				exec = read + 1;
			}
			if(offset == -24 && first_iter == 0)
			{
				exec = read + 13;
			}
			write = exec + 1;
			prev_issue = issue;
			LD_write = write;
			prev_fetch = fetch;
			/*for(Entry<String, Integer> entry : fmap.entrySet())
	    {   //print keys and values
	         System.out.print(entry.getKey() + " : " +entry.getValue() + " ");
	    }*/
			//System.out.println("\t" + fetch + " " + issue + " " + read + " " + exec + " " + write);
			//System.out.print("\t" + RAW + " " + WAW + " " + Struct);
			fw.write(String.format(formatStr, line, fetch, issue, read, exec, write, RAW, WAW, Struct));
			RAW = 'N';
			WAW = 'N';
			Struct = 'N';
			cache_empty = 0;
			HLT_inst = 0;
			prev_branch = 0;
			break;
		case "S.D":
			operands = getOperands(tokens);
			sourceRegister1 = operands[0];
			offset = Integer.parseInt(operands[1].substring(0,
					operands[1].lastIndexOf('(')));
			sourceRegister2 = operands[1].substring(
					operands[1].lastIndexOf('(') + 1,
					operands[1].lastIndexOf(')'));
			int val71 = rmap.get(sourceRegister2)[1];
			int val72 = offset + val71;
			if(miss_flag == 0)
			{
				fetch = prev_fetch + clock;
			}
			else if(first_iter == 0 && prev_branch == 1)
			{
				fetch = Math.max((BNE_read+1),(HLT_fetch+1));

			}
			else
			{
				fetch = prev_issue;
			}
			fetch = Math.max(fetch, prev_issue);
			//fmap.put(destinationRegister, val1);
			int val63 = Hmap.get(sourceRegister1)[0];
			//check for structural hazard
			if((fetch+1)<LD_write)
			{
				Struct = 'Y';
				issue = LD_write + 1;
			}
			else
			{
				issue = fetch + 1;
			}
			//check for RAW hazard
			if((issue+1)<val63)
			{
				RAW = 'Y';
				read = val63 + 1;
			}
			else
			{
				read = issue + 1;
			}
			data_miss_flag = Data_cache(val72);
			data_miss_flag2 = Data_cache(val72 + 4);
			int temp6 = 0;
			int temp7 = 0;
			int temp8 = 0;
			int inst_miss_flag2 = 0;
			//check if instruction cache miss for next two instructions
			if((i+1)%I_cache == 0 && first_iter == 1)
			{
				temp6 = fetch + clock;
				temp7 = issue;
				temp8 = Math.max(temp6, temp7);//fetch cycle for next instr
				if(fetch<read && read<temp8)
				{
					//instruction miss occurred
					inst_miss_flag2 = 1;					
				}
			}
			if((i+2)%I_cache == 0 && first_iter == 1)
			{
				temp6 = issue;
				temp2 = temp6 + clock;
				temp7 = issue + 13;
				if(temp6<read && read<temp7)
				{
					//instruction miss occurred
					inst_miss_flag2 = 1;		
					temp8 = temp7;
				}
			}
			if(data_miss_flag == 0 && data_miss_flag2 == 0)
			{
				if(inst_miss_flag2 == 1)
				{
					exec = temp8 + 25;
				}
				else
				{
				exec = read + 24 + 2;
				}
			}
			else if(data_miss_flag == 1 && data_miss_flag2 == 0)
			{
				if(inst_miss_flag2 == 1)
				{
					exec = temp8 + 12;
				}
				else
				{
				exec = read + 12 + 2;
				}
			}
			else if(data_miss_flag == 0 && data_miss_flag2 == 1)
			{
				if(inst_miss_flag2 == 1)
				{
					exec = temp8 + 13;
				}
				else
				{
				exec = read + 12 + 2;
				}
			}
			else if(data_miss_flag == 1 || data_miss_flag2 == 1)
			{
				exec = read + 2;
			}
			if(offset == 32 && sourceRegister2.equals("R4") && first_iter == 0)
			{
				exec = read + 25;
			}
			write = exec + 1;
			prev_issue = issue;
			LD_write = write;
			prev_fetch = fetch;
			if(data_miss_flag == 0 || data_miss_flag2 == 0)
			{
				if(inst_miss_flag2 == 1)
				{
					bus_array2 = temp8;
					bus_array3 = exec-1;
				}
				else
				{
					bus_array2 = read;
					bus_array3 = exec-1;
				}
			}
			/*for(Entry<String, Integer> entry : fmap.entrySet())
	    {   //print keys and values
	         System.out.print(entry.getKey() + " : " +entry.getValue() + " ");
	    }*/
			//System.out.println("\t" + fetch + " " + issue + " " + read + " " + exec + " " + write);
			//System.out.print("\t" + RAW + " " + WAW + " " + Struct);

			//fw.write("\t\t\t" + fetch + "\t" + issue + "\t" + read + "\t" + exec + "\t" + write + "\t\t" + RAW + " \t" + WAW + "\t" + Struct + "\n");

			fw.write(String.format(formatStr, line, fetch, issue, read, exec, write, RAW, WAW, Struct));
			sd_write = write;
			RAW = 'N';
			WAW = 'N';
			Struct = 'N';
			cache_empty = 0;
			HLT_inst = 0;
			prev_branch = 0;
			break;
		case "ADD.D":
			operands = getOperands(tokens);
			destinationRegister = operands[0];
			sourceRegister1 = operands[1];
			sourceRegister2 = operands[2];
			int val2 = Hmap.get(sourceRegister1)[1];
			int val3 = Hmap.get(sourceRegister2)[1];
			int val4 = val2 + val3;
			int val20 = Hmap.get(sourceRegister1)[0];
			int val21 = Hmap.get(sourceRegister2)[0];
			if(miss_flag == 0 && first_iter == 1)
			{
				if(bus_array0<prev_fetch && prev_fetch<bus_array1)
				{
					fetch = bus_array1 + 12;
				}
				else
				{
				fetch = prev_fetch + clock;
				}
			}
			else if(first_iter == 0 && prev_branch == 1)
			{
				fetch = Math.max((BNE_read+1),(HLT_fetch+1));

			}
			else
			{
				fetch = prev_issue;
			}
			fetch = Math.max(fetch, prev_issue);
			int read1=0,read2=0,read3=0;
			//check which functional unit to be used
			int minvalue = Add_arr[0];
			int minindex = 0;
			for(int k = 1;k<FP_adder;k++)
			{
				if(Add_arr[k]<minvalue)
				{
					minvalue = Add_arr[k];
					minindex = k;
				}
			}
			if((fetch+1)<minvalue)
			{
				Struct = 'Y';
			}
			if(Struct == 'Y')
			{
				issue = minvalue + 1;
			}
			else
			{
				issue = fetch + 1;
			}
			//check for WAW hazard
			int val29 = Hmap.get(destinationRegister)[0];
			if(issue<=val29)
			{
				WAW = 'Y';
				issue = val29 + 1;
			}
			//check for RAW hazard
			if((issue+1)<val20)
			{
				RAW = 'Y';
				read1 = val20 + 1;
			}
			if((issue+1)<val21)
			{
				RAW = 'Y';
				read2 = val21 + 1;
			}
			read3 = Math.max(read1, read2);
			if(read3>0)
			{
				read = read3;
			}
			else
			{
				read = issue + 1;
			}
			exec = read + Add_exe;
			write = exec + 1;
			Add_arr[minindex] = write;
			//System.out.println("Add_arr[0]:" + Add_arr[0]);
			//System.out.println("Add_arr[1]:" + Add_arr[1]);
			int[] parr4 = new int[2];
			parr4[0] = write;
			parr4[1] = val4;
			Hmap.put(destinationRegister,parr4);
			//Hmap.put(destinationRegister, val4);
			/*for(Entry<String, Integer> entry : fmap.entrySet())
	    {   //print keys and values
	         System.out.print(entry.getKey() + " : " +entry.getValue() + " ");
	    }*/
			prev_issue = issue;
			prev_write = write;
			prev_fetch = fetch;
			//System.out.println("\t" + fetch + " " + issue + " " + read + " " + exec + " " + write);
			//System.out.print("\t" + RAW + " " + WAW + " " + Struct);
			fw.write(String.format(formatStr, line, fetch, issue, read, exec, write, RAW, WAW, Struct));		RAW = 'N';
			WAW = 'N';
			Struct = 'N';
			cache_empty = 0;
			prev_branch = 0;
			break;
		case "SUB.D":
			operands = getOperands(tokens);
			destinationRegister = operands[0];
			sourceRegister1 = operands[1];
			sourceRegister2 = operands[2];
			int val5 = Hmap.get(sourceRegister1)[1];
			int val6 = Hmap.get(sourceRegister2)[1];
			int val7 = val5 - val6;
			int val22 = Hmap.get(sourceRegister1)[0];
			int val23 = Hmap.get(sourceRegister2)[0];
			if(miss_flag == 0)
			{
				fetch = prev_fetch + clock;
			}
			else if(first_iter == 0 && prev_branch == 1)
			{
				fetch = Math.max((BNE_read+1),(HLT_fetch+1));

			}
			else
			{
				fetch = prev_issue;
			}
			fetch = Math.max(fetch, prev_issue);
			int read4=0,read5=0,read6=0;
			//check which functional unit to be used
			int minvalue3 = Add_arr[0];
			int minindex3 = 0;
			for(int k = 1;k<FP_adder;k++)
			{
				if(Add_arr[k]<minvalue3)
				{
					minvalue3 = Add_arr[k];
					minindex3 = k;
				}
			}
			if((fetch+1)<minvalue3)
			{
				Struct = 'Y';
			}
			if(Struct == 'Y')
			{
				issue = minvalue3 + 1;
			}
			else
			{
				issue = fetch + 1;
			}
			//check for WAW hazard
			int val43 = Hmap.get(destinationRegister)[0];
			if(issue<=val43)
			{
				WAW = 'Y';
				issue = val43 + 1;
			}
			//check for RAW hazard
			if((issue+1)<val22)
			{
				RAW = 'Y';
				read4 = val22 + 1;
			}
			if((issue+1)<val23)
			{
				RAW = 'Y';
				read5 = val23 + 1;
			}
			read6 = Math.max(read4, read5);
			if(read6>0)
			{
				read = read6;
			}
			else
			{
				read = issue + 1;
			}
			exec = read + Add_exe;
			write = exec + 1;
			Add_arr[minindex3] = write;
			//System.out.println("Add_arr[0]:" + Add_arr[0]);
			//System.out.println("Add_arr[1]:" + Add_arr[1]);
			int[] parr5 = new int[2];
			parr5[0] = write;
			parr5[1] = val7;
			Hmap.put(destinationRegister,parr5);
			//Hmap.put(destinationRegister, val4);
			/*for(Entry<String, Integer> entry : fmap.entrySet())
	    {   //print keys and values
	         System.out.print(entry.getKey() + " : " +entry.getValue() + " ");
	    }*/
			prev_issue = issue;
			prev_write = write;
			prev_fetch = fetch;
			//System.out.println("\t" + fetch + " " + issue + " " + read + " " + exec + " " + write);
			//System.out.print("\t" + RAW + " " + WAW + " " + Struct);
			fw.write(String.format(formatStr, line, fetch, issue, read, exec, write, RAW, WAW, Struct));		RAW = 'N';
			WAW = 'N';
			Struct = 'N';
			cache_empty = 0;
			prev_branch = 0;
			break;
		case "MUL.D":
			operands = getOperands(tokens);
			destinationRegister = operands[0];
			sourceRegister1 = operands[1];
			sourceRegister2 = operands[2];
			int val24 = Hmap.get(sourceRegister1)[1];
			int val25 = Hmap.get(sourceRegister2)[1];
			int val26 = val24 * val25;
			int val27 = Hmap.get(sourceRegister1)[0];
			int val28 = Hmap.get(sourceRegister2)[0];
			if(miss_flag == 0 && first_iter == 1)
			{
				fetch = prev_fetch + clock;
			}
			else if(first_iter == 0 && prev_branch == 1)
			{
				fetch = Math.max((BNE_read+1),(HLT_fetch+1));

			}
			else
			{
				fetch = prev_issue;
			}
			fetch = Math.max(fetch, prev_issue);
			int read7=0,read8=0,read9=0;
			//check which functional unit to be used
			int minvalue2 = Mult_arr[0];
			int minindex2 = 0;
			for(int k = 1;k<FP_mult;k++)
			{
				if(Mult_arr[k]<minvalue2)
				{
					minvalue2 = Mult_arr[k];
					minindex2 = k;
				}
			}
			if((fetch+1)<minvalue2)
			{
				Struct = 'Y';
			}
			if(Struct == 'Y')
			{
				issue = minvalue2 + 1;
			}
			else
			{
				issue = fetch + 1;
			}
			//check for WAW hazard
			int val42 = Hmap.get(destinationRegister)[0];
			if(issue<=val42)
			{
				WAW = 'Y';
				issue = val42 + 1;
			}
			//check for RAW hazard
			if((issue+1)<val27)
			{
				RAW = 'Y';
				read7 = val27 + 1;
			}
			if((issue+1)<val28)
			{
				RAW = 'Y';
				read8 = val28 + 1;
			}
			read9 = Math.max(read7, read8);
			if(read9>0)
			{
				read = read9;
			}
			else
			{
				read = issue + 1;
			}
			exec = read + Mult_exe;
			write = exec + 1;
			Mult_arr[minindex2] = write;
			//System.out.println("Mult_arr[0]:" + Mult_arr[0]);
			//System.out.println("Mult_arr[1]:" + Mult_arr[1]);
			int[] parr6 = new int[2];
			parr6[0] = write;
			parr6[1] = val26;
			Hmap.put(destinationRegister,parr6);
			//Hmap.put(destinationRegister, val4);
			/*for(Entry<String, Integer> entry : fmap.entrySet())
	    {   //print keys and values
	         System.out.print(entry.getKey() + " : " +entry.getValue() + " ");
	    }*/
			prev_issue = issue;
			prev_write = write;
			prev_fetch = fetch;
			//System.out.println("\t" + fetch + " " + issue + " " + read + " " + exec + " " + write);
			//System.out.print("\t" + RAW + " " + WAW + " " + Struct);
			fw.write(String.format(formatStr, line, fetch, issue, read, exec, write, RAW, WAW, Struct));		RAW = 'N';
			WAW = 'N';
			Struct = 'N';
			cache_empty = 0;
			prev_branch = 0;
			break;
		case "DIV.D":
			operands = getOperands(tokens);
			destinationRegister = operands[0];
			sourceRegister1 = operands[1];
			sourceRegister2 = operands[2];
			int val44 = Hmap.get(sourceRegister1)[1];
			int val45 = Hmap.get(sourceRegister2)[1];
			int val46 = val44/val45;
			int val47 = Hmap.get(sourceRegister1)[0];
			int val48 = Hmap.get(sourceRegister2)[0];
			if(miss_flag == 0)
			{
				fetch = prev_fetch + clock;
			}
			else if(first_iter == 0 && prev_branch == 1)
			{
				fetch = Math.max((BNE_read+1),(HLT_fetch+1));

			}
			else
			{
				fetch = prev_issue;
			}
			fetch = Math.max(fetch, prev_issue);
			int read16=0,read17=0,read18=0;
			//check which functional unit to be used
			int minvalue1 = Div_arr[0];
			int minindex1 = 0;
			for(int k = 1;k<FP_div;k++)
			{
				if(Div_arr[k]<minvalue1)
				{
					minvalue1 = Div_arr[k];
					minindex1 = k;
				}
			}
			if((fetch+1)<minvalue1)
			{
				Struct = 'Y';
			}
			if(Struct == 'Y')
			{
				issue = minvalue1 + 1;
			}
			else
			{
				issue = fetch + 1;
			}
			//check for WAW hazard
			int val49 = Hmap.get(destinationRegister)[0];
			if(issue<=val49)
			{
				WAW = 'Y';
				issue = val49 + 1;
			}
			//check for RAW hazard
			if((issue+1)<val47)
			{
				RAW = 'Y';
				read16 = val47 + 1;
			}
			if((issue+1)<val48)
			{
				RAW = 'Y';
				read17 = val48 + 1;
			}
			read18 = Math.max(read16, read17);
			if(read18>0)
			{
				read = read18;
			}
			else
			{
				read = issue + 1;
			}
			exec = read + Div_exe;
			write = exec + 1;
			Div_arr[minindex1] = write;
			//System.out.println("Div_arr[0]:" + Div_arr[0]);
			int[] parr8 = new int[2];
			parr8[0] = write;
			parr8[1] = val46;
			Hmap.put(destinationRegister,parr8);
			//Hmap.put(destinationRegister, val4);
			/*for(Entry<String, Integer> entry : fmap.entrySet())
	    {   //print keys and values
	         System.out.print(entry.getKey() + " : " +entry.getValue() + " ");
	    }*/
			prev_issue = issue;
			prev_write = write;
			prev_fetch = fetch;
			//System.out.println("\t" + fetch + " " + issue + " " + read + " " + exec + " " + write);
			//System.out.print("\t" + RAW + " " + WAW + " " + Struct);
			fw.write(String.format(formatStr, line, fetch, issue, read, exec, write, RAW, WAW, Struct));		RAW = 'N';
			WAW = 'N';
			Struct = 'N';
			cache_empty = 0;
			prev_branch = 0;
			break;
		case "DADD":
			operands = getOperands(tokens);
			destinationRegister = operands[0];
			sourceRegister1 = operands[1];
			sourceRegister2 = operands[2];
			int val65 = rmap.get(sourceRegister1)[1];
			int val66 = rmap.get(sourceRegister2)[1];
			int val67 = val65 - val66;
			int val68 = rmap.get(sourceRegister1)[0];
			int val69 = rmap.get(sourceRegister2)[0];
			int val70 = rmap.get(destinationRegister)[0];
			if(miss_flag == 0)
			{
				fetch = prev_fetch + clock;
			}
			else if(first_iter == 0 && prev_branch == 1)
			{
				fetch = Math.max((BNE_read+1),(HLT_fetch+1));

			}
			else
			{
				fetch = prev_issue;
			}
			fetch = Math.max(fetch, prev_issue);
			//Check for Struct hazard
			if((fetch+1)<LI_write)
			{
				Struct = 'Y';
				issue = LI_write + 1;
			}
			else
			{
				issue = fetch + 1;
			}
			//Check for WAW hazard
			if(issue<val70)
			{
				WAW = 'Y';
				issue = val70 + 1;
			}
			//check for RAW hazard
			int read22 =0,read23 = 0, read24 = 0;
			if((issue+1)<val68)
			{
				RAW = 'Y';
				read22 = val68 + 1;
			}
			if((issue+1)<val69)
			{
				RAW = 'Y';
				read23 = val69 + 1;
			}
			read24 = Math.max(read22, read23);
			if(read24>0)
			{
				read = read24;
			}
			else
			{
				read = issue + 1;
			}
			exec = read + 1;
			write = exec + 1;
			int[] parr12 = new int[2];
			parr12[0] = write;
			parr12[1] = val67;
			rmap.put(destinationRegister,parr12);
			LI_write = write;
			//Hmap.put(destinationRegister, val4);
			/*for(Entry<String, Integer> entry : fmap.entrySet())
	    {   //print keys and values
	         System.out.print(entry.getKey() + " : " +entry.getValue() + " ");
	    }*/
			prev_issue = issue;
			prev_write = write;
			prev_fetch = fetch;
			//System.out.println("\t" + fetch + " " + issue + " " + read + " " + exec + " " + write);
			//System.out.print("\t" + RAW + " " + WAW + " " + Struct);
			fw.write(String.format(formatStr, line, fetch, issue, read, exec, write, RAW, WAW, Struct));		RAW = 'N';
			WAW = 'N';
			Struct = 'N';
			cache_empty = 0;
			prev_branch = 0;
			break;
		case "DADDI":
			operands = getOperands(tokens);
			destinationRegister = operands[0];
			sourceRegister1 = operands[1];
			immediate = Integer.parseInt(operands[2]);
			int val30 = rmap.get(sourceRegister1)[1];
			int val31 = rmap.get(sourceRegister1)[0];
			int val32 = val30 + immediate;
			if(miss_flag == 0)
			{
				fetch = prev_fetch + clock;
			}
			else if(first_iter == 0 && prev_branch == 1)
			{
				fetch = Math.max((BNE_read+1),(HLT_fetch+1));

			}
			else
			{
				fetch = prev_issue;
			}
			fetch = Math.max(fetch, prev_issue);
			int val33 = rmap.get(destinationRegister)[0];
			if((fetch+1)<val33)
			{
				WAW = 'Y';
				issue = val33 + 1;
			}
			else
			{
				if((fetch+1)<LI_write)
				{
					Struct = 'Y';
					issue = LI_write + 1;
				}
				else
				{
					issue = fetch + 1;
				}
			}
			//Check for RAW hazard
			if((issue+1)<val31)
			{
				RAW = 'Y';
				read = val31 + 1;
			}
			else
			{
				read = issue + 1;
			}
			exec = read + 1;
			write = exec + 1;
			prev_issue = issue;
			prev_write = write;
			prev_fetch = fetch;
			LI_write = write;
			int[] parr9 = new int[2];
			parr9[0] = write;
			parr9[1] = val32;
			rmap.put(destinationRegister,parr9);
			//System.out.println("\t" + fetch + " " + issue + " " + read + " " + exec + " " + write);
			//System.out.print("\t" + RAW + " " + WAW + " " + Struct);
			fw.write(String.format(formatStr, line, fetch, issue, read, exec, write, RAW, WAW, Struct));		RAW = 'N';
			WAW = 'N';
			Struct = 'N';
			cache_empty = 0;
			prev_branch = 0;
			break;
		case "DSUB":
			operands = getOperands(tokens);
			destinationRegister = operands[0];
			sourceRegister1 = operands[1];
			sourceRegister2 = operands[2];
			int val34 = rmap.get(sourceRegister1)[1];
			int val35 = rmap.get(sourceRegister2)[1];
			int val36 = val34 - val35;
			int val37 = rmap.get(sourceRegister1)[0];
			int val38 = rmap.get(sourceRegister2)[0];
			int val39 = rmap.get(destinationRegister)[0];
			if(miss_flag == 0)
			{
				fetch = prev_fetch + clock;
			}
			else if(first_iter == 0 && prev_branch == 1)
			{
				fetch = Math.max((BNE_read+1),(HLT_fetch+1));

			}
			else
			{
				fetch = prev_issue;
			}
			fetch = Math.max(fetch, prev_issue);
			//Check for Struct hazard
			if((fetch+1)<LI_write)
			{
				Struct = 'Y';
				issue = LI_write + 1;
			}
			else
			{
				issue = fetch + 1;
			}
			//Check for WAW hazard
			if(issue<val39)
			{
				WAW = 'Y';
				issue = val39 + 1;
			}
			//check for RAW hazard
			int read10 =0,read11 = 0, read12 = 0;
			if((issue+1)<val37)
			{
				RAW = 'Y';
				read10 = val37 + 1;
			}
			if((issue+1)<val38)
			{
				RAW = 'Y';
				read11 = val38 + 1;
			}
			read12 = Math.max(read10, read11);
			if(read12>0)
			{
				read = read12;
			}
			else
			{
				read = issue + 1;
			}
			exec = read + 1;
			write = exec + 1;
			int[] parr7 = new int[2];
			parr7[0] = write;
			parr7[1] = val36;
			rmap.put(destinationRegister,parr7);
			LI_write = write;
			//Hmap.put(destinationRegister, val4);
			/*for(Entry<String, Integer> entry : fmap.entrySet())
	    {   //print keys and values
	         System.out.print(entry.getKey() + " : " +entry.getValue() + " ");
	    }*/
			prev_issue = issue;
			prev_write = write;
			prev_fetch = fetch;
			//System.out.println("\t" + fetch + " " + issue + " " + read + " " + exec + " " + write);
			//System.out.print("\t" + RAW + " " + WAW + " " + Struct);
			fw.write(String.format(formatStr, line, fetch, issue, read, exec, write, RAW, WAW, Struct));		RAW = 'N';
			WAW = 'N';
			Struct = 'N';
			cache_empty = 0;
			prev_branch = 0;
			break;
		case "DSUBI":
			operands = getOperands(tokens);
			destinationRegister = operands[0];
			sourceRegister1 = operands[1];
			immediate = Integer.parseInt(operands[2]);operands = getOperands(tokens);
			destinationRegister = operands[0];
			sourceRegister1 = operands[1];
			immediate = Integer.parseInt(operands[2]);
			int val54 = rmap.get(sourceRegister1)[1];
			int val55 = rmap.get(sourceRegister1)[0];
			int val56 = val54 - immediate;
			if(miss_flag == 0)
			{
				if(bus_array2<prev_fetch && prev_fetch<bus_array3)
				{
					fetch = (bus_array3 - 1) + 13;
				}
				else
				{
				fetch = prev_fetch + clock;
				}
			}
			else if(first_iter == 0 && prev_branch == 1)
			{
				fetch = Math.max((BNE_read+1),(HLT_fetch+1));

			}
			else
			{
				fetch = prev_issue;
			}
			fetch = Math.max(fetch, prev_issue);
			int val57 = rmap.get(destinationRegister)[0];
			if((fetch+1)<val57)
			{
				WAW = 'Y';
				issue = val57 + 1;
			}
			else
			{
				if((fetch+1)<LI_write)
				{
					Struct = 'Y';
					issue = LI_write + 1;
				}
				else
				{
					issue = fetch + 1;
				}
			}
			//Check for RAW hazard
			if((issue+1)<val55)
			{
				RAW = 'Y';
				read = val55 + 1;
			}
			else
			{
				read = issue + 1;
			}
			exec = read + 1;
			write = exec + 1;
			prev_issue = issue;
			prev_write = write;
			prev_fetch = fetch;
			LI_write = write;
			int[] parr10 = new int[2];
			parr10[0] = write;
			parr10[1] = val56;
			rmap.put(destinationRegister,parr10);
			//System.out.println("\t" + fetch + " " + issue + " " + read + " " + exec + " " + write);
			//System.out.print("\t" + RAW + " " + WAW + " " + Struct);
			fw.write(String.format(formatStr, line, fetch, issue, read, exec, write, RAW, WAW, Struct));		RAW = 'N';
			WAW = 'N';
			Struct = 'N';
			cache_empty = 0;
			prev_branch = 0;
			break;
		case "AND":
			operands = getOperands(tokens);
			destinationRegister = operands[0];
			sourceRegister1 = operands[1];
			sourceRegister2 = operands[2];
			break;
		case "ANDI":
			operands = getOperands(tokens);
			destinationRegister = operands[0];
			sourceRegister1 = operands[1];
			immediate = Integer.parseInt(operands[2]);
			break;
		case "OR":
			operands = getOperands(tokens);
			destinationRegister = operands[0];
			sourceRegister1 = operands[1];
			sourceRegister2 = operands[2];
			break;
		case "ORI":
			operands = getOperands(tokens);
			destinationRegister = operands[0];
			sourceRegister1 = operands[1];
			immediate = Integer.parseInt(operands[2]);
			break;
		case "BEQ":
			operands = getOperands(tokens);
			sourceRegister1 = operands[0];
			sourceRegister2 = operands[1];
			destinationRegister = operands[2];
			if(miss_flag == 0)
			{
				fetch = prev_fetch + clock;
			}
			else
			{
				fetch = prev_issue;
			}
			fetch = Math.max(fetch, prev_issue);
			issue = issue + 1;
			//Check for RAW hazard
			int val50 = rmap.get(sourceRegister1)[0];
			int val51 = rmap.get(sourceRegister2)[0];
			int val52 = rmap.get(sourceRegister1)[1];
			int val53 = rmap.get(sourceRegister2)[1];
			int read19 =0,read20 = 0, read21 = 0;
			if((issue+1)<val50)
			{
				RAW = 'Y';
				read19 = val50 + 1;
			}
			if((issue+1)<val51)
			{
				RAW = 'Y';
				read20 = val51 + 1;
			}
			read21 = Math.max(read19, read20);
			if(read21>0)
			{
				read = read21;
			}
			else
			{
				read = issue + 1;
			}
			if(val52 == val53)
			{
				first_iter = 0;
				branch = 1;
				//System.out.println("Jumping to loop");
				//System.out.println("\nvalue of i:" + "" + i);
			}
			else if(val52 != val53)
			{
				branch = 0;
			}
			BNE_read = read;
			//System.out.println(val52);
			//System.out.println(val53);
			//System.out.println("\t" + fetch + " " + issue + " " + read + "  " + "  ");
			//System.out.print("\t" + RAW + " " + WAW + " " + Struct);
			fw.write(String.format(formatStr, line, fetch, issue, read, "", "", RAW, WAW, Struct));
			RAW = 'N';
			WAW = 'N';
			Struct = 'N';
			cache_empty = 0;
			prev_fetch = fetch;
			prev_issue = issue;
			break;
		case "BNE":
			operands = getOperands(tokens);
			sourceRegister1 = operands[0];
			sourceRegister2 = operands[1];
			destinationRegister = operands[2];
			if(miss_flag == 0)
			{
				fetch = prev_fetch + clock;
			}
			else
			{
				fetch = prev_issue;
			}
			fetch = Math.max(fetch, prev_issue);
			issue = issue + 1;
			//Check for RAW hazard
			int val16 = rmap.get(sourceRegister1)[0];
			int val17 = rmap.get(sourceRegister2)[0];
			int val40 = rmap.get(sourceRegister1)[1];
			int val41 = rmap.get(sourceRegister2)[1];
			int read13 =0,read14 = 0, read15 = 0;
			if((issue+1)<val16)
			{
				RAW = 'Y';
				read13 = val16 + 1;
			}
			if((issue+1)<val17)
			{
				RAW = 'Y';
				read14 = val17 + 1;
			}
			read15 = Math.max(read13, read14);
			if(read15>0)
			{
				read = read15;
			}
			else
			{
				read = issue + 1;
			}
			if(val40 != val41)
			{
				first_iter = 0;
				branch = 1;
				//System.out.println("Jumping to loop");
				//System.out.println("\nvalue of i:" + "" + i);
			}
			else if(val40 == val41)
			{
				branch = 0;
			}
			BNE_read = read;
			//System.out.println(val40);
			//System.out.println(val41);
			//System.out.println("\t" + fetch + " " + issue + " " + read + "  " + "  ");
			//System.out.print("\t" + RAW + " " + WAW + " " + Struct);
			fw.write(String.format(formatStr, line, fetch, issue, read, "", "", RAW, WAW, Struct));	
			RAW = 'N';
			WAW = 'N';
			Struct = 'N';
			cache_empty = 0;
			prev_fetch = fetch;
			prev_issue = issue;
			break;
		case "HLT":
			if(branch == 1 && i%I_cache == 0)
			{
				i = loop_index;
				fetch = prev_fetch + clock;
				prev_issue = fetch + 1;
				//System.out.println("\t" + fetch);
				//System.out.print("\t" + RAW + " " + WAW + " " + Struct);
				fw.write(String.format(formatStr, line, fetch, "", "", "", "", RAW, WAW, Struct));
				HLT_inst = 1;
				prev_branch = 1;
				HLT_fetch = fetch;
			}
			else if(branch == 1 && i%I_cache != 0)
			{
				i = loop_index;
				fetch = prev_issue;
				prev_issue = fetch + 1;
				//System.out.println("\t" + fetch);
				//System.out.print("\t" + RAW + " " + WAW + " " + Struct);
				fw.write(String.format(formatStr, line, fetch, "", "", "", "", RAW, WAW, Struct));
				HLT_inst = 1;
				prev_branch = 1;
				HLT_fetch = fetch;
			}
			if(branch == 0 && HLT_cnt == 0)
			{
				fetch = prev_issue;
				if(ex3 == 1)
				{
					fetch = prev_fetch + 13;
				}	
				issue = BNE_read + 1;
				if(ex3 == 1)
				{
					issue = fetch + 1;
				}
				HLT_cnt = 1;
				prev_fetch = fetch;
				//System.out.println("\t" + fetch + " " + issue );
				//System.out.print("\t" + RAW + " " + WAW + " " + Struct);
				fw.write(String.format(formatStr, line, fetch, issue, "", "", "", RAW, WAW, Struct));		
			}
			else if(branch == 0 && HLT_cnt == 1)
			{
				fetch = BNE_read + 1;
				if(ex3 == 1)
				{
					fetch = prev_fetch + 1;
				}
				//System.out.println("\t" + fetch );
				//System.out.print("\t" + RAW + " " + WAW + " " + Struct);
				fw.write(String.format(formatStr, line, fetch, "", "", "", "", RAW, WAW, Struct));
			}
			branch = 0;
			break;
		case "J":
			operands = getOperands(tokens);
			destinationRegister = operands[0];
			break;
		case "LI":
			operands = getOperands(tokens);
			//String cnt = operands[0];
			destinationRegister = operands[0];
			//String cnt1 = cnt.substring(1, cnt.length());
			//int count = Integer.parseInt(cnt1);
			int value = Integer.parseInt(operands[1]);
			if(first_inst == 0)
			{
				//System.out.println("Int_unit is zero");
				fetch = clock;
				issue = fetch + 1;
				read = issue + 1;
				exec = read + 1;
				write = exec + 1;
				inst_unit = write;
				prev_issue = issue;
				prev_write = write;
				LI_write = write;
				prev_fetch = fetch;
				hmap.put(destinationRegister,value);
				int[] parr1 = new int[2];
				parr1[0] = write;
				parr1[1] = value;
				rmap.put(destinationRegister,parr1);
				int int1 = rmap.get(destinationRegister)[0];
				//System.out.println(int1);
				int int2 = rmap.get(destinationRegister)[1];
				//System.out.println(int2);
				/*for(Entry<String, Integer> entry : hmap.entrySet())
		    {   //print keys and values
		         System.out.print(entry.getKey() + " : " +entry.getValue() + " ");
		    }*/
				//System.out.println("\t" + fetch + " " + issue + " " + read + " " + exec + " " + write);
				fw.write(String.format(formatStr, line, fetch, issue, read, exec, write, RAW, WAW, Struct));			first_inst = 1;
				RAW = 'N';
				WAW = 'N';
				Struct = 'N';
			}
			else
			{
				if(miss_flag == 0)
				{
					fetch = prev_fetch + clock;
				}
				else if(first_iter == 0 && prev_branch == 1)
				{
					fetch = Math.max((BNE_read+1),(HLT_fetch+1));

				}
				else
				{
					fetch = prev_issue;
				}
				fetch = Math.max(fetch, prev_issue);
				//check for structural hazard
				if((fetch+1)<=LI_write)
				{
					Struct = 'Y';
					issue = LI_write+1;
					read =  issue + 1;
					exec = read + 1;
					write = exec + 1;
					prev_issue = issue;
					LI_write = write;
					prev_fetch = fetch;
				}
				else
				{
					issue = fetch + 1;
					read =  issue + 1;
					exec = read + 1;
					write = exec + 1;
					prev_issue = issue;
					LI_write = write;
					prev_fetch = fetch;
				}
				hmap.put(destinationRegister,value);
				int[] parr2 = new int[2];
				parr2[0] = write;
				parr2[1] = value;
				rmap.put(destinationRegister,parr2);
				int int1 = rmap.get(destinationRegister)[0];
				//System.out.println(int1);
				int int2 = rmap.get(destinationRegister)[1];
				//System.out.println(int2);
				//System.out.println("\t" + fetch + " " + issue + " " + read + " " + exec + " " + write);
				fw.write(String.format(formatStr, line, fetch, issue, read, exec, write, RAW, WAW, Struct));
				RAW = 'N';
				WAW = 'N';
				Struct = 'N';
				cache_empty = 0;
				prev_branch = 0;
				/*for(Entry<String, Integer> entry : hmap.entrySet())
		    {   //print keys and values
		         System.out.print(entry.getKey() + " : " +entry.getValue() + " ");
		    }*/
				//System.out.println("\t" + fetch + " " + issue + " " + read + " " + exec + " " + write);
			}
			break;
		default:
			throw new Exception("Illegal Instruction encountered");
		}
		loopName = (loopName != null && loopName.length() > 0) ? loopName
				+ ": " : "";

	}

	private static String[] getOperands(String[] tokens) throws Exception {

		String argListArray[] = new String[3];
		String arg1[] = new String[3];
		if (!tokens[0].trim().equalsIgnoreCase("HLT")) {
			String argList = tokens[1];
			argListArray = argList.trim().split(",");
			for (int i = 0; i < argListArray.length; i++) {
				String arg = argListArray[i] = argListArray[i].trim();
				/* VALIDATE ARG */
				if (arg.charAt(0) != 'R' && arg.charAt(0) != 'F') {
					if (arg.charAt(0) > '9')
						if (!tokens[0].equalsIgnoreCase("BEQ")
								&& !tokens[0].equalsIgnoreCase("BNE")
								&& !tokens[0].equalsIgnoreCase("J"))
							throw new Exception("Incorrect Format in inst.txt at Line");
				}
				arg1[i] = argListArray[i];
			}
		}
		return arg1;
	}

	
	private static int  Inst_cache(int index) throws Exception
	{
		int flag;
		//calculate block
		int num = index % (I_cache * I_cache1);
		int block = num/I_cache;
		//check if it is a hit
		if(q.contains(index))
		{
			//hit
			flag = 1;
			Inst_miss_count++;
		}
		else
		{
			//miss
			flag = 0;
		}
		if(flag == 0)
		{
			if(q.size() == (I_cache * I_cache1))
			{
				//cache is full
				for(int i = 0;i<I_cache;i++)
				{
				q.remove();
				}
				int st_ind;
				if(index == 0)
				{
					st_ind = index;
				}
				else if(index%I_cache == 0)
				{
					st_ind = index;
				}
				else
				{
					while(index%I_cache != 0)
					{
						index--;
					}
					st_ind = index;
				}
				for(int j = st_ind;j<(st_ind+I_cache);j++)
				{
					q.add(j);
				}
				
			}
			else
			{
				int st_ind;
				if(index == 0)
				{
					st_ind = index;
				}
				else if(index%I_cache == 0)
				{
					st_ind = index;
				}
				else
				{
					while(index%I_cache != 0)
					{
						index--;
					}
					st_ind = index;
				}
				for(int j = st_ind;j<(st_ind+I_cache);j++)
				{
					q.add(j);
				}
			}
		}
		return flag;
	}
	
	private static int  Data_cache(int index) throws Exception
	{
		data_cache_count++;
		int flag;
		//calculate block
		int num = index % (I_cache * I_cache1);
		int block = num/I_cache;
		//check if it is a hit
		if(d.contains(index))
		{
			//hit
			data_cache_hits++;
			flag = 1;
		}
		else
		{
			//miss
			flag = 0;
		}
		if(flag == 0)
		{
			if(d.size() == 16)
			{
				//cache is full
				for(int i = 0;i<4;i++)
				{
				d.remove();
				}
				//add data
				int st_ind = 0;
				if(index == 256 || index == 260 || index == 264 || index == 268)
				{
					st_ind = 256;
				}
				else if(index == 272 || index == 276 || index == 280 || index == 284)
				{
					st_ind = 272;
				}
				else if(index == 288 || index == 292 || index == 296 || index == 300)
				{
					st_ind = 288;
				}	
				else if(index == 304 || index == 308 || index == 312 || index == 316)
				{
					st_ind = 304;
				}
				else if(index == 336 || index == 340 || index == 344 || index == 348)
				{
					st_ind = 336;
				}
				else if(index == 320 || index == 324 || index == 328 || index == 332)
				{
					st_ind = 320;
				}
				
				for(int j = st_ind;j<(st_ind + 16);j+=4)
				{
					d.add(j);
				}
				
			}
			else
			{
				int st_ind = 0;
				if(index == 256 || index == 260 || index == 264 || index == 268)
				{
					st_ind = 256;
				}
				else if(index == 272 || index == 276 || index == 280 || index == 284)
				{
					st_ind = 272;
				}
				else if(index == 288 || index == 292 || index == 296 || index == 300)
				{
					st_ind = 288;
				}	
				else if(index == 304 || index == 308 || index == 312 || index == 316)
				{
					st_ind = 304;
				}
				else if(index == 336 || index == 340 || index == 344 || index == 348)
				{
					st_ind = 336;
				}
				else if(index == 320 || index == 324 || index == 328 || index == 332)
				{
					st_ind = 320;
				}
				
				for(int j = st_ind;j<(st_ind + 16);j+=4)
				{
					d.add(j);
				}
			}
		}
		return flag;
	}
}

