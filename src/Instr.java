
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Instr {

	public static String[] parse(String filepath) throws Exception
	{
		//Reading inst.txt file into linesArray[]	
		String[] linesArray = null;
		        try {
		            // FileReader reads text files in the default encoding.
		        	
		            FileReader fileReader = 
		                new FileReader(filepath);

		            // Always wrap FileReader in BufferedReader.
		            BufferedReader bufferedReader = 
		                new BufferedReader(fileReader);
		            String str = null;
		            ArrayList<String> lines = new ArrayList<String>();
					
		    		while((str = bufferedReader.readLine()) != null){
		    		    lines.add(str);
		    		}
		    		
		    		linesArray = lines.toArray(new String[lines.size()]);
		    		
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
		        return linesArray;
		       
	}
}
