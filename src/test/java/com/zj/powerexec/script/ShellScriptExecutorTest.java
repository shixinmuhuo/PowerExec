package com.zj.powerexec.script;

import com.zj.powerexec.Constants;
import com.zj.powerexec.connection.HostConnection;
import com.zj.powerexec.connection.HostConnector;
import com.zj.powerexec.connection.StreamExchange;
import com.zj.powerexec.connection.ssh.SSHConnectorImpl;
import com.zj.powerexec.host.Host;
import com.zj.powerexec.host.HostHelper;
import com.zj.powerexec.observer.ExchangeObserver;
import com.zj.powerexec.observer.FinishOneExchangeListener;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

class ShellScriptExecutorTest {

    @Test
    void execBuildDownloader() {
        String loginScript = """
                [server]
                ssh root@xxxxx
                Password:xxxxx
                """;

        String execScript = """
                cd /data/PowerExec
                sh build.sh
                """;
        Host host = HostHelper.parseHost(loginScript).get(0);

        Map<String, Object> params = new HashMap<>();
        params.put(Constants.SCRIPT, execScript);
        ShellScriptExecutor scriptExecutor = new ShellScriptExecutor(params) {
            @Override
            public void exitCondition(HostConnection hostConnection) throws InterruptedException, TimeoutException {
                StreamExchange streamExchange =  hostConnection.getStreamExchange();
                FinishOneExchangeListener execSuccessListener = new FinishOneExchangeListener();
                ExchangeObserver exchangeObserver = streamExchange.getExchangeObserver();
                execSuccessListener.refreshPattern(Pattern.compile("lastest") );
                exchangeObserver.register(execSuccessListener);
                execSuccessListener.await(3, TimeUnit.MINUTES);
            }
        };
        HostConnector hostConnector = new SSHConnectorImpl();
        try (HostConnection hostConnection = hostConnector.connect(host)){
            String rsp = scriptExecutor.exec(hostConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    void execRestartDownloader() {
        String loginScript = """
                [server]
                ssh xxxx@192.168.0.1
                Password:123456
                ssh xxxx@xxxxx
                password xxxxxx
                ssh root@node2
                password xxxxxx
                """;

        String execScript = """
                cd /data/PowerExec
                sh redeploy.sh
                """;
        Host host = HostHelper.parseHost(loginScript).get(0);

        Map<String, Object> params = new HashMap<>();
        params.put(Constants.SCRIPT, execScript);
        ShellScriptExecutor scriptExecutor = new ShellScriptExecutor(params) {
            @Override
            public void exitCondition(HostConnection hostConnection) throws InterruptedException, TimeoutException {
                StreamExchange streamExchange =  hostConnection.getStreamExchange();
                FinishOneExchangeListener execSuccessListener = new FinishOneExchangeListener();
                ExchangeObserver exchangeObserver = streamExchange.getExchangeObserver();
                execSuccessListener.refreshPattern(Pattern.compile("容器PowerExec启动") );
                exchangeObserver.register(execSuccessListener);
                execSuccessListener.await(2, TimeUnit.MINUTES);
            }
        };
        HostConnector hostConnector = new SSHConnectorImpl();
        try (HostConnection hostConnection = hostConnector.connect(host)){
            String rsp = scriptExecutor.exec(hostConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}