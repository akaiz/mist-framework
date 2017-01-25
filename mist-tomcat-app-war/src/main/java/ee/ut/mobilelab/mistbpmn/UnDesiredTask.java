package ee.ut.mobilelab.mistbpmn;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.sql.Timestamp;
import java.util.logging.Logger;

/**
 * Created by agabaisaac on 12/31/16.
 */
public class UnDesiredTask implements JavaDelegate {
    private static final Logger LOGGER = Logger.getLogger("Camunda Open HAB");

    public UnDesiredTask() {
    }

    public void execute(DelegateExecution execution) throws Exception {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        LOGGER.info(timestamp+" Here is result \'" + execution.getVariable("result") + "\n");
        LOGGER.info(timestamp+" Here  in the  un desired task ");

    }
}
