package ee.ut.mobilelab.mistbpmn;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.io.File;
import java.util.logging.Logger;


public class MistRequestTwo implements JavaDelegate {
    private static final Logger LOGGER = Logger.getLogger("CallBack");



    public MistRequestTwo() {

        super();



    }

    public void execute(DelegateExecution execution) throws Exception {

        String mistTwo= (String)execution.getVariable("mist_two_url");
        String mistFilesPath = (String)execution.getVariable("mist_files_path");
        String mistFile = (String)execution.getVariable("mist_file");
        String processId = (String)execution.getVariable("processId");
        String payload = (String)execution.getVariable("payload");
        LOGGER.info("HERE IN THE MIST TWO"+mistTwo);
        if(mistTwo!=null){
            File war = new File(mistFilesPath+"mist-0.war");
            File mist_file = new File(mistFilesPath+mistFile);
            HttpPost req = new HttpPost(mistTwo);
            MultipartEntityBuilder meb = MultipartEntityBuilder.create();
            meb.addTextBody("processId",processId);

            if(payload.equals("true")){
                File mist_payload = new File(mistFilesPath+(mistFile.contains("light_0")?"payload-light.jpg":"payload-heavy.jpeg"));

                meb.addBinaryBody("payload", mist_payload, ContentType.APPLICATION_OCTET_STREAM, mist_payload.getName());
            }

            meb.addBinaryBody("war", war, ContentType.APPLICATION_OCTET_STREAM, war.getName());
            meb.addBinaryBody("mist", mist_file, ContentType.APPLICATION_OCTET_STREAM, mist_file.getName());

            req.setEntity(meb.build());
            HttpClient httpClient    = HttpClientBuilder.create().build();
            HttpResponse response = httpClient.execute(req);
            LOGGER.info("Response from Mist TWO ");
        }




    }
}
