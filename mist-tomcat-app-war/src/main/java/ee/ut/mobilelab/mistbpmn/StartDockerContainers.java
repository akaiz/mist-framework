package ee.ut.mobilelab.mistbpmn;


import java.util.logging.Logger;

import com.github.kevinsawicki.http.HttpRequest;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class StartDockerContainers extends DockerCommands implements JavaDelegate  {
    private static final Logger LOGGER = Logger.getLogger("Mist tomcat app");


    public StartDockerContainers() {
        super();
    }

    public void execute(DelegateExecution execution) throws Exception {


        super.stopContainers();


        String responseUrl = (String)execution.getVariable("responseUrl");

        String command = "docker run -p 8090:8080 akaiz/mist-docker";

        String line = " ";

        // starting container and getting its buffer response

        while((line = super.startContainer(command).readLine()) != null) {

            if(line.contains("Tomcat started on port(s)")){
                 LOGGER.info(timestamp+" Mist-docker  started \n");
                 LOGGER.info( timestamp+"  Request made to Mist-docker from Camunda \n");
                 // making a get request that executes a given task in a container
                String response = HttpRequest.get("http://localhost:8090/xml/Tallinn-Harku").body();

                 LOGGER.info( timestamp+" Camunda received results from Mist-docker \n");
                 // here is the response  returned
                 LOGGER.info(response + "\n");
                // for testing  we are using devivceId 123  but finally we will be using the response returned from the docker

                int temperature = Integer.parseInt(response);
                if(temperature<0) {
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
