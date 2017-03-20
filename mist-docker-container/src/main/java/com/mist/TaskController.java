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
import org.springframework.web.context.request.WebRequest;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
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
    @RequestMapping(method = GET, path = "/image",params = {"task","imagePath"})
    public String imageProcess(@RequestParam(value = "imagePath") String imagePath) throws IOException, URISyntaxException {

        File file = new File(imagePath );

        ImageInputStream is = ImageIO.createImageInputStream(file);
        Iterator iter = ImageIO.getImageReaders(is);

        if (!iter.hasNext())
        {
            System.out.println("Cannot load the specified file "+ file);
            System.exit(1);
        }
        ImageReader imageReader = (ImageReader)iter.next();
        imageReader.setInput(is);

        BufferedImage image = imageReader.read(0);



        int height = image.getHeight();
        int width = image.getWidth();

       String colourHex =null;
       if(width<100){
           colourHex = getMostCommonColour(iterate(height,width,image));
           colourHex = getMostCommonColour(iterate(height,width,image));

       }
       else if(width<200){
           colourHex = getMostCommonColour(iterate(height,width,image));
           colourHex = getMostCommonColour(iterate(height,width,image));
           colourHex = getMostCommonColour(iterate(height,width,image));

       }
       else if(width<300){
           colourHex = getMostCommonColour(iterate(height,width,image));
           colourHex = getMostCommonColour(iterate(height,width,image));
           colourHex = getMostCommonColour(iterate(height,width,image));
           colourHex = getMostCommonColour(iterate(height,width,image));
                 }





        return  colourHex;
    }
    public Map iterate(int height,int width,BufferedImage image){
        Map m = new HashMap();
        for(int i=0; i < width ; i++)
        {
            for(int j=0; j < height ; j++)
            {
                int rgb = image.getRGB(i, j);
                int[] rgbArr = getRGBArr(rgb);
                // Filter out grays....
                if (!isGray(rgbArr)) {
                    Integer counter = (Integer) m.get(rgb);
                    if (counter == null)
                        counter = 0;
                    counter++;
                    m.put(rgb, counter);
                }
            }
        }
        return m;
    }
    @RequestMapping(method = GET,path = "/download",params = {"pathUrl"})
    @ResponseBody
    public String downloadImage(@RequestParam(value = "pathUrl") String pathUrl) throws InterruptedException, IOException, JSONException {

        ClassLoader loader = TaskController.class.getClassLoader();

        String workingDir =loader.getResource("").getPath()+"/uploads";
        System.out.println("Current working directory : " + workingDir);
        File f = new File(workingDir);
        if(f.isDirectory() && f.exists()){
            try {
                //delete and create
                FileUtils.deleteDirectory(new File(workingDir));
                f.mkdirs();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            f.mkdirs();
        }

        Random ran = new Random();
        int top = 3;
        char data = ' ';
        String dat = "";

        for (int i=0; i<=top; i++) {
            data = (char)(ran.nextInt(25)+97);
            dat = data + dat;
        }

        String downloadedImage =loader.getResource("uploads").getPath()+"/image_"+dat+".png";
        downloadUsingNIO(pathUrl,downloadedImage);

        File file = new File(downloadedImage);
        ImageInputStream is = ImageIO.createImageInputStream(file);
        Iterator iter = ImageIO.getImageReaders(is);

        if (!iter.hasNext())
        {
            System.out.println("Cannot load the specified file "+ file);
            System.exit(1);
        }
        ImageReader imageReader = (ImageReader)iter.next();
        imageReader.setInput(is);

        BufferedImage image = imageReader.read(0);



        int height = image.getHeight();
        int width = image.getWidth();

        Map m = new HashMap();
        for(int i=0; i < width ; i++)
        {
            for(int j=0; j < height ; j++)
            {
                int rgb = image.getRGB(i, j);
                int[] rgbArr = getRGBArr(rgb);
                // Filter out grays....
                if (!isGray(rgbArr)) {
                    Integer counter = (Integer) m.get(rgb);
                    if (counter == null)
                        counter = 0;
                    counter++;
                    m.put(rgb, counter);
                }
            }
        }
        String colourHex = getMostCommonColour(m);
        System.out.println(colourHex);





        return  colourHex;
    }

    @RequestMapping(method = GET,path = "/test")
    @ResponseBody
    public String downloadZip() throws InterruptedException, IOException, JSONException {


     return  System.getProperty("user.home");
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


    public static String getMostCommonColour(Map map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });
        Map.Entry me = (Map.Entry )list.get(list.size()-1);
        int[] rgb= getRGBArr((Integer)me.getKey());
        return Integer.toHexString(rgb[0])+""+Integer.toHexString(rgb[1])+""+Integer.toHexString(rgb[2]);
    }

    public static int[] getRGBArr(int pixel) {
        int alpha = (pixel >> 24) & 0xff;
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
        return new int[]{red,green,blue};

    }
    public static boolean isGray(int[] rgbArr) {
        int rgDiff = rgbArr[0] - rgbArr[1];
        int rbDiff = rgbArr[0] - rgbArr[2];
        // Filter out black, white and grays...... (tolerance within 10 pixels)
        int tolerance = 10;
        if (rgDiff > tolerance || rgDiff < -tolerance)
            if (rbDiff > tolerance || rbDiff < -tolerance) {
                return false;
            }
        return true;
    }
}




