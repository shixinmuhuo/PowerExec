package com.zj.powerexec.task;

import com.zj.powerexec.host.Host;
import com.zj.powerexec.host.HostHelper;
import com.zj.powerexec.script.ScriptExecutor;
import com.zj.powerexec.script.ScriptHelper;
import org.junit.jupiter.api.Test;

import java.util.List;

class ExecScriptTaskTest {

    @Test
    void call() throws Exception {
        String loginScript = """
                [test]
                ssh root@192.168.0.104
                Password:123456
                [test1]
                ssh root@xxxx
                Password:xxxxx
                """;
        String script = """
                [ShellScriptExecutor]
                output=true
                script= cd /data && pwd && ip a &&  sleep 1
                
                """;
        List<ScriptExecutor> scriptExecutorList = ScriptHelper.parseScript(script);
        Host host = HostHelper.parseHost(loginScript).get(0);
        ExecScriptTask execScriptTask = new ExecScriptTask(host, scriptExecutorList);
        String rsp = execScriptTask.call();
        System.out.println(rsp);
    }
}