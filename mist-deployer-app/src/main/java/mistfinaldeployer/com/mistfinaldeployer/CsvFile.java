package mistfinaldeployer.com.mistfinaldeployer;


import com.opencsv.CSVWriter;
import de.siegmar.fastcsv.writer.CsvWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CsvFile {
      static String fileName = "/home/pi/desktop/mist-framework/mistlog.csv";
      public CsvFile(){

      }

    public  static String write(String name,String start,String end) throws IOException {
        CsvWriter csvWriter = new CsvWriter();
        Collection<String[]> data = new ArrayList<>();
        if(! new File(fileName).exists())
        {
            new File(fileName).mkdirs();
        }
        File file = new File(fileName);

        data.add(new String[] { "value1", "value2" });

       csvWriter.write(Paths.get(fileName), StandardCharsets.UTF_8, data);
       return  "done";
    }


}


