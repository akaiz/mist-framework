package ee.ut.mobilelab.mistbpmn;

import com.github.kevinsawicki.http.HttpRequest;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.model.bpmn.instance.Extension;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.logging.Logger;

/**
 * Created by agabaisaac on 2/16/17.
 */
// Start the docker image    docker run -p 8090:8080 -v image_absolute_path_folder :/image_docker_folder akaiz/mist-docker

public class DockerImageProcessing extends DockerCommands implements JavaDelegate {
    private static final Logger LOGGER = Logger.getLogger("CallBack");
    private Expression dockerImage;
    private Expression command;
    private Expression imagePath;
   // String localhost="http://138.68.176.11";
    String localhost="http://localhost";


    public DockerImageProcessing() {

        super();

    }

    public void execute(DelegateExecution execution) throws Exception {

        String runTwice = (String)execution.getVariable("run_twice");

        if(runTwice!=null && runTwice.equals("yes")){
            execution.setVariable("run_twice","yes");
        }
        else{
            execution.setVariable("run_twice","no");
        }
        String dockerImageValue = (String) dockerImage.getValue(execution);
        String commandValue = (String) command.getValue(execution);
        String imageUrlValue = (String) imagePath.getValue(execution);
        super.stopContainers(dockerImageValue);
        LOGGER.info("File path path"+imageUrlValue);
        File file = new File(imageUrlValue);
        File folder = new File(file.getParent());
        String command = "docker run -p 8090:8080 -v "+folder+":/mist"+" "+dockerImageValue;
        LOGGER.info("Final docker command"+command);
        String line = " ";
        // starting container and getting its buffer response
        CsvFile.write(execution.getVariable("log_id").toString(),"Mist-docker  started");
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(super.startContainer(command).getInputStream()));

        while((line = reader.readLine()) != null) {

            if(line.contains("Tomcat started on port(s)")){
                LOGGER.info(timestamp+" Mist-docker  started \n");
                String processRequest =localhost+":8090/image?task="+commandValue+"&imagePath=/mist/"+file.getName();
                LOGGER.info(processRequest+" \n");


                String response = HttpRequest.get(processRequest).body();
                execution.setVariable("response",response);

                break;
            }
        }
        CsvFile.write(execution.getVariable("log_id").toString(),"Mist-docker  completed");
        super.stopContainers(dockerImageValue);


    }
}
