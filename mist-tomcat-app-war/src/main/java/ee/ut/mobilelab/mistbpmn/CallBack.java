package ee.ut.mobilelab.mistbpmn;


import com.github.kevinsawicki.http.HttpRequest;
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
            CsvFile.write(execution.getVariable("log_id").toString(),"Sending response to callback");

            HttpClient httpClient    = HttpClientBuilder.create().build();
            HttpPost post          = new HttpPost(callBackUrl);

            StringEntity postingString = new StringEntity(execution.getVariable("log_id").toString(),"UTF-8");//gson.tojson() converts your pojo to json
            post.setEntity(postingString);
            post.setHeader("Content-type", "application/json");
            HttpResponse  response = httpClient.execute(post);
            LOGGER.info("Response from the call back"+response);


    }
}


}