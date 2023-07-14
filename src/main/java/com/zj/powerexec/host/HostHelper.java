package com.zj.powerexec.host;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.zj.powerexec.Constants;
import com.zj.powerexec.connection.HostConnection;
import com.zj.powerexec.connection.ssh.SSHConnectorImpl;
import com.zj.powerexec.connection.telnet.TelnetConnectorImpl;
import com.zj.powerexec.script.SSHLoginScriptExecutor;
import com.zj.powerexec.script.ScriptExecutor;
import com.zj.powerexec.script.TelnetLoginScriptExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author chenzhijie
 * @date 2023-07-11
 **/
@Slf4j
public class HostHelper {

    public static Pattern connectPattern = Pattern.compile("(ssh|telnet)\\s*(\\S+)@(\\S+)(\\s+-p\\s+(\\d+))?");

    public static Pattern passwordPattern = Pattern.compile("[P|p]assword:?\\s*(\\S+)");
    public static Pattern keyLocationPattern = Pattern.compile("keyLocation:?\\s*(\\S+)");

    public static List<Host> parseHost(String src) {
        List<Host> hostList = new ArrayList<>();

        String hostName = null;
        String []lines = src.split("\n");
        List<HostConnectionNode> nodeList = null;
        for (String line : lines) {
            line = StrUtil.trim(line);
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("[")) {
                if (nodeList != null) {
                    Host host = new Host();
                    host.setHostName(hostName)
                            .setNodeList(nodeList);
                    hostList.add(host);
                }
                nodeList = new ArrayList<>();
                hostName = ReUtil.getGroup1("\\[(.*)]", line);
            } else if (ReUtil.isMatch(connectPattern, line)) {
                Assert.notNull(nodeList, "必须得有设备名称才能解析连接信息");
                HostConnectionNode hostConnectionNode = new HostConnectionNode();

                List<String> groups =  ReUtil.getAllGroups(connectPattern, line);

                hostConnectionNode.setProtocol(groups.get(1));
                hostConnectionNode.setUsername(groups.get(2));
                hostConnectionNode.setAddress(groups.get(3));
                if (groups.size() == 5) {
                    hostConnectionNode.setPort(Integer.parseInt(groups.get(4)));
                } else {
                    if (hostConnectionNode.getProtocol().equals("ssh")) {
                        hostConnectionNode.setPort(22);
                    } else if (hostConnectionNode.getProtocol().equals("telnet")) {
                        hostConnectionNode.setPort(23);
                    }
                }

                nodeList.add(hostConnectionNode);
            } else if (ReUtil.isMatch(passwordPattern, line)) {
                Assert.notNull(nodeList, "必须得有设备名称才能解析连接信息");
                Assert.isFalse(nodeList.isEmpty(), "必须得有连接信息才能解析密码");
                HostConnectionNode hostConnectionNode = nodeList.get(nodeList.size()-1);
                String password = ReUtil.getGroup1(passwordPattern, line);
                hostConnectionNode.setPassword(password);


            } else if (ReUtil.isMatch(keyLocationPattern, line)) {
                Assert.notNull(nodeList, "必须得有设备名称才能解析连接信息");
                Assert.isFalse(nodeList.isEmpty(), "必须得有连接信息才能解析密钥");
                HostConnectionNode hostConnectionNode = nodeList.get(nodeList.size()-1);
                String keyLocation = ReUtil.getGroup1(keyLocationPattern, line);
                hostConnectionNode.setKeyLocation(keyLocation);
            }

        }

        if (nodeList != null) {
            Host host = new Host();
            host.setHostName(hostName)
                    .setNodeList(nodeList);
            hostList.add(host);
        }
        return hostList;
    }

    public static HostConnection createConnection(Host host) throws Exception {
        HostConnection hostConnection = switch (host.getNodeList().get(0).getProtocol()) {
            case "ssh" -> new SSHConnectorImpl().connect(host);
            case "telnet" -> new TelnetConnectorImpl().connect(host);
            default -> throw new IllegalStateException("Unexpected protocol: " + host.getNodeList().get(0).getProtocol());
        };

        try {
            loginNext(hostConnection, 0);
            log.debug("login host[{}] success", host.getHostName());
            return hostConnection;
        } catch (Exception e) {
            hostConnection.close();
            throw e;
        }
    }

    /**
     * 通过递归登录
     * */
    private static void loginNext(HostConnection hostConnection, int nodeIndex) throws Exception {
        List<HostConnectionNode> nodeList = hostConnection.getHost().getNodeList();
        if (nodeIndex >= nodeList.size()) {
            return;
        }
        HostConnectionNode hostConnectionNode = nodeList.get(nodeIndex);
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.LOGIN_NODE_INDEX, nodeIndex);
        ScriptExecutor scriptExecutor = switch (hostConnectionNode.getProtocol()) {
            case "ssh" -> new SSHLoginScriptExecutor(params);
            case "telnet" -> new TelnetLoginScriptExecutor(params);
            default -> throw new IllegalStateException("Unexpected protocol: " + hostConnectionNode.getProtocol());
        };

        scriptExecutor.exec(hostConnection);
        //递归到下一个登录节点
        loginNext(hostConnection, ++nodeIndex);
    }

}
