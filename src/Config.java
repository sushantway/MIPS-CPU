import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class Config {
	public static int[] parse(String filepath) throws Exception
	{
		//Reading Config.txt file
        String token = null;
        String line = null;
        int tokenArray[] = new int[8];
        try {
            // FileReader reads text files in the default encoding.
            int tok = 0;
            FileReader fileReader1 = 
                    new FileReader(filepath);

                // Always wrap FileReader in BufferedReader.
                BufferedReader bufferedReader1 = 
                    new BufferedReader(fileReader1);

                while((line = bufferedReader1.readLine()) != null) {
                    //System.out.println(line);
                    StringTokenizer st1 = new StringTokenizer(line);
                    while (st1.hasMoreTokens()) {
                    	token = st1.nextToken();
                    	tok++;
                    	token = token.replaceAll(",", "");
                    	if(tok == 3)
                    		tokenArray[0] = Integer.parseInt(token);
                    	else if(tok == 4)
                    		tokenArray[1] = Integer.parseInt(token);
                    	else if(tok == 7)
                    		tokenArray[2] = Integer.parseInt(token);
                    	else if(tok == 8)
                    		tokenArray[3] = Integer.parseInt(token);
                    	else if(tok == 11)
                    		tokenArray[4] = Integer.parseInt(token);
                    	else if(tok == 12)
                    		tokenArray[5] = Integer.parseInt(token);
                    	else if(tok == 14)
                    		tokenArray[6] = Integer.parseInt(token);
                    	else if(tok == 15)
                    		tokenArray[7] = Integer.parseInt(token);
                    }
        }
                
                bufferedReader1.close();     
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
        }
        return tokenArray;
	}
}
