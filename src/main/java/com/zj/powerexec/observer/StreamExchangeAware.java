package com.zj.powerexec.observer;


import com.zj.powerexec.connection.StreamExchange;

/**
 * StreamExchageAware
 *
 * @author chenzhijie
 * @date 2023/4/11
 */
public interface StreamExchangeAware {

    void setStreamExchange(StreamExchange streamExchange);
}
