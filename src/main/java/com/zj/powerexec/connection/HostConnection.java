package com.zj.powerexec.connection;

import com.zj.powerexec.host.Host;
import com.zj.powerexec.host.HostConnectionNode;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * @author chenzhijie
 * @cdate 2023-07-11
 **/
public abstract class HostConnection implements Closeable {

    protected Host host;
    private StreamExchange streamExchange;

    protected HostConnection(Host host) {
        this.host = host;
    }

    protected void setStreamExchange(StreamExchange streamExchange) {
        this.streamExchange = streamExchange;
        streamExchange.start();
    }

    public Host getHost() {
        return host;
    }

    /**
     * 获取连接交互类
     * */
    public StreamExchange getStreamExchange() {
        return streamExchange;
    }

    /**
     * 拿到打印的数据
     * */
    public String getPrintContent() {
        return getStreamExchange().getPrintContent();
    }

    /**
     * 往流里面输出数据
     * */
    public void write(String cmd) throws IOException {
        getStreamExchange().write(cmd);
    }

    @Override
    public void close() throws IOException {

        try {
            List<HostConnectionNode> hostConnectionNodeList = host.getNodeList();
            if (hostConnectionNodeList.size() > 1) {
                for (int i=0;i< hostConnectionNodeList.size()-2;i++) {
                    streamExchange.write("exit");
                }
            }
        } catch (Exception ignore) {}
        finally {
            streamExchange.close();
        }
    }
}
