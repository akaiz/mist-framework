package mistfinaldeployer.com.mistfinaldeployer;

import java.io.*;

public class CsvFile {
      static String fileName = "/home/pi/Desktop/mist-framework/mist-deployer-app/mistlog.csv";

      public CsvFile(){

      }

    public  static String write(String id,String name,String time) throws IOException {

        String newLine = System.getProperty("line.separator");

        PrintWriter printWriter = null;
        File file = new File(fileName);
        try {
            if(!file.exists()){
                file.createNewFile();
                printWriter = new PrintWriter(new FileOutputStream(fileName, true));
                printWriter.write( "id,title,start,end");
            }

            printWriter = new PrintWriter(new FileOutputStream(fileName, true));
            printWriter.write( newLine+id+","+name+","+time);
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } finally {
            if (printWriter != null) {
                printWriter.flush();
                printWriter.close();
            }
        }
        return  "xs";
    }
}





