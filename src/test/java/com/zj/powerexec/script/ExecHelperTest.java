package com.zj.powerexec.script;

import org.junit.jupiter.api.Test;

class ExecHelperTest {

    @Test
    void execTask() {

        String hostSrc = """
                [server]
                ssh xxxx@xxxxxxxx
                Password:xxxxxx
                [test-server]
                ssh xxxxx@xxxxxxx
                Password:xxxxxx
                """;
        String scriptSrc = """
                [ShellScriptExecutor]
                output=true
                script= cd /data && pwd
                
                """;
        ExecHelper.execTask(2, hostSrc, scriptSrc);
    }

    @Test
    void execRedeployTask() {

        String hostSrc = """
                [node1]
                ssh test@xxxxx
                Password:xxxxxxx
                ssh root@node1
                Password:xxxxxx
                [node2]
                ssh root@xxxx
                Password:xxxxxx
                ssh root@node2
                Password:xxxxxxx
                """;
        String scriptSrc = """
                [ConditionShellScriptExecutor]
                script= cd /data/PowerExec && sh redeploy.sh
                timeout = 3m
                pattern=
                """;
        ExecHelper.execTask(2, hostSrc, scriptSrc);
    }
}