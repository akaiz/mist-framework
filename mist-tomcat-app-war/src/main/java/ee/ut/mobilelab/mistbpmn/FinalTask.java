package ee.ut.mobilelab.mistbpmn;

import com.github.kevinsawicki.http.HttpRequest;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by agabaisaac on 12/31/16.
 */
public class FinalTask implements JavaDelegate {
    private static final Logger LOGGER = Logger.getLogger("FinalTask");

    public FinalTask() {
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
