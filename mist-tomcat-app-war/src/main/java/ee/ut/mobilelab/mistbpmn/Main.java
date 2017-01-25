package ee.ut.mobilelab.mistbpmn;
// file: RunShellCommandFromJava.java
import com.github.kevinsawicki.http.HttpRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;
// This class is made for testing never used in the system running
public class Main {
    private static final Logger LOGGER = Logger.getLogger("Camunda Open HAB");

    public static void main(String[] args) throws IOException, InterruptedException {
        String command = "docker ps -a -q";
        Process proc = Runtime.getRuntime().exec(command);

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(proc.getInputStream()));

        String line = "";
        while((line = reader.readLine()) != null) {
            Runtime.getRuntime().exec("docker kill "+line);
            Runtime.getRuntime().exec("docker rm "+line);
            System.out.println("Result from stopping containers "+line+" \n");

        }


//        proc.waitFor();


    }
}