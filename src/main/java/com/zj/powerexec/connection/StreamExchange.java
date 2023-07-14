package com.zj.powerexec.connection;

import cn.hutool.core.util.StrUtil;
import com.zj.powerexec.Constants;
import com.zj.powerexec.observer.ExchangeListener;
import com.zj.powerexec.observer.ExchangeObserver;
import com.zj.powerexec.utils.ByteCharTransferUtil;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * StreamExchange
 *
 * @author chenzhijie
 * @date 2023/4/8
 */
@Getter
@Slf4j
public class StreamExchange implements Runnable, Closeable {

    private final InputStream inputStream;

    private final OutputStream outputStream;

    private final InputStream errInputStream;

    private final StringBuffer stringBuffer = new StringBuffer();

    private final AtomicBoolean running;

    private final ExchangeObserver exchangeObserver;

    private final PrintWriter printWriter;

    private final String key;

    private final Charset charset;

    private final Thread mainThread;

    //最大的保存的临时输出长度
    private final int maxTempPrintLength;

    private final Object lock = new Object();

    public StreamExchange(Process process, String key) {
        this(process.getInputStream(), process.getErrorStream(), process.getOutputStream(), key);
    }

    public StreamExchange(Process process, String key, int maxTempPrintLength) {
        this(process.getInputStream(), process.getErrorStream(), process.getOutputStream(), key, maxTempPrintLength);
    }

    public StreamExchange(InputStream inputStream, InputStream errInputStream, OutputStream outputStream, String key)  {
        this(inputStream, errInputStream, outputStream, key, 5000);
    }

    public StreamExchange(InputStream inputStream, InputStream errInputStream, OutputStream outputStream, String key, int maxTempPrintLength) {
        this.exchangeObserver = new ExchangeObserver(this);
        this.inputStream = inputStream;
        this.errInputStream = errInputStream;
        this.printWriter = new PrintWriter(outputStream);
        this.outputStream = outputStream;
        this.key = key;
        this.mainThread = new Thread(this, "BaseDataExchange-"+ System.currentTimeMillis());
        this.running = new AtomicBoolean(true);
        if (maxTempPrintLength < 0) {
            maxTempPrintLength = Integer.MAX_VALUE;
        }
        this.maxTempPrintLength = maxTempPrintLength;

        String charsetName = System.getProperty(Constants.CHARSET);
        if (charsetName != null) {
            charset = Charset.forName(System.getProperty(Constants.CHARSET));
        } else {
            charset = StandardCharsets.UTF_8;
        }
    }

    @Override
    public void run() {
        if (inputStream == null) {
            log.error("输入流为空");
            return;
        }
        try {
            byte []buf = new byte[1024*1024];
            int n = 0;
            while (true) {
                if (!running.get()) return;
                if (mainThread.isInterrupted()) return;
                StringBuilder stringBuilder = new StringBuilder();

                //实践发现，同一行数据在传输过程中可能是断断续续的，available的数量为0并不代表后面没有数据了
                //所以为了保证输出到观察者的数据是完整的一行需要额外等那么一会,做法就是设置一个状态标识上一次有没有数据，没有数据
                //就等待一会儿，大概十毫秒，再进入下一个循环周期，直到超过最大空转次数，如果此时还是没有数据，
                // 那应该是没有了，数据不会断这么长。
                int cycleIndex = 0;
                int maxCycleTime = 2;
                boolean hasDataStatus = false;
                while (true) {

                    if (mainThread.isInterrupted()) {
                        return;
                    }

                    //读取有效数据
                    if (inputStream.available() > 0) {
                        n = inputStream.read(buf, 0, inputStream.available());
                    } else if (errInputStream != null && errInputStream.available() > 0) {
                        n = errInputStream.read(buf, 0, errInputStream.available());
                    }

                    if (n > 0) {
                        char[] newChars;
                        newChars = ByteCharTransferUtil.byteToChar(Arrays.copyOf(buf, n), charset);
//                        log.debug("收到外部数据长度:{}", newChars.length);
                        stringBuilder.append(newChars);
                        hasDataStatus = true;
                        cycleIndex = 0;
                        n = 0;
                        continue;
                    }

                    //执行空转
                    if (cycleIndex < maxCycleTime) {
                        Thread.sleep(10);
                        cycleIndex++;
                    } else {
                        break;
                    }
                }

                if (!hasDataStatus) {
                    Thread.sleep(100);
                } else {
                    String line = stringBuilder.toString();
                    line = line.replace("\r", "");

                    if (maxTempPrintLength > 0 ) {
                        synchronized (lock) {
                            if (stringBuffer.length() >= maxTempPrintLength) {
                                stringBuffer.delete(0, Math.min(line.length(), stringBuffer.length()));
                            }
                            stringBuffer.append(line);
                        }
                    }
                    log.debug(line); //调试用的
                    exchangeObserver.onNotify(line);
                }
            }
        } catch (IOException e) {
            exchangeObserver.onError(e);
            running.set(false);
            if (mainThread.isInterrupted()) {
                return;
            }
            log.error("io 异常退出:{},{},{}" , key, e.getClass().getSimpleName(), e.getMessage());
        } catch (InterruptedException e) {
            log.debug("io 正常退出:{}" , key);
        }

        catch (Exception e) {
            log.error("io循环异常退出", e);
        }
    }


    public void reNotify() {
        String tempData = stringBuffer.toString();
        if (StrUtil.isNotBlank(tempData)) {
            exchangeObserver.onNotify(tempData);
        }
    }

    public void reNotify(ExchangeListener exchangeListener) {
        String tempData = stringBuffer.toString();
        if (StrUtil.isNotBlank(tempData)) {
            exchangeListener.onNotify(tempData);
        }
    }



    public void reset() {
        synchronized (lock) {
            stringBuffer.delete(0, stringBuffer.length());
        }
    }

    public synchronized void write(String cmd) throws IOException {
        printWriter.println(cmd);
        printWriter.flush();
    }

    public String getPrintContent()  {
        synchronized (lock) {
            String tempData = stringBuffer.toString();
            stringBuffer.delete(0, tempData.length());
            return tempData;
        }
    }

    @Override
    public void close() throws IOException {
        running.set(false);
        mainThread.interrupt();
        inputStream.close();
    }

    public void start() {
        if (!mainThread.isAlive()) {
            mainThread.start();
        }
    }
}
