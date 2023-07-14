package com.zj.powerexec.script;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.zj.powerexec.Constants;
import com.zj.powerexec.connection.HostConnection;
import com.zj.powerexec.connection.StreamExchange;
import com.zj.powerexec.host.HostConnectionNode;
import com.zj.powerexec.observer.ExchangeObserver;
import com.zj.powerexec.observer.FinishOneExchangeListener;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.connection.ConnectionException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/**
 * @author chenzhijie
 * @date 2023-07-14
 **/
@Slf4j
public class TelnetLoginScriptExecutor extends ScriptExecutor{

    static Pattern loginFailPattern = Pattern.compile("(incorrect|fail|error|invalid)");
    static Pattern loginErrorWordPattern = Pattern.compile("([P|p]assword|[L|l]ogin|error|invalid)", Pattern.DOTALL);

    public TelnetLoginScriptExecutor(Map<String, Object> params) {
        super(params);
    }

    @Override
    public String exec(HostConnection hostConnection) throws Exception {
        int loginNodeIndex = (int) params.get(Constants.LOGIN_NODE_INDEX);
        HostConnectionNode firstNode = hostConnection.getHost().getNodeList().get(loginNodeIndex);
        StreamExchange streamExchange = hostConnection.getStreamExchange();
        login(firstNode, streamExchange, loginNodeIndex == 0);
        return null;
    }

    public boolean login(HostConnectionNode connectionNode,
                         StreamExchange streamExchange,
                         boolean isFirstNode) throws InterruptedException, IOException {
        if (!isFirstNode) {
            streamExchange.write(String.format("telnet %s %d", connectionNode.getAddress(), connectionNode.getPort()));
        }
        ExchangeObserver exchangeObserver = streamExchange.getExchangeObserver();
        FinishOneExchangeListener finishOneExchangeListener = new FinishOneExchangeListener();
        exchangeObserver.register(finishOneExchangeListener);

        String outStr;
        try {
            finishOneExchangeListener.refreshPattern("[L|l]ogin:");
            finishOneExchangeListener.await(15, TimeUnit.SECONDS);
            streamExchange.reset();

            finishOneExchangeListener.refreshPattern("[P|p]assword:");
            streamExchange.write(connectionNode.getUsername());
            finishOneExchangeListener.await(15, TimeUnit.SECONDS);
            streamExchange.reset();

            finishOneExchangeListener.refreshPredict(StrUtil::isNotBlank);
            streamExchange.write(connectionNode.getPassword());
            finishOneExchangeListener.await(15, TimeUnit.SECONDS);

            outStr = streamExchange.getPrintContent();
            if (ReUtil.contains(loginFailPattern, outStr)) {
                throw new ConnectionException("登录节点[%s]失败，错误信息%s".formatted(connectionNode.toString(), outStr));
            }
            //如果执行换行命令返回为空字符串的话则认定为登录失败
            finishOneExchangeListener.refreshPredict(StrUtil::isNotBlank);
            streamExchange.write(" ");
            finishOneExchangeListener.await(1, TimeUnit.SECONDS);

            outStr = streamExchange.getPrintContent();
            return checkLoginMsg(connectionNode, outStr);
        } catch (TimeoutException timeoutException) {
            throw new ConnectionException("登录节点[%s]失败，错误信息%s".formatted(connectionNode.toString(), streamExchange.getPrintContent()));
        }
        finally {
            exchangeObserver.remove(finishOneExchangeListener);
        }
    }

    private boolean checkLoginMsg(HostConnectionNode connectionNode, String msg) throws ConnectionException {
        if (StrUtil.isBlank(msg)) {
            throw new ConnectionException("登录节点[%s]失败，登录后敲击空格没有数据输出".formatted(connectionNode.toString()));
        }
        if (ReUtil.contains(loginErrorWordPattern, msg)) {
            throw new ConnectionException("登录节点[%s]失败，错误信息%s".formatted(connectionNode.toString(), msg));
        }
        return true;
    }
}
