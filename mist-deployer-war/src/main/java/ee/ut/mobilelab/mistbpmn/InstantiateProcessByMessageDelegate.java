package ee.ut.mobilelab.mistbpmn;

import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.ServletProcessApplication;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@ProcessApplication("UT Mist Deployer App")
public class InstantiateProcessByMessageDelegate extends ServletProcessApplication implements JavaDelegate {
    public InstantiateProcessByMessageDelegate() {
    }

    public void execute(DelegateExecution execution) throws Exception {
        RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();
        runtimeService.startProcessInstanceByMessage("MistDeployer");
    }
}