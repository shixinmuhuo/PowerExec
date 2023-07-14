package com.zj.powerexec.host;

import com.zj.powerexec.Constants;
import com.zj.powerexec.connection.HostConnection;
import com.zj.powerexec.script.ConditionShellScriptExecutor;
import com.zj.powerexec.script.ShellScriptExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HostHelperTest {

    @Test
    void parseHost() {
        String src = """
                [node1]
                ssh root@node1
                Password:123456
                keyLocation: /data
                ssh zhijie@192.168.0.104
                password ccz589497bfhdhjjbh2902%$%
                telnet czj@liifhh
                password ksjjbkbjjhwevjfhgew
                
                [node2]
                ssh root@node1
                Password:123456
                keyLocation: /data
                ssh zhijie@192.168.0.104
                password ccz589497bfhdhjjbh2902%$%
                telnet czj@liifhh
                password ksjjbkbjjhwevjfhgew
                """;
        List<Host> hostList = HostHelper.parseHost(src);
        Assertions.assertNotNull(hostList);
        assertFalse(hostList.isEmpty());
        assertEquals(2, hostList.size());
        Host host = hostList.get(0);
        assertNotNull(host.getHostName());
        assertNotNull(host.getNodeList());
    }

    @Test
    void createConnection() {
        String src = """
                [qingxi-104]
                ssh chenzhijie@192.168.0.1
                password:123456
                telnet chenzhijie@192.168.0.1
                password:123456
                ssh chenzhijie@192.168.0.1
                password:123456

                """;
        List<Host> hostList = HostHelper.parseHost(src);
        Host host = hostList.get(0);
        try (HostConnection hostConnection = HostHelper.createConnection(host)){
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.SCRIPT, "ip a && sleep 1");
            params.put(Constants.PATTERN, "127.0.0.1");
            params.put(Constants.TIMOUT, "2s");
            ShellScriptExecutor scriptExecutor = new ConditionShellScriptExecutor(params);
            scriptExecutor.exec(hostConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}