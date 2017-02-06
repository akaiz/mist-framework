package ee.ut.mobilelab.mistbpmn;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.logging.Logger;

public class FinalTask extends DockerCommands implements JavaDelegate {
    private static final Logger LOGGER = Logger.getLogger("FinalTask");

    public FinalTask() {
        super();
    }

    public void execute(DelegateExecution execution) throws Exception {
        super.stopContainers();

    }
}
