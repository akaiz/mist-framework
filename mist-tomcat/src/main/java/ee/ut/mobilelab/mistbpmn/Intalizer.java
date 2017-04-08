package ee.ut.mobilelab.mistbpmn;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.sql.Timestamp;
import java.util.logging.Logger;

/**
 * Created by agabaisaac on 4/8/17.
 */
public class Intalizer extends DockerCommands implements JavaDelegate {
    private static final Logger LOGGER = Logger.getLogger("Intializer");
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    public Intalizer() {
        super();
    }
    public void execute(DelegateExecution execution) throws Exception {
        String runTwice = (String)execution.getVariable("run_twice");
        String runCount = (String)execution.getVariable("run_count");
        if(runTwice!=null &&runTwice.equals("yes")){

            if(runCount==null){
                execution.setVariable("run_count","0");
                execution.setVariable("run_twice","yes");
            }
            else{
                execution.setVariable("run_twice","no");
            }
        }
        else{
            execution.setVariable("run_twice","no");
        }
        LOGGER.info("------> run twice"+runTwice+"---> count "+runCount);
    }


}