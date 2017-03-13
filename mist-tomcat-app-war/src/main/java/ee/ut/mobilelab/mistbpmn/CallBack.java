package ee.ut.mobilelab.mistbpmn;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.sql.Timestamp;
import java.util.logging.Logger;



public class CallBack extends DockerCommands implements JavaDelegate {
    private static final Logger LOGGER = Logger.getLogger("CallBack");
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    public CallBack() {
        super();
    }

    public void execute(DelegateExecution execution) throws Exception {
        String callBackUrl = (String)execution.getVariable("call_back_url");
        if(callBackUrl!=null){
            LOGGER.info("Sending response to callback "+callBackUrl);


            HttpClient httpClient    = HttpClientBuilder.create().build();
            HttpPost post          = new HttpPost(callBackUrl+"/callback/time");
            String data ="{\"name\": \"name\",\n" +
                    "\"id\":\"233\",\n" +
                    "\"start\":"+timestamp.getTime()+"\"\"\n" +
                    "}";
            StringEntity postingString = new StringEntity("task finshed","UTF-8");
            post.setEntity(postingString);
            post.setHeader("Content-type", "application/json");
            HttpResponse response2 = httpClient.execute(post);
            LOGGER.info("Response from the call back"+response2);


    }
}}