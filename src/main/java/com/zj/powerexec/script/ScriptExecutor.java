package com.zj.powerexec.script;

import com.zj.powerexec.connection.HostConnection;

import java.util.Collections;
import java.util.Map;

/**
 * @author chenzhijie
 * @cdate 2023-07-11
 **/
public abstract class ScriptExecutor {

    //禁止修改里面的数据
    protected Map<String, Object> params;

    public ScriptExecutor(Map<String, Object> params) {
        if (params != null) {
            this.params = Collections.unmodifiableMap(params);
        }
    }

    public abstract String exec(HostConnection hostConnection) throws Exception;
}
