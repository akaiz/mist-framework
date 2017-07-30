package mistfinaldeployer.com.mistfinaldeployer;

import java.io.*;
import java.sql.Timestamp;

public class CsvFile {
    static String fileNamePath = "/mist-framework/mist-deployer-app/mistlog.csv";

    public CsvFile(){

    }

    public  static String write(String id,String name,String baseFolder) throws IOException {
        String fileName= baseFolder+fileNamePath;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println(fileName);

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
