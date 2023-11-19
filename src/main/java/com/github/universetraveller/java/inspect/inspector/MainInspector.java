package com.github.universetraveller.java.inspect.inspector;
import com.github.universetraveller.java.inspect.model.Inspector;
import com.github.universetraveller.java.inspect.model.InspectorRunner;
public class MainInspector extends Inspector {

    private void prepareToRun() throws Exception{
        this.vm = this.launchVirtualMachine();
        this.makeClassPrepareRequest().enable();
    }

    public void execute(InspectorRunner runner){
        try{
            this.prepareToRun();
            runner.run(this);
        }catch(Exception e){
            this.logger.severe("MainInspector process is unexpectedly stopped");
            e.printStackTrace();
        }
    }
}