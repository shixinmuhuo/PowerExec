package com.zj.powerexec;

import java.util.regex.Pattern;

/**
 * Constants
 *
 * @author chenzhijie
 * @date 2023/3/20
 */
public interface Constants {

    String SCRIPT = "script";
    String PATTERN = "pattern";
    String TIMOUT = "timeout";
    String OUTPUT = "output";
    Pattern TIMEOUT_PATTERN = Pattern.compile("(\\d+)([s|m|M|h|d])");
    String SCRIPT_PATH = "script_path"; //脚本存放路径
    String HOST_PATH = "host_path"; //主机信息存放
    String MAX_CONCURRENT = "max_concurrent"; //最大并行
    String CHARSET = "charset";
    String LOGIN_NODE_INDEX = "login_node_index";
}
