package ee.ut.mobilelab.mistbpmn;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONException;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.impl.util.json.XML;

public class StartDockerContainers extends DockerCommands implements JavaDelegate  {
    private static final Logger LOGGER = Logger.getLogger("Mist tomcat app");
    static CredentialsProvider credsProvider = new BasicCredentialsProvider();
    public StartDockerContainers() {
        super();
    }

    public void execute(DelegateExecution execution) throws Exception {

        super.stopContainers();
        String mistNodeUrl = (String)execution.getVariable("mist_node_url");
        String allResourcesAvailable = (String)execution.getVariable("all_resources_available");

        if(!mistNodeUrl.equals("none")){
            File war_file= new File ( System.getProperty("user.dir")+"/mist_data/mist-0.war") ;
            File mist_file = new File ( System.getProperty("user.dir")+"/mist_data/mist_file.txt") ;
            HttpPost req = new HttpPost(mistNodeUrl) ;
            MultipartEntityBuilder meb = MultipartEntityBuilder.create();
            meb.addTextBody("fileDescription", "war file to deploy");
            //"application/octect-stream"
            meb.addBinaryBody("war", war_file, ContentType.APPLICATION_OCTET_STREAM, war_file.getName());
            meb.addBinaryBody("mist", mist_file, ContentType.APPLICATION_OCTET_STREAM, mist_file.getName());
            req.setEntity(meb.build()) ;
            String response = executeRequest (req, credsProvider);
            execution.setVariable("remote",true);
        }
        else{

            if(allResourcesAvailable.equals("true")){

                String xmlFile = System.getProperty("user.dir")+"/data/city_weather.xml";
                String line="",str="";
                BufferedReader br = new BufferedReader(new FileReader(xmlFile));
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
                            int temperature = Integer.parseInt(jsonObject.get("airtemperature").toString());
                            execution.setVariable("remote",false);
                            if(temperature<0) {
                                // result is the variable being checked in the xor to determined if the flow goes to desired or not
                                execution.setVariable("result",true);
                                // response this value is added to the system variables and can be used in other parts of the gateway
                                execution.setVariable("response",temperature);
                            } else {
                                execution.setVariable("result", false);
                            }

                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{

                String command = "docker run -p 8090:8080 akaiz/mist-docker";

                String line = " ";

                // starting container and getting its buffer response

                while((line = super.startContainer(command).readLine()) != null) {

                    if(line.contains("Tomcat started on port(s)")){
                        LOGGER.info(timestamp+" Mist-docker  started \n");
                        LOGGER.info( timestamp+"  Request made to Mist-docker from Camunda \n");
                        // making a get request that executes a given task in a container

                        HttpClient httpclient = HttpClients.createDefault();
                        HttpPost httppost = new HttpPost("http://localhost:8090/download");

// Request parameters and other properties.
                        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                        params.add(new BasicNameValuePair("pathUrl", "http://kodu.ut.ee/~isaac/city.zip"));
                        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

//Execute and get the response.
                        HttpResponse response = httpclient.execute(httppost);
                        HttpEntity entity = response.getEntity();
                        String resultTemp=null;
                        if (entity != null) {
                            InputStream instream = entity.getContent();
                            try {
                                resultTemp = IOUtils.toString(instream, "UTF-8");
                            } finally {
                                instream.close();
                            }
                        }

                        LOGGER.info( timestamp+" Camunda received results from Mist-docker \n");
                        // here is the response  returned
                        LOGGER.info(response + "\n");
                        // for testing  we are using devivceId 123  but finally we will be using the response returned from the docker
                        execution.setVariable("remote",false);
                        int temperature = Integer.parseInt(resultTemp);
                        if(temperature<0 ) {
                            // result is the variable being checked in the xor to determined if the flow goes to desired or not
                            execution.setVariable("result",true);
                            // response this value is added to the system variables and can be used in other parts of the gateway
                            execution.setVariable("response",response);
                        } else {
                            execution.setVariable("result", false);
                        }

                        break;
                    }
                }

            }

        }
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
