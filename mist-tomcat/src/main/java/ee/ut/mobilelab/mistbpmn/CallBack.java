package ee.ut.mobilelab.mistbpmn;

import com.pusher.rest.Pusher;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.logging.Logger;



public class CallBack extends DockerCommands implements JavaDelegate {
    private static final Logger LOGGER = Logger.getLogger("CallBack");
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    public CallBack() {
        super();
    }
    public void execute(DelegateExecution execution) throws Exception {
        String baseFolder = (String) execution.getVariable("baseFolder");
        String callBackUrl = (String) execution.getVariable("call_back_url");
        if (callBackUrl != null) {
            LOGGER.info("Sending response to callback " + callBackUrl);
            CsvFile.write(execution.getVariable("log_id").toString(), "Sending response to callback",baseFolder);
            String platform = (String)execution.getVariable("platform");
            if(!platform.equals("cloud")){
                HttpResponse response = null;
                HttpPost req = new HttpPost(callBackUrl);
                MultipartEntityBuilder meb = MultipartEntityBuilder.create();
                meb.addTextBody("processId",execution.getVariable("log_id").toString());
                meb.addTextBody("camundaHost",execution.getVariable("camundaHost").toString());
                req.setEntity(meb.build());
                HttpClient httpClient    = HttpClientBuilder.create().build();
                response = httpClient.execute(req);
                LOGGER.info("Response from Mist One "+response);
            }
            else {
                CsvFile.write(execution.getVariable("log_id").toString(), "Call back received",baseFolder);
//                Pusher pusher = new Pusher("358148", "47cbb81f75fff2140125", "e8b5360a72b5bad02f3b");
//                pusher.setCluster("eu");
//                pusher.setEncrypted(true);
//
//                pusher.trigger("my-channel", "my-event", Collections.singletonMap("message", "Finished"));

            }

        }


    }}