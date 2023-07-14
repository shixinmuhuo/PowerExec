package com.zj.powerexec.script;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenzhijie
 * @date 2023-07-12
 **/
public class ScriptHelper {

    public static List<ScriptExecutor> parseScript(String script) {
        List<ScriptExecutor> scriptExecutorList = new ArrayList<>();
        StringBuilder valueBuilder = new StringBuilder();
        String []lines = script.split("\n");
        String execName = null;
        Map<String, Object> params = new HashMap<>();
        for (int i = 0 ; i < lines.length; i ++) {
            String line = lines[i];
            line = StrUtil.trim(line);
            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("[")){
                if (execName != null) {
                    if (!valueBuilder.isEmpty()) {
                        putValue(params, valueBuilder);
                    }
                    ScriptExecutor scriptExecutor = createScriptExecutor(execName, params);
                    scriptExecutorList.add(scriptExecutor);
                    params.clear();
                }
                execName = ReUtil.getGroup1("\\[(.*)]", line);
            } else if (line.endsWith("\\")) {
                valueBuilder.append(line);
            } else {
                valueBuilder.append(line);
                putValue(params, valueBuilder);
            }
        }

        if (execName != null) {
            if (!valueBuilder.isEmpty()) {
                putValue(params, valueBuilder);
            }
            ScriptExecutor scriptExecutor = createScriptExecutor(execName, params);
            scriptExecutorList.add(scriptExecutor);
            params.clear();
        }
        return scriptExecutorList;
    }

    private static void putValue(Map<String, Object> params, StringBuilder kvBuilder) {
        String kv = kvBuilder.toString();
        Assert.isTrue(kv.contains("="), "参数不合法[%s],必须是key=name的格式".formatted(kv));
        String k = kv.substring(0,kv.indexOf("="));
        String v = kv.substring(kv.indexOf("=")+1);
        params.put(StrUtil.trim(k), StrUtil.trim(v));
        kvBuilder.delete(0, kvBuilder.length());
    }

    private static ScriptExecutor createScriptExecutor(String execName, Map<String, Object> params) {
        return switch (execName) {
            case "ShellScriptExecutor" -> new ShellScriptExecutor(params);
            case "ConditionShellScriptExecutor" -> new ConditionShellScriptExecutor(params);
            default -> throw new IllegalStateException("Unexpected value: " + execName);
        };
    }
}
