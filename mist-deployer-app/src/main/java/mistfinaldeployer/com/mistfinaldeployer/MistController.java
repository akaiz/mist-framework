package mistfinaldeployer.com.mistfinaldeployer;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import org.asynchttpclient.*;
import org.asynchttpclient.request.body.multipart.FilePart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;


import java.io.*;
import java.net.*;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
class Node{
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMist_files_path() {
        return mist_files_path;
    }

    public void setMist_files_path(String mist_files_path) {
        this.mist_files_path = mist_files_path;
    }

    public String getMist_file() {
        return mist_file;
    }
    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
    public String getCall_back_ip() {
        return call_back_ip;
    }

    public void setCall_back_ip(String call_back_ip) {
        this.call_back_ip = call_back_ip;
    }

    public void setMist_file(String mist_file) {
        this.mist_file = mist_file;
    }
    String url;
    String mist_files_path;
    String mist_file;
    String payload;
    String call_back_ip;
}
class Callback{
    String name;
    String start;
    String end;
    String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }


}


@RestController
public class MistController {
    CredentialsProvider credsProvider = new BasicCredentialsProvider();
    InetAddress myIP= InetAddress.getLocalHost();
    String realPathtoUploads,mistpath;

    long startTime ,endTime;
    Boolean mistStarted = false;
    String startRequest="";
    @Autowired
    private HttpServletRequest request;

    public MistController() throws UnknownHostException {
    }

    @RequestMapping("/stop")
    public String stop() throws IOException {
        if(mistStarted){
           return undeploy("");
        }

        return "Bpmn not started";
    }


    private  String sendRequest() throws IOException {

        String postText = startRequest;
        System.out.println("Post request sent with this data "+postText);

        String       postUrl       = "http://localhost:8080/engine-rest/message";// put in your url
        Gson gson          = new Gson();
        HttpClient httpClient    = HttpClientBuilder.create().build();
        HttpPost post          = new HttpPost(postUrl);
        System.out.println(postText);
        StringEntity postingString = new StringEntity(postText,"UTF-8");//gson.tojson() converts your pojo to json
        post.setEntity(postingString);
        post.setHeader("Content-type", "application/json");

        System.out.println("Request being processed .......................");
        HttpResponse  response2 = httpClient.execute(post);

        System.out.println(response2+" request execution finished   ");
        return response2.toString();
    }

    @RequestMapping(value = "/deploy", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> uploadFile(@RequestParam("war") MultipartFile uploadfile   ,  @RequestParam("mist") MultipartFile mistfile) throws IOException {
        credsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials("tomcat", "tomcat"));
        try {

            if(!uploadfile.isEmpty() && !mistfile.isEmpty()){


                String uploadsDir = "/uploads/";
                realPathtoUploads =  request.getServletContext().getRealPath(uploadsDir);
                if(! new File(realPathtoUploads).exists())
                {
                    new File(realPathtoUploads).mkdirs();
                }

                String filename = uploadfile.getOriginalFilename();
                String directory = realPathtoUploads;
                mistpath = Paths.get(directory, filename).toString();

                // Save the file locally
                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(new File(mistpath)));
                stream.write(uploadfile.getBytes());
                stream.close();

//        Getting data from the mist file

                String mistFilename = mistfile.getOriginalFilename();

                String filepath2 = Paths.get(directory, mistFilename).toString();

                stream = new BufferedOutputStream(new FileOutputStream(new File(filepath2)));
                stream.write(mistfile.getBytes());
                stream.close();



                BufferedReader br = new BufferedReader(new FileReader(filepath2));
                try {
                    StringBuilder sb = new StringBuilder();
                    String line = br.readLine();

                    while (line != null) {
                        sb.append(line);
                        sb.append("\n");
                        line = br.readLine();
                    }
                    startRequest=sb.toString();
                } finally {
                    br.close();
                }
                // deploying to tomcat and  start


                return new ResponseEntity<>(deploy(),HttpStatus.OK);
            }

            return new ResponseEntity<>("PLEASE PROVIDE CORRECT INPUT",HttpStatus.OK);



        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }


    }
    // deploy file to camunda

    private  String deploy() throws ClientProtocolException, IOException {
        credsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials("tomcat", "tomcat"));
         if(mistpath!=null){
            String url = "http://localhost:8080/manager/text/deploy?path=/mistBpmn&update=true";
            // get this war generated from the maveen install of the mist-bpmn war
            File file = new File (mistpath) ;
            HttpPut req = new HttpPut(url) ;
            MultipartEntityBuilder meb = MultipartEntityBuilder.create();
            meb.addTextBody("fileDescription", "war file to deploy");
            //"application/octect-stream"
            meb.addBinaryBody("attachment", file, ContentType.APPLICATION_OCTET_STREAM, file.getName());
            req.setEntity(meb.build()) ;
            String response = executeRequest (req, credsProvider);

            System.out.println("Response after depoly  : "+response);

            return response;

        }
        else {
            return "Are you sure you uplload the mist war";
        }

    }
    @RequestMapping(value = "/deploynode",method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> uploadFilenew(@RequestParam("war") MultipartFile uploadfile ,  @RequestParam("mist") MultipartFile mistfile,
                                           @RequestParam(name = "payload", required=false) MultipartFile payload,
                                           @RequestParam("callback") String callback,@RequestParam("processId") String processId) throws IOException {
        credsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials("tomcat", "tomcat"));

        try {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            CsvFile.write(processId,"Recieved deployment ");

            if(!uploadfile.isEmpty() && !mistfile.isEmpty()){


                String uploadsDir = "/uploads/";
                realPathtoUploads =  request.getServletContext().getRealPath(uploadsDir);
                if(! new File(realPathtoUploads).exists())
                {
                    new File(realPathtoUploads).mkdirs();
                }
                else{
                    // delete folder if exists
                    delete(new File(realPathtoUploads));
                    // recreate it again
                    new File(realPathtoUploads).mkdirs();

                }
                // if payload was sent
                System.out.println("Payload ---------------- sent"+payload);
                if(payload!=null){
                    System.out.println("Payload ---------------- sent");
                    String directory = "/home/pi/mist/";
                    delete(new File(directory));
                    // recreate it again
                    new File(directory).mkdirs();
                    String upload_path = Paths.get(directory, "mist_img.jpg").toString();

                    // Save the PAYLOAD file locally
                    BufferedOutputStream stream =
                            new BufferedOutputStream(new FileOutputStream(new File(upload_path)));
                    stream.write(uploadfile.getBytes());
                    stream.close();

                }
                String filename = uploadfile.getOriginalFilename();
                String directory = realPathtoUploads;
                mistpath = Paths.get(directory, filename).toString();

                // Save the file locally
                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(new File(mistpath)));
                stream.write(uploadfile.getBytes());
                stream.close();

//        Getting data from the mist file

                String mistFilename = mistfile.getOriginalFilename();

                String filepath2 = Paths.get(directory, mistFilename).toString();

                stream = new BufferedOutputStream(new FileOutputStream(new File(filepath2)));
                stream.write(mistfile.getBytes());
                stream.close();



                BufferedReader br = new BufferedReader(new FileReader(filepath2));
                try {
                    StringBuilder sb = new StringBuilder();
                    String line = br.readLine();

                    while (line != null) {
                        sb.append(line);
                        sb.append("\n");
                        line = br.readLine();
                    }
                    startRequest=sb.toString();
                } finally {
                    br.close();
                }
                // deploying to tomcat and  start

                return new ResponseEntity<>(deployToCamunda(processId),HttpStatus.OK);
            }

            return new ResponseEntity<>("PLEASE PROVIDE CORRECT INPUT",HttpStatus.OK);



        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    // deploy file to camunda

    private  String deployToCamunda(String processId) throws ClientProtocolException, IOException {
        if(mistpath!=null){
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            String url = "http://localhost:8080/manager/text/deploy?path=/mistBpmn&update=true";
            // get this war generated from the maveen install of the mist-bpmn war
            CsvFile.write(processId,"Started deployment to Camunda");
            File file = new File (mistpath) ;
            HttpPut req = new HttpPut(url) ;
            MultipartEntityBuilder meb = MultipartEntityBuilder.create();
            meb.addTextBody("fileDescription", "war file to deploy");
            //"application/octect-stream"
            meb.addBinaryBody("attachment", file, ContentType.APPLICATION_OCTET_STREAM, file.getName());
            req.setEntity(meb.build()) ;
            String response = executeRequest (req, credsProvider);

            System.out.println("Response after depoly  : "+response);
            CsvFile.write(processId,"Finished deployment to Camunda");
            // Starting the  depoloyed machine
            String postText = startRequest;
            System.out.println("Post request sent with this data "+postText);

            String       postUrl       = "http://localhost:8080/engine-rest/message";// put in your url
            Gson gson          = new Gson();
            HttpClient httpClient    = HttpClientBuilder.create().build();
            HttpPost post          = new HttpPost(postUrl);
            System.out.println(postText);
            StringEntity postingString = new StringEntity(postText,"UTF-8");//gson.tojson() converts your pojo to json
            post.setEntity(postingString);
            post.setHeader("Content-type", "application/json");

            System.out.println("Request being processed .......................");
            HttpResponse  response2 = httpClient.execute(post);
            try {
                undeploy(processId);
            } catch (IOException e) {
                e.printStackTrace();
            }
                  return  response2.toString();

        }
         else {
            return "Are you sure you uplload the mist war";
        }


    }

    @RequestMapping(value = "deploy/node", method = RequestMethod.POST)
    public String deployToNode(@RequestBody Node node) throws ClientProtocolException, IOException{
        if (node.url != null) {
            String processId = randomString(5)+","+node.getMist_file();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String mistFilesPath =node.mist_files_path;
            if(! new File(mistFilesPath).exists())
            {
                return  "Missing mist files at "+mistFilesPath;
            }

            String tempFile = mistFilesPath+"mis_temp.txt";

            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(tempFile)));
            BufferedWriter bw = null;
            FileWriter fw = null;
            fw = new FileWriter(tempFile);
            bw = new BufferedWriter(fw);


            BufferedReader br = new BufferedReader(new FileReader(mistFilesPath+node.getMist_file()));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                String newLine = System.getProperty("line.separator");

                while (line != null) {
                    if(line.contains("call_back_url")){
                        line ="\"processVariables\" : {\"call_back_url\" : {\"value\" : \"http://"+node.getCall_back_ip()+":8098/callback\",\"type\": \"String\"},";
                    }
                    if(line.contains("log_id")){
                        line =" \"log_id\":{\"value\" :\""+processId+"\",\"type\": \"String\"}";
                    }
                    // Always write the line, whether you changed it or not.
                    bw.write(line+newLine);

                    line = br.readLine();
                }
                startRequest=sb.toString();

            } finally {
                br.close();
            }
            try {

                if (bw != null)
                    bw.close();

                if (fw != null)
                    fw.close();
                File realName = new File(mistFilesPath+node.getMist_file());
                realName.delete(); // remove the old file
                new File(tempFile).renameTo(realName); // Rename temp file


            } catch (IOException ex) {

                ex.printStackTrace();

            }
            CsvFile.write(processId,"Process Start");
            File war = new File(mistFilesPath+"mist-0.war");
            File mist_file = new File(mistFilesPath+node.getMist_file());
            HttpPost req = new HttpPost(node.url);
            MultipartEntityBuilder meb = MultipartEntityBuilder.create();
            meb.addTextBody("callback", "http://"+node.getCall_back_ip()+"/callback");
            meb.addTextBody("processId",processId);
            System.out.println("payload ------->"+node.getPayload());
            if(node.getPayload().equals("true")){
                File mist_payload = new File(mistFilesPath+(node.getMist_file().contains("0")?"payload-light.jpg":"payload-heavy.jpeg"));
                System.out.println("payload ii ------->"+mist_payload.getAbsolutePath());
                meb.addBinaryBody("payload", mist_payload, ContentType.APPLICATION_OCTET_STREAM, mist_payload.getName());
            }

            meb.addBinaryBody("war", war, ContentType.APPLICATION_OCTET_STREAM, war.getName());
            meb.addBinaryBody("mist", mist_file, ContentType.APPLICATION_OCTET_STREAM, mist_file.getName());

            req.setEntity(meb.build());
            String response = executeRequest(req, credsProvider);
            CsvFile.write(processId,"Process End");
            return "Success Mist Tasks completed";

        }
        return "Sorry empty url supplied";
    }

        public  String undeploy(String processId) throws ClientProtocolException, IOException{
        credsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials("tomcat", "tomcat"));
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
          //  CsvFile.write(processId,"Started un deployment to Camunda");
        String url = "http://localhost:8080/manager/text/undeploy?path=/mistBpmn";
        HttpGet req = new HttpGet(url) ;
        String response = executeRequest (req, credsProvider);
        System.out.println("Response : "+response);
       // CsvFile.write(processId,"Finished un deployment to Camunda");
        return  response;
    }
    @RequestMapping(value = "/callback", method = RequestMethod.POST)
    @ResponseBody
    public String callbackAllFinshed(@RequestBody String response  ) throws IOException {
      String x = response.replace("response","");
      x=x.replaceAll("=","");
      x=x.replace("%2C",",");
        CsvFile.write(x,"Call back recieved");
      return  "received";

    }
    @RequestMapping(value = "/callback/time", method = RequestMethod.POST)
    @ResponseBody

    public String callbackTime(@RequestBody  Callback callback  ) throws IOException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String time= timestamp.getTime()+"";

    CsvFile.write(callback.getId(),callback.getName());
        return "recieved";

    }

    private String executeRequest(HttpRequestBase requestBase, CredentialsProvider credsProvider) throws ClientProtocolException, IOException {
        CloseableHttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        InputStream responseStream = null;
        String res = null;
        HttpResponse response = client.execute(requestBase) ;
        HttpEntity responseEntity = response.getEntity() ;
        responseStream = responseEntity.getContent() ;

        BufferedReader br = new BufferedReader (new InputStreamReader (responseStream)) ;
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append(System.getProperty("line.separator"));
        }
        br.close() ;
        res = sb.toString();

        return res;
    }
    public  void delete(File file)
            throws IOException{

        if(file.isDirectory()){

            //directory is empty, then delete it
            if(file.list().length==0){

                file.delete();
                System.out.println("Directory is deleted : "
                        + file.getAbsolutePath());

            }else{

                //list all the directory contents
                String files[] = file.list();

                for (String temp : files) {
                    //construct the file structure
                    File fileDelete = new File(file, temp);

                    //recursive delete
                    delete(fileDelete);
                }

                //check the directory again, if empty then delete it
                if(file.list().length==0){
                    file.delete();
                    System.out.println("Directory is deleted : "
                            + file.getAbsolutePath());
                }
            }

        }else{
            //if file, then delete it
            file.delete();
            System.out.println("File is deleted : " + file.getAbsolutePath());
        }

    }

    public static String randomString(int length){
        String alphabet =
                new String("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"); //9
        int n = alphabet.length(); //10

        String result = new String();
        Random r = new Random(); //11

        for (int i=0; i<length; i++) //12
            result = result + alphabet.charAt(r.nextInt(n)); //13

        return result;
    }
    public static String getIpAddress() throws IOException {
        String ip=null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    // *EDIT*
                    if (addr instanceof Inet6Address) continue;

                    ip = addr.getHostAddress();
                    System.out.println(iface.getDisplayName() + " " + ip);
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        return ip;
    }

}

