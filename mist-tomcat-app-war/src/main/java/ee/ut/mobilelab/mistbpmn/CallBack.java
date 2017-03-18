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

              HttpRequest.post(callBackUrl).send(execution.getVariable("log_id").toString()).message();
             LOGGER.info("Call back has been reached ");



    }
}


}