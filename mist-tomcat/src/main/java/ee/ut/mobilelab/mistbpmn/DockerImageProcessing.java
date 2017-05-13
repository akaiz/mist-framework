package ee.ut.mobilelab.mistbpmn;
import com.github.kevinsawicki.http.HttpRequest;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.logging.Logger;
public class DockerImageProcessing extends DockerCommands implements JavaDelegate {
    private Expression dockerImage;
    private Expression command;
    private Expression imagePath;
     String localhost="http://localhost";
    public DockerImageProcessing() {
        super();
    }
    public void execute(DelegateExecution execution) throws Exception {

        String dockerImageValue = (String) dockerImage.getValue(execution);
        String commandValue = (String) command.getValue(execution);
        String imageUrlValue = (String) imagePath.getValue(execution);
        File file = new File(imageUrlValue);
        String folder = "/home/pi/Desktop/mist-framework/mist-deployer-app/mist-files/";
        String command = "docker run -p 8090:8080 -v "+folder+":/mist"+" "+dockerImageValue;
        String line = " ";
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(super.startContainer(command).getInputStream()));

        while((line = reader.readLine()) != null) {
            if(line.contains("Tomcat started on port(s)")){
                String processRequest =localhost+":8090/image?task="+commandValue+"&imagePath=/mist/pay.jpg";
                String response = HttpRequest.get(processRequest).body();
                execution.setVariable("response",response);

                break;
            }
   }
        String processRequest =localhost+":8090/image?task="+commandValue+
                "&imagePath=/home/pi/Desktop/mist-framework/mist-deployer-app/mist-files/pay.jpg";
        String response = HttpRequest.get(processRequest).body();
        execution.setVariable("response",response);
        CsvFile.write(execution.getVariable("log_id").toString(),"Mist-docker  completed");
       super.stopContainers(dockerImageValue);


    }
}
