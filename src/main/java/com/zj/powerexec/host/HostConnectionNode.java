package com.zj.powerexec.host;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author chenzhijie
 * @date 2023-07-11
 **/
@Data
@Accessors(chain = true)
public class HostConnectionNode {

    /**
     * 连接协议。ssh，telnet等
     * */
    private String protocol;

    /**
     * 地址信息
     * */
    private String address;

    /**
     * 端口信息
     * */
    private int port;

    /**
     * 用户名
     * */
    private String username;
    /**
     * 密码
     * */
    private String password;
    /**
     * 密钥位置
     * */
    private String keyLocation;

    public String toString() {
        return "%s %s %s@%s -p %s".formatted(protocol, keyLocation==null?"":"-i "+keyLocation,  username, address, port);
    }

}
