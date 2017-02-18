package ee.ut.mobilelab.mistbpmn;


import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.util.logging.Logger;



public class CallBack extends DockerCommands implements JavaDelegate {
    private static final Logger LOGGER = Logger.getLogger("CallBack");

    public CallBack() {
        super();
    }

    public void execute(DelegateExecution execution) throws Exception {
        String callBackUrl = (String)execution.getVariable("call_back_url");
        if(callBackUrl!=null){
            LOGGER.info("Sending response to callback "+callBackUrl);
            LOGGER.info("Sending response data"+execution.getVariable("response"));
        }


    }
}