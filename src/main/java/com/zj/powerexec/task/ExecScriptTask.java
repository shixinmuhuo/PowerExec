package com.zj.powerexec.task;

import com.zj.powerexec.connection.HostConnection;
import com.zj.powerexec.host.Host;
import com.zj.powerexec.host.HostHelper;
import com.zj.powerexec.script.ScriptExecutor;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author chenzhijie
 * @date 2023-07-10
 **/
public class ExecScriptTask implements Callable<String> {

    final Host host;
    final List<ScriptExecutor> scriptExecutorList;

    protected StringBuilder logBuilder = new StringBuilder();

    public ExecScriptTask(Host host, List<ScriptExecutor> scriptExecutorList) {
        this.host = host;
        this.scriptExecutorList = scriptExecutorList;
    }


    @Override
    public String call() throws Exception {
        long startTime = System.currentTimeMillis();
        try (HostConnection hostConnection = HostHelper.createConnection(host)){
            for (ScriptExecutor scriptExecutor : scriptExecutorList) {
                logBuilder.append("executor[%s]\n".formatted(scriptExecutor.getClass().getSimpleName()));
                String rsp = scriptExecutor.exec(hostConnection);
                logBuilder.append(rsp).append("\n");
            }
        }

        long cost = System.currentTimeMillis() - startTime;
        logBuilder.append("host[%s] exec success, cost %s mill".formatted(host.getHostName(), cost));
        return logBuilder.toString();
    }

}
