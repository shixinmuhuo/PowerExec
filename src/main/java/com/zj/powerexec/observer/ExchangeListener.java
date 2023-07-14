package com.zj.powerexec.observer;

/**
 * ExchangeListenner
 *
 * @author chenzhijie
 * @date 2023/3/28
 */
public interface ExchangeListener {

    void onNotify(String str);

    void onError(Exception e);
}
