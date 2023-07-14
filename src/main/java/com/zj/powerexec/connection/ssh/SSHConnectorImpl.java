package com.zj.powerexec.connection.ssh;

import cn.hutool.core.util.StrUtil;
import com.zj.powerexec.exceptions.ConnectionException;
import com.zj.powerexec.host.Host;
import com.zj.powerexec.host.HostConnectionNode;
import com.zj.powerexec.connection.HostConnection;
import com.zj.powerexec.connection.HostConnector;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;

import java.io.IOException;
import java.util.List;

/**
 * ssh伪终端的建立
 * @author chenzhijie
 * @date 2023-07-11
 **/
@Slf4j
public class SSHConnectorImpl implements HostConnector {

    //强制设置超时时间20s
    private static final int connect_timeout = 1000 * 15;
    private static final int timeout = 1000 * 30;

    @Override
    public HostConnection connect(Host host) throws Exception {

        List<HostConnectionNode> hostConnectionNodeList = host.getNodeList();
        SSHClient sshClient = new SSHClient();
        sshClient.addHostKeyVerifier(new PromiscuousVerifier());

        //登录第一个节点
        HostConnectionNode firstHostConnectionNode = hostConnectionNodeList.get(0);
        try {
            sshClient.setConnectTimeout(connect_timeout);
            sshClient.setTimeout(timeout);
            sshClient.connect(firstHostConnectionNode.getAddress(), firstHostConnectionNode.getPort());
        } catch (IOException e) {
            String exceptionMsg = String.format("直连目标设备异常:%s, exception:%s", firstHostConnectionNode, e.getClass() + "@" + e.getMessage());
            throw new ConnectionException(exceptionMsg);
        }

        try {
            auth(firstHostConnectionNode, sshClient);
        } catch (TransportException | UserAuthException e) {
            sshClient.close();
            String exceptionMsg = String.format("直连目标设备的账密异常:%s, exception:%s", firstHostConnectionNode, e.getClass() + "@" + e.getMessage());
            throw new ConnectionException(exceptionMsg);
        }

        return new SSHHostConnection(sshClient, host);
    }



    private void auth(HostConnectionNode hostConnectionNode, SSHClient sshClient) throws UserAuthException, TransportException {
        if (StrUtil.isBlank(hostConnectionNode.getPassword())){

            if (StrUtil.isBlank(hostConnectionNode.getKeyLocation())) {
                sshClient.authPublickey(hostConnectionNode.getUsername());
            } else {
                sshClient.authPublickey(hostConnectionNode.getUsername(), hostConnectionNode.getKeyLocation());
            }
        } else {
            sshClient.authPassword(hostConnectionNode.getUsername(), hostConnectionNode.getPassword());
        }
    }
}
