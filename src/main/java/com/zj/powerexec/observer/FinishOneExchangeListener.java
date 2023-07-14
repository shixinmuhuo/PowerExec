package com.zj.powerexec.observer;

import cn.hutool.core.util.ReUtil;
import com.zj.powerexec.connection.StreamExchange;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * FinishOneExchangeListener
 *
 * @author chenzhijie
 * @date 2023/3/28
 */
@Slf4j
public class FinishOneExchangeListener implements ExchangeListener, StreamExchangeAware {

    volatile CountDownLatch countDownLatch;

    volatile Predicate<String> predicate;

    volatile Exception exception;

    private StreamExchange streamExchange;

    public FinishOneExchangeListener() {

    }

    public FinishOneExchangeListener(Pattern pattern) {
        refreshPattern(pattern);
    }

    public void refreshPattern(String pattern) {
        refreshPattern(Pattern.compile(pattern));
    }
    public void refreshPattern(Pattern pattern) {
        refreshPredict(s -> ReUtil.contains(pattern, s));
    }

    public void refreshPredict(Predicate<String> predicate) {
        this.predicate = predicate;
        this.countDownLatch = new CountDownLatch(1);
        this.exception = null;

    }
    @Override
    public void onNotify(String str) {
        if (predicate == null) {
            return;
        }
        if (predicate.test(str)) {
            CountDownLatch countDownLatch = this.countDownLatch;
            countDownLatch.countDown();
//            log.debug("触发了countdown:"+str);
        }
    }

    @Override
    public void onError(Exception e) {

        this.exception = e;
        countDownAll();
    }

    public void await() throws InterruptedException {
        CountDownLatch countDownLatch = this.countDownLatch;
        countDownLatch.await();
    }

    public void countDownAll() {
        CountDownLatch countDownLatch = this.countDownLatch;
        long count = countDownLatch.getCount();
        for (int i=0;i<count;i++) {
            countDownLatch.countDown();
        }
    }

    public void await(long timeout, TimeUnit unit ) throws InterruptedException, TimeoutException {
        CountDownLatch countDownLatch = this.countDownLatch;
        boolean result = countDownLatch.await(timeout, unit);
        if (!result) {
            throw new TimeoutException();
        }

        if (exception != null) {
            throw new RuntimeException(exception);
        }
    }

    public void reset() {
        this.countDownLatch = new CountDownLatch(1);
    }

    public boolean hasFinish() {
        return countDownLatch.getCount() == 0;
    }

    public void reNotify() {
        streamExchange.reNotify(this);
    }

    @Override
    public void setStreamExchange(StreamExchange streamExchange) {
        this.streamExchange = streamExchange;
    }
}
