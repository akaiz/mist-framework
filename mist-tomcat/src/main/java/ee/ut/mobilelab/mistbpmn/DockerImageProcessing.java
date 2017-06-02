package ee.ut.mobilelab.mistbpmn;
import com.github.kevinsawicki.http.HttpRequest;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class DockerImageProcessing extends DockerCommands implements JavaDelegate {
    private Expression dockerImage;
    private Expression command;
    private Expression imagePath;
    String baseUrl ="localhost";
    String baseFolder=null;
    public DockerImageProcessing() {
        super();
    }
    public void execute(DelegateExecution execution) throws Exception {
        String baseFolder = (String) execution.getVariable("baseFolder");
        String dockerImageValue = (String) dockerImage.getValue(execution);
        String commandValue = (String) command.getValue(execution);
        String imageUrlValue = (String) imagePath.getValue(execution);
        String platform = (String) execution.getVariable("platform");
        if(!platform.equals("mist")){
            baseUrl = (String)execution.getVariable("baseUrl");
        }
        baseFolder=(String)execution.getVariable("baseFolder");
        File file = new File(imageUrlValue);
        String folder =  baseFolder+"/mist-framework/mist-deployer-app/mist-files/";
        String command = "docker run -p 8090:8080 -v "+folder+":/mist"+" "+dockerImageValue;
        String line = " ";
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(super.startContainer(command).getInputStream()));

        while((line = reader.readLine()) != null) {
            if(line.contains("Tomcat started on port(s)")){
                String processRequest = baseUrl +":8090/image?task="+commandValue+"&imagePath=/mist/pay.jpg";
                String response = HttpRequest.get(processRequest).body();
                execution.setVariable("response",response);

                break;
            }
        }
//        String processRequest = baseUrl +":8090/image?task="+commandValue+
//                "&imagePath="+baseUrl+"/mist-framework/mist-deployer-app/mist-files/pay.jpg";
//        String response = HttpRequest.get(processRequest).body();
//        execution.setVariable("response",response);
        CsvFile.write(execution.getVariable("log_id").toString(),"Mist-docker  completed",baseFolder);
        super.stopContainers(dockerImageValue);


    }
}
