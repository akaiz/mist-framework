package ee.ut.mobilelab.mistbpmn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;



public class DockerCommands {
    Logger LOGGER = Logger.getLogger("Mist tomcat app");
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    public String stopContainers() throws IOException, InterruptedException {


        LOGGER.info(timestamp+" Here  in the  final task  to remove and kill containers started");
        String command = "docker ps -a -q --filter=ancestor=akaiz/mist-docker";
        Process proc = Runtime.getRuntime().exec(command);
        // Delay to enable the above command to be finished
        TimeUnit.SECONDS.sleep(2);

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String line;
        while((line = reader.readLine()) != null) {
            LOGGER.info(timestamp+" Result from stopping container started "+line+" \n");
            TimeUnit.SECONDS.sleep(2);

            Runtime.getRuntime().exec("docker kill "+line);

            TimeUnit.SECONDS.sleep(2);

            Runtime.getRuntime().exec("docker rm "+line);
            LOGGER.info(timestamp+" Result from stopping containers finished"+line+" \n");
        }
        return "success";

    }
    public  BufferedReader startContainer(String containerCommand){
        LOGGER.info(timestamp+" Container Intialization \n");

        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec(containerCommand);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(proc.getInputStream()));

        return reader;

    }
}
