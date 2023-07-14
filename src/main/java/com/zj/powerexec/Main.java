package com.zj.powerexec;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReUtil;
import com.zj.powerexec.script.ExecHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author chenzhijie
 * @date 2023-07-07
 **/
@Slf4j
public class Main {

    static Pattern argPattern = Pattern.compile("--(\\w+)=(\\S+)");
    public static void main(String[] args) {
        Map<String, String> params = argsToMap(args);
        try {
            run(params);
        } catch (Exception e) {
            log.error("run error:", e);
        }
    }

    private static Map<String, String> argsToMap(String[] args) {
        Map<String, String> params = new LinkedHashMap<>();
        for (String arg : args) {
            if (ReUtil.isMatch(argPattern, arg)) {
                List<String> groups = ReUtil.getAllGroups(argPattern, arg);
                params.put(groups.get(1), groups.get(2));
            }
        }
        return params;
    }

    private static void run(Map<String, String> params) throws Exception{
        String charset = params.get(Constants.CHARSET);
        if (charset != null) {
            System.setProperty(Constants.CHARSET, charset);
        }
        String maxConcurrent = params.get(Constants.MAX_CONCURRENT);
        int concurrent = 1;
        if (maxConcurrent != null) {
            concurrent = Integer.parseInt(maxConcurrent);
        }
        log.debug("concurrent set to [{}]", concurrent);
        Assert.notNull(params.get(Constants.HOST_PATH), "host_path must not bu null");
        String hostSrc = IoUtil.readUtf8(new FileInputStream(params.get(Constants.HOST_PATH)));
        Assert.notNull(params.get(Constants.SCRIPT_PATH), "script+path must not be null");
        String scriptSrc = IoUtil.readUtf8(new FileInputStream(params.get(Constants.SCRIPT_PATH)));

        ExecHelper.execTask(concurrent, hostSrc, scriptSrc);
    }
}
