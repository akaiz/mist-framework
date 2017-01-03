package ee.ut.mist.deployer;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
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

import java.io.*;
import java.util.concurrent.TimeUnit;

public class DeployManager{

    static CredentialsProvider credsProvider = new BasicCredentialsProvider();;

    public static void main(String args[]) throws ClientProtocolException, IOException, InterruptedException {
        /*
         * warning only ever AuthScope.ANY while debugging
         * with these settings the tomcat username and pw are added to EVERY request
         */
        credsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials("tomcat", "tomcat"));
        System.out.println(args[0]);

        if(args[0].equals("deploy")){
            deploy();
        }
        else if (args[0].equals("undeploy")){
            undeploy();
        }
        else if (args[0].equals("start")){
            // making a post request with the data that is in the post_data.txt file
            sendRequest();
        }
    }



    private static void deploy() throws ClientProtocolException, IOException {
        String url = "http://localhost:8080/manager/text/deploy?path=/mistBpmn&update=true";
        // get this war generated from the maveen install of the mist-bpmn war

        File currentDirectory = new File(new File(".").getAbsolutePath());

        File file = new File (currentDirectory.getCanonicalPath()+ "/mist-2.1.war") ;

        HttpPut req = new HttpPut(url) ;
        MultipartEntityBuilder meb = MultipartEntityBuilder.create();
        meb.addTextBody("fileDescription", "war file to deploy");
        //"application/octect-stream"
        meb.addBinaryBody("attachment", file, ContentType.APPLICATION_OCTET_STREAM, file.getName());

        req.setEntity(meb.build()) ;
        String response = executeRequest (req, credsProvider);

        System.out.println("Response : "+response);



    }



    public static void undeploy() throws ClientProtocolException, IOException{
        String url = "http://localhost:8080/manager/text/undeploy?path=/mistBpmn";
        HttpGet req = new HttpGet(url) ;
        String response = executeRequest (req, credsProvider);
        System.out.println("Response : "+response);



    }

    private static String executeRequest(HttpRequestBase requestBase, CredentialsProvider credsProvider) throws ClientProtocolException, IOException {
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
    private static void sendRequest() throws IOException {
        File currentDirectory = new File(new File(".").getAbsolutePath());
        InputStream is = new FileInputStream(currentDirectory.getCanonicalPath()+ "/post_data.txt");
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));
        String line = buf.readLine(); StringBuilder sb = new StringBuilder();
        while(line != null){ sb.append(line).append("\n"); line = buf.readLine(); }
        String postText = sb.toString();
        System.out.println("Post request sent with this data "+postText);

        String       postUrl       = "http://localhost:8080/engine-rest/message";// put in your url
        Gson         gson          = new Gson();
        HttpClient   httpClient    = HttpClientBuilder.create().build();
        HttpPost     post          = new HttpPost(postUrl);
        StringEntity postingString = new StringEntity(postText,"UTF-8");//gson.tojson() converts your pojo to json
        post.setEntity(postingString);
        post.setHeader("Content-type", "application/json");

        System.out.println("Request being processed .......................");
        HttpResponse  response2 = httpClient.execute(post);

        System.out.println(response2+" request execution finished   ");
    }
}