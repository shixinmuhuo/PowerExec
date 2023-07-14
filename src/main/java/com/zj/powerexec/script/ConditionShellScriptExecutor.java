package com.zj.powerexec.script;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReUtil;
import com.zj.powerexec.Constants;
import com.zj.powerexec.connection.HostConnection;
import com.zj.powerexec.connection.StreamExchange;
import com.zj.powerexec.observer.ExchangeObserver;
import com.zj.powerexec.observer.FinishOneExchangeListener;
import com.zj.powerexec.utils.MyStrUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/**
 * @author chenzhijie
 * @date 2023-07-12
 **/
public class ConditionShellScriptExecutor extends ShellScriptExecutor {

    private final int timeout;
    private final TimeUnit timeUnit;
    private final String pattern;

    public ConditionShellScriptExecutor(Map<String, Object> params) {
        super(params);
        this.pattern = (String) params.get(Constants.PATTERN);
        String timeoutStr = (String) params.get(Constants.TIMOUT);
        List<String> timeouts = ReUtil.getAllGroups(Constants.TIMEOUT_PATTERN, timeoutStr);
        Assert.isTrue(timeouts.size() == 3, "timeout string[%s] invalid".formatted(timeoutStr));
        this.timeout = Integer.parseInt(timeouts.get(1));
        this.timeUnit = MyStrUtil.toTimeUnit(timeouts.get(2));
    }


    @Override
    public void exitCondition(HostConnection hostConnection) throws InterruptedException, TimeoutException {
        StreamExchange streamExchange =  hostConnection.getStreamExchange();
        FinishOneExchangeListener execSuccessListener = new FinishOneExchangeListener();
        ExchangeObserver exchangeObserver = streamExchange.getExchangeObserver();
        execSuccessListener.refreshPattern(Pattern.compile(pattern) );
        exchangeObserver.register(execSuccessListener);
        execSuccessListener.await(timeout, timeUnit);
    }
}
