package com.zj.powerexec.connection.telnet;

import com.zj.powerexec.connection.HostConnection;
import com.zj.powerexec.connection.StreamExchange;
import com.zj.powerexec.host.Host;
import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;

/**
 * @author chenzhijie
 * @date 2023-07-14
 **/
public class TelnetHostConnection extends HostConnection {

    private final TelnetClient telnetClient;

    protected TelnetHostConnection(Host host, TelnetClient telnetClient) {
        super(host);
        this.telnetClient = telnetClient;
        setStreamExchange(buildStreamExchange());
    }

    protected StreamExchange buildStreamExchange() {

        return new StreamExchange(telnetClient.getInputStream(), null, telnetClient.getOutputStream(), host.getHostName());
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            telnetClient.disconnect();
        }
    }
}
