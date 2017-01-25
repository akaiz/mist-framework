package ee.ut.mobilelab.mistbpmn;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.sql.Timestamp;
import java.util.logging.Logger;

/**
 * Created by agabaisaac on 12/31/16.
 */
public class DesiredTask implements JavaDelegate {
    private static final Logger LOGGER = Logger.getLogger("DesiredTask");

    public DesiredTask() {
    }

    public void execute(DelegateExecution execution) throws Exception {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        LOGGER.info(timestamp+" Here in Desired Task.");
        LOGGER.info(timestamp+" Here is deviceId \'" + execution.getVariable("deviceId") + "\'...");
        LOGGER.info(timestamp+" Here is the response  that was returned from previous task \'" + execution.getVariable("response") + "\n");
        LOGGER.info(timestamp+" Here  Desired Task ended ");

    }
}
