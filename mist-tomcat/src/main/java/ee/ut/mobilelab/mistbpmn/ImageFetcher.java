package ee.ut.mobilelab.mistbpmn;


import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Logger;

public class ImageFetcher extends DockerCommands implements JavaDelegate {
    private static final Logger LOGGER = Logger.getLogger("CallBack");
    Expression imageUrl;

    public ImageFetcher() {

        super();

    }

    public void execute(DelegateExecution execution) throws Exception {
        String baseFolder = (String) execution.getVariable("baseFolder");
        LOGGER.info("Here in the image fetcher started");
        CsvFile.write(execution.getVariable("log_id").toString(),"Image Fetch Started",baseFolder);
        String imageUrlValue = (String) imageUrl.getValue(execution);

        File file = new File(imageUrlValue);
        File folder = new File(file.getParent());
           if(!folder.exists()){
               folder.mkdirs();
           }

           downloadUsingNIO((String) execution.getVariable("remote_image_url"),imageUrlValue);
           execution.setVariable("fetch","success");
          CsvFile.write(execution.getVariable("log_id").toString(),"Image Fetch complete",baseFolder);
          LOGGER.info("Here in the image fetcher download complete ---->"+ execution.getVariable("remote_image_url"));

    }
    private  void downloadUsingNIO(String urlStr, String file) throws IOException {
        URL url = new URL(urlStr);
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }
}