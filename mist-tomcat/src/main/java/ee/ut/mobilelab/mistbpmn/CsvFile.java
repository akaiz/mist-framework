package ee.ut.mobilelab.mistbpmn;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;

public class CsvFile {
      static String fileName = "/mist-framework/mist-deployer-app/mistlog.csv";

      public CsvFile(){

      }

    public  static String write(String id,String name, String baseFolder) throws IOException {
        fileName= baseFolder+fileName;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        String newLine = System.getProperty("line.separator");

        PrintWriter printWriter = null;
        File file = new File(fileName);
        try {
            if(!file.exists()){
                file.createNewFile();
                printWriter = new PrintWriter(new FileOutputStream(fileName, true));
                printWriter.write( "id,type,title,time");
            }

            printWriter = new PrintWriter(new FileOutputStream(fileName, true));
            printWriter.write( newLine+id+","+name+","+timestamp.getTime()+"");
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





