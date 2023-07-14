package com.zj.powerexec.script;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.thread.NamedThreadFactory;
import com.zj.powerexec.host.Host;
import com.zj.powerexec.host.HostHelper;
import com.zj.powerexec.task.ExecScriptTask;

import com.zj.powerexec.task.BlockRejectedExecutionHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chenzhijie
 * @date 2023-07-11
 **/
@Slf4j
public class ExecHelper {

    public static ThreadPoolExecutor createThreadExecutor(int concurrent) {
        return new ThreadPoolExecutor(concurrent,
                concurrent,
                1,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new NamedThreadFactory("execScriptTask", true),
                new BlockRejectedExecutionHandler()
                );
    }

    public static void execTask(int concurrent, String hostSrc, String scriptSrc) {

        long startTime = System.currentTimeMillis();
        ThreadPoolExecutor threadPoolExecutor = createThreadExecutor(concurrent);
        List<Host> hostList = HostHelper.parseHost(hostSrc);
        List<ScriptExecutor> scriptExecutorList = ScriptHelper.parseScript(scriptSrc);
        //完成的任务计数器
        AtomicInteger finishCounter = new AtomicInteger();
        //当前运行的任务；
        AtomicInteger runningGauge = new AtomicInteger();
        List<Future<String>> futures = new ArrayList<>();
        Object lock = new Object();
        for (Host host : hostList) {
            Future<String> future = threadPoolExecutor.submit(
                    () ->{
                        runningGauge.incrementAndGet();
                        try {
                            return new ExecScriptTask(host, scriptExecutorList).call();
                        } finally {
                            synchronized (lock) {
                                runningGauge.decrementAndGet();
                                finishCounter.incrementAndGet();
                                log.info("host[%s]任务执行结束,当前正在执行的任务数:%d".formatted(host.getHostName(), runningGauge.get()));
                                log.info("当前进度：%d/%d".formatted(finishCounter.get(), hostList.size()));
                            }
                        }
                    });
            futures.add(future);
        }

        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("\n##################################start##################################\n");
        int successCounter = 0;
        for (int i = 0 ; i < futures.size();i++) {
            Future<String> future = futures.get(i);
            Host host = hostList.get(i);
            try {
                logBuilder.append("[%s]\n".formatted(host.getHostName()));
                String rsp = future.get();
                logBuilder.append(rsp);
                successCounter++;
            } catch (ExecutionException e) {
                logBuilder.append("执行异常:").append(ExceptionUtil.stacktraceToString(e));
            } catch (InterruptedException e) {
                return;
            }
            logBuilder.append("\n\n");
        }
        logBuilder.append("执行任务的设备共有%d台,执行成功的有%d台,总耗时为%d秒".
                formatted(hostList.size(), successCounter, (System.currentTimeMillis()-startTime)/1000));
        logBuilder.append("\n##################################end##################################");
        log.info(logBuilder.toString());
    }

}
