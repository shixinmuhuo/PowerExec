package com.zj.powerexec.script;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScriptHelperTest {

    @Test
    void parseScript() {
        String script = """
                [ShellScriptExecutor]
                script= cd /data
                [ConditionShellScriptExecutor]
           
                script= cd /data/PowerExec \\
                        sh build.sh
                timeout = 2m
                pattern = latest
                """;
        List<ScriptExecutor> scriptExecutorList = ScriptHelper.parseScript(script);
        assertEquals(2, scriptExecutorList.size());
        assertEquals(ShellScriptExecutor.class, scriptExecutorList.get(0).getClass());
        assertEquals(ConditionShellScriptExecutor.class, scriptExecutorList.get(1).getClass());

    }
}