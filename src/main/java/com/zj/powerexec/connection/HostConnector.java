package com.zj.powerexec.connection;

import com.zj.powerexec.host.Host;

/**
 * @author chenzhijie
 * @cdate 2023-07-11
 **/
public interface HostConnector {

    /**
     * @param host 主机连接参数
     * @return HostConnection 主机连接
     * */
    HostConnection connect(Host host) throws Exception;
}
