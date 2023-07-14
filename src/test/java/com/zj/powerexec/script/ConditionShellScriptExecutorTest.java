package com.zj.powerexec.script;

import com.zj.powerexec.Constants;
import com.zj.powerexec.connection.HostConnection;
import com.zj.powerexec.connection.HostConnector;
import com.zj.powerexec.connection.ssh.SSHConnectorImpl;
import com.zj.powerexec.host.Host;
import com.zj.powerexec.host.HostHelper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ConditionShellScriptExecutorTest {

    @Test
    void exitCondition() {
        String loginScript = """
                [ray-server]
                ssh root@192.168.0.104
                Password:xxxxxxxx
                
                """;

        String execScript = """
                cd /data/PowerExec
                sh build.sh
                """;
        Host host = HostHelper.parseHost(loginScript).get(0);

        Map<String, Object> params = new HashMap<>();
        params.put(Constants.SCRIPT, execScript);
        params.put(Constants.TIMOUT, "2m");
        params.put(Constants.PATTERN, "");
        ShellScriptExecutor scriptExecutor =
                new ConditionShellScriptExecutor(params);
        HostConnector hostConnector = new SSHConnectorImpl();
        try (HostConnection hostConnection = hostConnector.connect(host)){
            String rsp = scriptExecutor.exec(hostConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    void loginMysql() {
        String loginScript = """
                [test]
                ssh root@192.168.0.104
                Password:xxxxx
                
                """;

        Host host = HostHelper.parseHost(loginScript).get(0);


        String script = """
                [ConditionShellScriptExecutor]
                script= docker exec -it mysql-server mysql -u root -p
                timeout = 2m
                pattern = password
                
                [ConditionShellScriptExecutor]
                script= 123456
                timeout = 2s
                pattern = mysql>
                [ConditionShellScriptExecutor]
                output=true
                script= show databases;
                timeout = 2s
                pattern = mysql>
                """;
        List<ScriptExecutor> scriptExecutorList = ScriptHelper.parseScript(script);

        HostConnector hostConnector = new SSHConnectorImpl();
        try (HostConnection hostConnection = hostConnector.connect(host)){
            for (ScriptExecutor scriptExecutor : scriptExecutorList) {
                String rsp =  scriptExecutor.exec(hostConnection);
                System.out.println(rsp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}