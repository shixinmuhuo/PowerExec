package com.zj.powerexec.exceptions;


/**
 * SshConnectionException
 *
 * @author chenzhijie
 * @date 2022/6/8
 */
public class ConnectionException extends RuntimeException {

    public ConnectionException(String message) {
        super(message);
    }
}
