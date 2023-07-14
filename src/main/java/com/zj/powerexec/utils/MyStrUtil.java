package com.zj.powerexec.utils;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author chenzhijie
 * @date 2023-07-10
 **/
public class MyStrUtil {

    public static List<String> strBlockSplit(String str) {
        if (StrUtil.isBlank(str)) return null;
        StringBuilder tempBuilder = new StringBuilder();
        //双引号
        boolean isStartDqm = false;
        boolean isEscape = false;
        List<String> rspList = new ArrayList<>();
        for (int i=0;i<str.length();i++) {
            char c = str.charAt(i);

            if (isEscape) {
                tempBuilder.append(c);
                isEscape = false;
            } else if (c == '\\') {
                tempBuilder.append(c);
                isEscape = true;
            }
            else if (c == '"') {
                if (isStartDqm){
                    String value = tempBuilder.toString();
                    tempBuilder.delete(0, tempBuilder.length());
                    rspList.add(value);
                }
                isStartDqm = !isStartDqm;

            }
            else if (isStartDqm) {
                tempBuilder.append(c);
            }
            else if (checkSpaceChar(c) && !tempBuilder.isEmpty()) {
                String value = tempBuilder.toString();
                tempBuilder.delete(0, tempBuilder.length());
                rspList.add(value);
            } else {
                tempBuilder.append(c);
            }
        }

        if (!tempBuilder.isEmpty()) {
            rspList.add(tempBuilder.toString());
        }
        return rspList;
    }

    public static boolean checkSpaceChar(char c) {
        return c == ' ' || c == '\t' || c == '\n';
    }

    public static TimeUnit toTimeUnit(String waitUnitStr) {
        return switch (waitUnitStr) {
            case "s" -> TimeUnit.SECONDS;
            case "m" -> TimeUnit.MINUTES;
            case "h" -> TimeUnit.HOURS;
            case "d" -> TimeUnit.DAYS;
            case "M" -> TimeUnit.MILLISECONDS;
            default -> null;
        };
    }
}
