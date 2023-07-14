package com.zj.powerexec.script;

import com.zj.powerexec.Constants;
import com.zj.powerexec.connection.HostConnection;
import com.zj.powerexec.connection.StreamExchange;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author chenzhijie
 * @date 2023-07-11
 **/
public class ShellScriptExecutor extends ScriptExecutor {

    private final String script;
    private final boolean outputOpen;

    public ShellScriptExecutor(Map<String, Object> params) {
        super(params);
        this.script = params.get(Constants.SCRIPT).toString();
        Object output = params.get(Constants.OUTPUT);
        if (output != null && output.toString().toLowerCase(Locale.ROOT).equals("true")) {
            outputOpen = true;
        } else {
            outputOpen = false;
        }
    }

    @Override
    public String exec(HostConnection hostConnection) throws Exception {
        StreamExchange streamExchange = hostConnection.getStreamExchange();
        streamExchange.write(script);
        exitCondition(hostConnection);
        if (outputOpen) {
            return streamExchange.getPrintContent();
        } else {
            streamExchange.reset();
            return "";
        }
    }

    protected void exitCondition(HostConnection hostConnection) throws InterruptedException, TimeoutException {};
}
