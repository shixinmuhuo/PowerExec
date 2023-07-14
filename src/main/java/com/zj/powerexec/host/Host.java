package com.zj.powerexec.host;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author chenzhijie
 * @date 2023-07-11
 **/
@Data
@Accessors(chain = true)
public class Host {

    /**
     * 设备名称
     * */
    private String hostName;

    /**
     * 目标设备的登录节点，如果是直连，那么就只有一个登录节点，
     * 一次跳板则有连个节点，两次跳板则有两个节点，依此类推
     * */
    private List<HostConnectionNode> nodeList;
}
