package com.mist;

import com.github.kevinsawicki.http.HttpRequest;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.json.JSONArray;
import org.leibnizcenter.xml.TerseJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by agabaisaac on 12/28/16.
 */
@RestController
public class TaskController {
    private static final TerseJson.WhitespaceBehaviour COMPACT_WHITE_SPACE = TerseJson.WhitespaceBehaviour.Compact;
    List<String> fileList;
    private  String choosenCityWeatherPath;

    @Autowired
    private HttpServletRequest request;
    @Autowired
    ServletContext context;
    @RequestMapping("/")
    public String home() {
        return "Client is Ready ";
    }
    @RequestMapping(method = GET, path = "/xml/{town}")
    public String xml(@PathVariable String town) {
        final String uri = "http://www.ilmateenistus.ee/ilma_andmed/xml/observations.php";

        String result= HttpRequest.get(uri).body();

          JSONObject xmlJSONObj = null;
        try {
            xmlJSONObj = XML.toJSONObject(result);
            String jsonPrettyPrintString = xmlJSONObj.toString(11);

            JSONObject observations=xmlJSONObj.getJSONObject("observations");
            JSONArray jsonArray = observations.getJSONArray("station");

           for (int i=0;i<jsonArray.length();i++){
               JSONObject jsonObject = jsonArray.getJSONObject(i);
               if(jsonObject.get("name").equals(town)){
                   return jsonObject.get("airtemperature").toString();

               }
           }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @RequestMapping(method = POST,path = "/download")
    @ResponseBody
    public String x(@RequestParam("pathUrl") String pathUrl) throws InterruptedException, IOException, JSONException {

        String workingDir = System.getProperty("user.dir")+"/uploads";
        System.out.println("Current working directory : " + workingDir);

        File f = new File(workingDir);
        if(f.isDirectory() && f.exists()){
            try {
                FileUtils.deleteDirectory(new File(workingDir));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
			else{
            System.out.println("Directory is empty");
        }

        for (int x=0;x<3;x++){

            Random ran = new Random();
            int top = 3;
            char data = ' ';
            String dat = "";

            for (int i=0; i<=top; i++) {
                data = (char)(ran.nextInt(25)+97);
                dat = data + dat;
            }
            String uploadLocation = workingDir+ "/"+dat;
            File folder = new File(uploadLocation);
            if(!folder.exists()){
                folder.mkdirs();
            }

            String downloadedZip =workingDir+ "/"+dat+"/city.zip";
            downloadUsingNIO(pathUrl,downloadedZip);
            unzipFunction(uploadLocation,downloadedZip);

            if(x==2){
                choosenCityWeatherPath=uploadLocation+"/city_weather.xml";
            }


        }

        String result= choosenCityWeatherPath;
        String line="",str="";
        BufferedReader br = new BufferedReader(new FileReader(result));
        while ((line = br.readLine()) != null)
        {
            str+=line;
        }
        JSONObject xmlJSONObj = XML.toJSONObject(str);

        try {

            String jsonPrettyPrintString = xmlJSONObj.toString(11);

            JSONObject observations=xmlJSONObj.getJSONObject("observations");
            JSONArray jsonArray = observations.getJSONArray("station");

            for (int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if(jsonObject.get("name").equals("Tallinn-Harku")){
                    return jsonObject.get("airtemperature").toString();

                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }




     return  null;
    }

    private static void unzipFunction(String destinationFolder, String zipFile) {
        File directory = new File(destinationFolder);

        // if the output directory doesn't exist, create it
        if(!directory.exists())
            directory.mkdirs();

        // buffer for read and write data to file
        byte[] buffer = new byte[2048];

        try {
            FileInputStream fInput = new FileInputStream(zipFile);
            ZipInputStream zipInput = new ZipInputStream(fInput);

            ZipEntry entry = zipInput.getNextEntry();

            while(entry != null){
                String entryName = entry.getName();
                File file = new File(destinationFolder + File.separator + entryName);

                System.out.println("Unzip file " + entryName + " to " + file.getAbsolutePath());

                // create the directories of the zip directory
                if(entry.isDirectory()) {
                    File newDir = new File(file.getAbsolutePath());
                    if(!newDir.exists()) {
                        boolean success = newDir.mkdirs();
                        if(success == false) {
                            System.out.println("Problem creating Folder");
                        }
                    }
                }
                else {
                    FileOutputStream fOutput = new FileOutputStream(file);
                    int count = 0;
                    while ((count = zipInput.read(buffer)) > 0) {
                        // write 'count' bytes to the file output stream
                        fOutput.write(buffer, 0, count);
                    }
                    fOutput.close();
                }
                // close ZipEntry and take the next one
                zipInput.closeEntry();
                entry = zipInput.getNextEntry();
            }

            // close the last ZipEntry
            zipInput.closeEntry();

            zipInput.close();
            fInput.close();

            File zip = new File(zipFile);
            if(zip.exists()){
                zip.delete();
            }

            File temp = new File(destinationFolder+"/__MACOSX");
            if(temp.exists()){
                temp.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private  Path download(String sourceURL, String targetDirectory) throws IOException
    {
        URL url = new URL(sourceURL);
        String fileName = sourceURL.substring(sourceURL.lastIndexOf('/') + 1, sourceURL.length());
        Path targetPath = new File(targetDirectory + File.separator + fileName).toPath();
        Files.copy(url.openStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath;
    }
    private  void downloadUsingNIO(String urlStr, String file) throws IOException {
        URL url = new URL(urlStr);
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }

}



