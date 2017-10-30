
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Data {
	public static int[] parse(String filepath) throws Exception
	{
		//Reading inst.txt file into linesArray[]	
		int[] intArray = new int[32];
		        try {
		            // FileReader reads text files in the default encoding.
		        	
		            FileReader fileReader = 
		                new FileReader(filepath);

		            // Always wrap FileReader in BufferedReader.
		            BufferedReader bufferedReader = 
		                new BufferedReader(fileReader);
		            String str = null;
		            ArrayList<Integer> lines = new ArrayList<Integer>();
					
		    		while((str = bufferedReader.readLine()) != null){
		    		    lines.add(Integer.parseInt(str, 2));
		    		}
		    		
		    		for(int i = 0;i<intArray.length;i++)
		    		{
		    			intArray[i] = lines.get(i).intValue();
		    		}
		    		
		            // Always close files.
		            bufferedReader.close();  
		         
		        }
		        
		        catch(FileNotFoundException ex) {
		        	ex.printStackTrace();
		            System.out.println(
		                "Unable to open file '" + 
		                filepath + "'");                
		        }
		        catch(IOException ex) {
		            System.out.println(
		                "Error reading file '" 
		                + filepath + "'");                  
		            // Or we could just do this: 
		            // ex.printStackTrace();
		        }
		        return intArray;
		       
	}
}
