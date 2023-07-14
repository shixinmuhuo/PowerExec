package com.zj.powerexec.connection.ssh;

import com.zj.powerexec.connection.HostConnection;
import com.zj.powerexec.connection.HostConnector;
import com.zj.powerexec.host.Host;
import com.zj.powerexec.host.HostHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SSHConnectorImplTest {

    @Test
    void connect() {
        String loginScript = """
                [server]
                ssh xxxxx@xxxxx
                Password:xxxxxx
                ssh xxxxx@xxxxxxxx
                password xxxx
                #ssh xxxx@tx-gz-node1
                #password xxxxxxx
                """;

        String execScript = """
                cd /data/power-exec
                sh build.sh
                """;
        Host host = HostHelper.parseHost(loginScript).get(0);

        HostConnector hostConnector = new SSHConnectorImpl();
        try (HostConnection hostConnection = hostConnector.connect(host)){
            hostConnection.write("hostname");
            Thread.sleep(1000);
            String rsp = hostConnection.getPrintContent();
            System.out.println(rsp);
            assertTrue(rsp.contains("node1"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}