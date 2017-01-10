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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;


import java.io.*;
import java.nio.file.Paths;

import static org.springframework.web.bind.annotation.RequestMethod.GET;


@RestController
public class MistController {
    static CredentialsProvider credsProvider = new BasicCredentialsProvider();
    File currentDirectory = new File(new File(".").getAbsolutePath()+"/wars/");
    Boolean mistStarted = false;
    String startRequest="";

    @RequestMapping("/stop")
    public String stop() throws IOException {
        if(mistStarted){
           return undeploy();
        }

        return "Bpmn not started";
    }
    @RequestMapping(method = GET, path = "/xml/{town}")
    public String xml(@PathVariable String town) {
        final String uri = "http://www.ilmateenistus.ee/ilma_andmed/xml/observations.php";

        return null;
    }
    @RequestMapping(method = GET,path = "/start")
    private  void sendRequest() throws IOException {

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
    }
    @RequestMapping(value = "/deploy", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> uploadFile(@RequestParam("war") MultipartFile uploadfile   ,  @RequestParam("mist") MultipartFile mistfile) throws IOException {
        credsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials("tomcat", "tomcat"));
        try {

            String filename = uploadfile.getOriginalFilename();
            String directory = currentDirectory.getCanonicalPath();
            String filepath = Paths.get(directory, filename).toString();

            // Save the file locally
            BufferedOutputStream stream =
                    new BufferedOutputStream(new FileOutputStream(new File(filepath)));
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

            return new ResponseEntity<>(deploy(filename),HttpStatus.OK);

        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }


    }
    // deploy file to camunda

    private  String deploy(String filename) throws ClientProtocolException, IOException {
        String url = "http://localhost:8080/manager/text/deploy?path=/mistBpmn&update=true";
        // get this war generated from the maveen install of the mist-bpmn war

        File currentDirectory = new File(new File(".").getAbsolutePath());

        File file = new File (currentDirectory.getCanonicalPath()+ "/wars/"+filename) ;

        HttpPut req = new HttpPut(url) ;
        MultipartEntityBuilder meb = MultipartEntityBuilder.create();
        meb.addTextBody("fileDescription", "war file to deploy");
        //"application/octect-stream"
        meb.addBinaryBody("attachment", file, ContentType.APPLICATION_OCTET_STREAM, file.getName());

        req.setEntity(meb.build()) ;
        String response = executeRequest (req, credsProvider);

        System.out.println("Response : "+response);
        mistStarted=true;
        return  response;



    }



    public  String undeploy() throws ClientProtocolException, IOException{
        credsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials("tomcat", "tomcat"));
        String url = "http://localhost:8080/manager/text/undeploy?path=/mistBpmn";
        HttpGet req = new HttpGet(url) ;
        String response = executeRequest (req, credsProvider);
        System.out.println("Response : "+response);
        return  response;



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

}

