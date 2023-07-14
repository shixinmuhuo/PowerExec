package com.zj.powerexec.observer;

import com.zj.powerexec.connection.StreamExchange;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * ExchangeOberser
 *
 * @author chenzhijie
 * @date 2023/3/28
 */
public class ExchangeObserver implements ExchangeListener {

    private StreamExchange streamExchange;

    public ExchangeObserver(StreamExchange streamExchange) {
        this.streamExchange = streamExchange;
    }

    private final Set<ExchangeListener> exchangeListenerSet = new CopyOnWriteArraySet<>();

    public void register(ExchangeListener exchangeListener) {
        if (exchangeListener instanceof StreamExchangeAware) {
            ((StreamExchangeAware)exchangeListener).setStreamExchange(streamExchange);
        }
        exchangeListenerSet.add(exchangeListener);
    }

    public void reNotify() {
        streamExchange.reNotify();
    }

    public void remove(ExchangeListener exchangeListener) {
        exchangeListenerSet.remove(exchangeListener);
    }

    @Override
    public void onNotify(String str) {
        exchangeListenerSet.forEach(e -> e.onNotify(str));
    }

    @Override
    public void onError(Exception ee) {
        exchangeListenerSet.forEach(e -> e.onError(ee));
    }
}
