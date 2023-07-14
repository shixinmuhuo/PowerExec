# PowerExec

### 介绍

PowerExec是一个超强的支持无限跳板的远程执行脚本的工具。致力于解决重复繁琐的手工运维问题。

* 支持无限次的跳板来登录远程服务器
* 支持ssh，telnet等协议的混搭跳板
* 支持以人机交互的方式来执行脚本
* 支持多线程并行执行运维任务

### 适合人员

如果你有以下类似需要你可以尝试使用该工具来完成您的协助您的工作

* 您需要通过一次或多次跳板到目标设备执行设备命令
* 登录设备后，您需要执行一些登录账号密码的操作。或者其他需要进行人机交互的工作
* 您需要并行的登录多台设备执行以上的任务

其他相关需要有待开发，我给您预留了足够大的扩展空间，由于代码量不大，很轻松就能阅读完成，对于需要学习java的小伙伴也不失为一个合适的练手小程序。

### 安装要求

* java17    

### 快速使用

##### 独立编译代码

```
git clone https://github.com/shixinmuhuo/PowerExec.git
或者 
git clone https://gitee.com/zhijiegege/power-exec.git
mvn clean package -Dmaven.test.skip=true   
```

##### 配置执行脚本

```
java -Dlog4j.configuration=file:./log4j.properties \
-jar PowerExec-1.0.jar \
--host_path=host.conf \
--script_path=deploy.script \
--max_concurrent=10
```

##### 参数说明

| 参数名                | 描述                                           |
| --------------------- | ---------------------------------------------- |
| -Dlog4j.configuration | 指定日志配置文件路径，可空，使用默认的内置配置 |
| -jar                  | 指定jar包的位置                                |
| --host_path           | 指定主机配置的绝对路径                         |
| --script_path         | 指定脚本配置的绝对路径                         |
| --max_concurrent      | 指定最大并行执行数                             |



#### 配置主机信息

#####  配置格式,${name}为配置变量

```
[${nodeName}]
${protocol} ${username}@${address} -p ${port}
password:${password}
keyLocation: ${keyLocation}
```


##### 参数说明

| 名称        | 值                   | 描述                                                 |
| ----------- | -------------------- | ---------------------------------------------------- |
| nodeName    | 字符串，必填         | 节点的名称，标识用，建议和目标设备的hostname保持一致 |
| protocol    | 枚举，ssh｜telnet    | 远程连接协议，支持ssh和telnet                        |
| username    | 字符串，必填         | 登录用户名                                           |
| password    | 字符串，免密登录为空 | 登录密码                                             |
| address     | ip或者主机名         | 登录地址                                             |
| port        | 数值                 | 登录端口                                             |
| keyLocation | 路径                 | 免密登录指定密钥位置                                 |

##### 直连配置案例

```
[test]
ssh test@192.168.0.100 -p 22   #-p 22 可以缺省，默认ssh为22端口telnet为23端口
password: 123456
[test2]
ssh test2@192.168.0.101 
password: 123456
[test3]
telnet test2@192.168.0.102
password: 123456
[test4]
ssh test2@192.168.0.103    
keyLocation:/root/.ssh/id_rsa  #免密登录指定密钥位置
```

##### 跳板登录，ssh和telnet可以混搭跳板

```
[test3]
#跳板节点
ssh test@192.168.0.100 
password:123456
#目标节点
ssh test2@192.168.0.101
password:123456
[test4]
#一级跳板节点
ssh test@192.168.0.100 
password:123456
#二级跳板节点
ssh test2@192.168.0.101
password:123456
#目标节点
telnet test3@192.168.0.102
password:123456
```


#### 配置执行脚本

##### 配置格式

```
[${scriptExecutorName}]
output=${output}
script=${script}
timeout=${timeoutNumber}${timeoutUnit}
pattern=${pattern}
```

##### 参数说明

| 名称               | 值                                                     | 描述                                                         |
| ------------------ | :----------------------------------------------------- | ------------------------------------------------------------ |
| scriptExecutorName | 字符串，必填                                           | 代码中脚本执行器的名称，支持ShellScriptExecutor和ConditionShellScriptExecutor |
| output             | 布尔值，true\|false，缺省false                         | 配置执行过程的输出是否打印出来                               |
| script             | 脚本，必填                                             | 执行的脚本，如果有多行则使用反斜杠\,类似于linux控制台执行多行命令 |
| timeoutNumber      | 数值，必填                                             | 超时数，比如超时3秒的超时数是3                               |
| timeoutUnit        | 枚举，M\|s\|m\|h\|d  => 毫秒｜秒｜分钟｜小时｜天，必填 | 超时时间单位                                                 |
| pattern            | 正则表达式，必填                                       | 检查结果的正则表达式                                         |

注：

* 以上的参数说明只针对ConditionShellScriptExecutor执行器，
* ShellScriptExecutor执行器的output参数没有意义，它执行完了还没来得及接受流数据就退出了
* 有特殊需求可以自行编写执行器 

##### 配置案例

执行一个简单的ip a脚本

```
[ConditionShellScriptExecutor]
output=true #配置获取输出内容
script=ip a #配置脚本
timeout=3s #配置3秒超时
pattern=127.0.0.1 #配置检查执行成功的正则表达式，当检查到输出匹配到这段表达式后程序才能退出，否则超时
```

执行登录数据库并且执行一条sql的脚本

```
#执行登录数据库的脚本，检查到password字样进入下一段脚本
[ConditionShellScriptExecutor]
script= mysql -u root -p
timeout = 2s
pattern = password
#输入密码123456，直到mysql>出现
[ConditionShellScriptExecutor]
script= 123456
timeout = 2s
pattern = mysql>
#执行show databases;直到mysql>出现
[ConditionShellScriptExecutor]
output=true
script= show databases;
timeout = 2s
pattern = mysql>
```



