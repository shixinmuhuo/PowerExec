package com.zj.powerexec.connection.ssh;

import com.zj.powerexec.connection.HostConnection;
import com.zj.powerexec.connection.StreamExchange;
import com.zj.powerexec.host.Host;
import lombok.SneakyThrows;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;

import java.io.IOException;

/**
 * @author chenzhijie
 * @date 2023-07-11
 **/
public class SSHHostConnection extends HostConnection {

    private final SSHClient sshClient;

    public SSHHostConnection(SSHClient sshClient, Host host) {
        super(host);
        this.sshClient = sshClient;
        setStreamExchange(buildStreamExchange());
    }

    @SneakyThrows
    protected StreamExchange buildStreamExchange() {
        Session session = sshClient.startSession();
        session.allocateDefaultPTY();
        Session.Shell shell = session.startShell();
        return new StreamExchange(shell.getInputStream(), shell.getErrorStream(), shell.getOutputStream(), host.getHostName());
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            sshClient.close();
        }
    }
}
