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
import java.nio.file.Files;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
    private static final String OUTPUT_ZIP_FILE = "/Users/agabaisaac/Desktop/Desktop.zip";
    private static final String SOURCE_FOLDER = "/Users/agabaisaac/work";
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
    public String x(@RequestParam("pathUrl") String pathUrl) throws InterruptedException {
       // fileList = new ArrayList<String>();
//        generateFileList(new File(SOURCE_FOLDER));
  //      zipIt(OUTPUT_ZIP_FILE);

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

        for (int i=0;i<3;i++){
         downloadTask(pathUrl, workingDir);
        }


     return  null;
    }

    private void downloadTask(String pathUrl, String workingDir) {
        Random ran = new Random();
        int top = 3;
        char data = ' ';
        String dat = "";

        for (int i=0; i<=top; i++) {
            data = (char)(ran.nextInt(25)+97);
            dat = data + dat;
        }
        String realPathtoUploads =workingDir+ "/"+dat;


        if(! new File(realPathtoUploads).exists())
        {
            new File(realPathtoUploads).mkdirs();
        }

        try {
            String url = HttpDownloadUtility.downloadFile(pathUrl, realPathtoUploads);
            System.out.println(url);
         unZipIt(url,realPathtoUploads);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public void unZipIt(String zipFile, String outputFolder){

        byte[] buffer = new byte[1024];

        try{

            //create output directory is not exists
            File folder = new File(outputFolder);
            if(!folder.exists()){
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis =
                    new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while(ze!=null){

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                System.out.println("file unzip : "+ newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            System.out.println("Done folder "+outputFolder);

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }








    public void zipIt(String zipFile){

        byte[] buffer = new byte[1024];

        try{

            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            System.out.println("Output to Zip : " + zipFile);

            for(String file : this.fileList){

                System.out.println("File Added : " + file);
                ZipEntry ze= new ZipEntry(file);
                zos.putNextEntry(ze);

                FileInputStream in =
                        new FileInputStream(SOURCE_FOLDER + File.separator + file);

                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                in.close();
            }

            zos.closeEntry();
            //remember close it
            zos.close();

            System.out.println("Done");
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    /**
     * Traverse a directory and get all files,
     * and add the file into fileList
     * @param node file or directory
     */
    public void generateFileList(File node){

        //add file only
        if(node.isFile()){
            fileList.add(generateZipEntry(node.getAbsoluteFile().toString()));
        }

        if(node.isDirectory()){
            String[] subNote = node.list();
            for(String filename : subNote){
                generateFileList(new File(node, filename));
            }
        }

    }

    /**
     * Format the file path for zip
     * @param file file path
     * @return Formatted file path
     */
    private String generateZipEntry(String file){
        return file.substring(SOURCE_FOLDER.length()+1, file.length());
    }

    public static void saveFileFromUrlWithJavaIO(String fileName, String fileUrl)
            throws MalformedURLException, IOException {
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
            in = new BufferedInputStream(new URL(fileUrl).openStream());
            fout = new FileOutputStream(fileName);

            byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
        } finally {
            if (in != null)
                in.close();
            if (fout != null)
                fout.close();
        }
    }

}



