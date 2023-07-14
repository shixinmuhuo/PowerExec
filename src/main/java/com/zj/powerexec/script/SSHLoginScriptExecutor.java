package com.zj.powerexec.script;

import com.zj.powerexec.Constants;
import com.zj.powerexec.connection.HostConnection;
import com.zj.powerexec.connection.StreamExchange;
import com.zj.powerexec.exceptions.ConnectionException;
import com.zj.powerexec.host.HostConnectionNode;
import com.zj.powerexec.observer.ExchangeObserver;
import com.zj.powerexec.observer.FinishOneExchangeListener;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/**
 * ssh跳板登录，第一个节点已经登录建立了伪终端，
 * 剩下的节点登录直接通过命令行交互式登录
 * @author chenzhijie
 * @date 2023-07-11
 **/
public class SSHLoginScriptExecutor extends ScriptExecutor{


    public SSHLoginScriptExecutor(Map<String, Object> params) {
        super(params);
    }

    @Override
    public String exec(HostConnection hostConnection) throws Exception{
        int loginNodeIndex = (int) params.get(Constants.LOGIN_NODE_INDEX);
        List<HostConnectionNode> hostConnectionNodeList = hostConnection.getHost().getNodeList();
        StreamExchange streamExchange = hostConnection.getStreamExchange();
        removeLastLoginInfo(streamExchange);
        //第一个节点直接返回
        if (loginNodeIndex == 0) return null;
        login(hostConnectionNodeList.get(loginNodeIndex), streamExchange, 0);
        return null;
    }

    private void removeLastLoginInfo(StreamExchange streamExchange) {
        FinishOneExchangeListener loginSuccessListener = new FinishOneExchangeListener();
        ExchangeObserver exchangeObserver = streamExchange.getExchangeObserver();
        loginSuccessListener.refreshPattern("Last login");
        exchangeObserver.register(loginSuccessListener);

        try {
            loginSuccessListener.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException e) {
            return;
        }
        streamExchange.reset();
    }

    public static boolean login(HostConnectionNode hostConnectionNode,
                                StreamExchange streamExchange, int level) throws InterruptedException, TimeoutException, IOException {

        ExchangeObserver exchangeObserver = streamExchange.getExchangeObserver();
        FinishOneExchangeListener loginSuccessListener = new FinishOneExchangeListener();
        FinishOneExchangeListener fingerListener = new FinishOneExchangeListener();
        FinishOneExchangeListener passwordWaitInputListener = new FinishOneExchangeListener();
        FinishOneExchangeListener passwordExpireListener = new FinishOneExchangeListener();
        FinishOneExchangeListener passwordErrorListener = new FinishOneExchangeListener();
        loginSuccessListener.refreshPattern("Last login");
        fingerListener.refreshPattern(Pattern.compile("fingerprint", Pattern.DOTALL) );
        passwordWaitInputListener.refreshPattern("[p|P]assword:");
        passwordExpireListener.refreshPattern("Your password has expired");
        passwordErrorListener.refreshPattern("Permission denied");
        exchangeObserver.register(loginSuccessListener);
        exchangeObserver.register(fingerListener);
        exchangeObserver.register(passwordWaitInputListener);
        exchangeObserver.register(passwordExpireListener);
        exchangeObserver.register(passwordErrorListener);
        try {
            streamExchange.write(hostConnectionNode.toString());
            if (loginSuccessListener.hasFinish()) {
                return true;
            }
            if (hostConnectionNode.getKeyLocation() == null) {
                int retryTime = 10;
                while (retryTime -- > 0) {
                    try {
                        passwordWaitInputListener.await(1, TimeUnit.SECONDS);
                        break;
                    } catch (TimeoutException timeoutException) {
                        if (fingerListener.hasFinish() && level < 3) {
                            streamExchange.reset();
                            streamExchange.write("yes\n");
                            return login(hostConnectionNode, streamExchange, level+1);
                        }

                        if (passwordExpireListener.hasFinish()) {
                            throw new ConnectionException("密码已过期,节点[%s]".formatted(hostConnectionNode.toString()));
                        }

                    }
                }
                if (retryTime <= 0) {
                    throw new ConnectionException("登录节点[%s]失败，错误信息：%s".formatted(hostConnectionNode.toString(), streamExchange.getPrintContent()));
                }
                passwordWaitInputListener.reset();
                streamExchange.reset();
                streamExchange.write(hostConnectionNode.getPassword());
            }
            loginSuccessListener.await(15, TimeUnit.SECONDS);

            if (passwordExpireListener.hasFinish()) {
                throw new ConnectionException("密码已过期,节点[%s]".formatted(hostConnectionNode.toString()));
            }
            return true;
        } catch (TimeoutException e) {
            if (passwordExpireListener.hasFinish()) {
                throw new ConnectionException("密码已过期,节点[%s]".formatted(hostConnectionNode.toString()));
            } else if (fingerListener.hasFinish()) {
                streamExchange.reset();
                streamExchange.write("yes\n");
                return login(hostConnectionNode, streamExchange, level+1);
            } else if (passwordErrorListener.hasFinish()) {
                throw new ConnectionException("密码错误,节点[%s]".formatted(hostConnectionNode.toString()));
            }
            else if (passwordWaitInputListener.hasFinish()) {
                return login(hostConnectionNode, streamExchange, level+1);
            } else {
                throw new ConnectionException("跳板登录超时,节点[%s]".formatted(hostConnectionNode.toString()));
            }
        }
        finally {
            exchangeObserver.remove(loginSuccessListener);
            exchangeObserver.remove(fingerListener);
            exchangeObserver.remove(passwordWaitInputListener);
            exchangeObserver.remove(passwordExpireListener);
            exchangeObserver.remove(passwordErrorListener);
        }
    }

}
