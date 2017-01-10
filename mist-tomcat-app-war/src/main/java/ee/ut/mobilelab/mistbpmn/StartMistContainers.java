package ee.ut.mobilelab.mistbpmn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.github.kevinsawicki.http.HttpRequest;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class StartMistContainers implements JavaDelegate {
    private static final Logger LOGGER = Logger.getLogger("Camunda Open HAB");

    public StartMistContainers() {
    }

    public void execute(DelegateExecution execution) throws Exception {


        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        LOGGER.info(timestamp+" Here  in the  final task  to remove and kill containers started");
        String command = "docker ps -a -q --filter=ancestor=akaiz/mist-docker";
        Process proc = Runtime.getRuntime().exec(command);
        // Delay to enable the above command to be finished
        TimeUnit.SECONDS.sleep(2);

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(proc.getInputStream()));

        String line = "";
        stopAndKillContainer(timestamp, reader);
        String deviceId = (String)execution.getVariable("deviceId");
        String deviceTask1 = (String)execution.getVariable("deviceTask");
        String responseUrl = (String)execution.getVariable("responseUrl");
        LOGGER.info("Here is Acess to open Hab \'" + execution.getVariable("deviceId") + "\'...");
   

         command = "docker run -p 8090:8080 akaiz/mist-docker";

        proc = Runtime.getRuntime().exec(command);
         LOGGER.info(timestamp+" Camunda made Mist-docker Intialization \n");



         reader =
                new BufferedReader(new InputStreamReader(proc.getInputStream()));

        line = "";
        while((line = reader.readLine()) != null) {

            if(line.contains("Tomcat started on port(s)")){
                 LOGGER.info(timestamp+" Mist-docker  started \n");
                 LOGGER.info( timestamp+"  Request made to Mist-docker from Camunda \n");
                 // making a get request that executes a given task in a container
                String response = HttpRequest.get("http://localhost:8090/xml/Tallinn-Harku").body();

                 LOGGER.info( timestamp+" Camunda received results from Mist-docker \n");
                 // here is the response  returned
                 LOGGER.info(response + "\n");
                // for testing  we are using devivceId 123  but finally we will be using the response returned from the docker
                if(deviceId.equals("123")) {
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

    private void stopAndKillContainer(Timestamp timestamp, BufferedReader reader) throws IOException, InterruptedException {
        String line;
        while((line = reader.readLine()) != null) {
            LOGGER.info(timestamp+" Result from stopping container started "+line+" \n");
            TimeUnit.SECONDS.sleep(2);

            Runtime.getRuntime().exec("docker kill "+line);

            TimeUnit.SECONDS.sleep(2);

            Runtime.getRuntime().exec("docker rm "+line);
            LOGGER.info(timestamp+" Result from stopping containers finished"+line+" \n");

        }
    }
}
