package com.zj.powerexec.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

class MyStrUtilTest {

    @Test
    void strBlockSplit() {
        String src = "$output match $loginSuccess”";
        List<String> rspList = MyStrUtil.strBlockSplit(src);
        System.out.println(rspList);
    }
}