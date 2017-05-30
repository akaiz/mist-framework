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
    public String stopContainers(String imageName) throws IOException, InterruptedException {
        LOGGER.info(timestamp+" remove and kill containers that are already  started");
        String command = "docker ps -a -q --filter=ancestor="+imageName;
        Process proc = Runtime.getRuntime().exec(command);
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
    public  Process startContainer(String containerCommand){
        LOGGER.info(timestamp+" Container Intialization \n");

        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec(containerCommand);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return proc;

    }
}
