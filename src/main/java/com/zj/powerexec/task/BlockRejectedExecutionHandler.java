package com.zj.powerexec.task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author chenzhijie
 * 线程池阻塞拒绝策略
 * @date 2022-11-12
 **/
public class BlockRejectedExecutionHandler implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        BlockingQueue<Runnable> workQueen = executor.getQueue();
        try {
            workQueen.put(r);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
