package com.zj.powerexec.connection.telnet;

import com.zj.powerexec.connection.HostConnection;
import com.zj.powerexec.connection.HostConnector;
import com.zj.powerexec.exceptions.ConnectionException;
import com.zj.powerexec.host.Host;
import com.zj.powerexec.host.HostConnectionNode;
import org.apache.commons.net.telnet.TelnetClient;

/**
 * telnet伪终端的建立
 * @author chenzhijie
 * @date 2023-07-14
 **/
public class TelnetConnectorImpl implements HostConnector {
    @Override
    public HostConnection connect(Host host) throws Exception {
        //termType 字体
        TelnetClient telnetClient = new TelnetClient("VT220");
        telnetClient.setConnectTimeout(10000);
        telnetClient.setDefaultTimeout(30000);
        try {
            //登录第一个节点
            HostConnectionNode firstHostConnectionNode = host.getNodeList().get(0);
            telnetClient.connect(firstHostConnectionNode.getAddress(), firstHostConnectionNode.getPort());
        } catch (Exception e) {
            telnetClient.disconnect();
            throw new ConnectionException("端口不通");
        }

        return new TelnetHostConnection(host, telnetClient);
    }


}
