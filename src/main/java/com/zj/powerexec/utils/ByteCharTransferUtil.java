package com.zj.powerexec.utils;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * ByteCharTransferUtil
 *
 * @author chenzhijie
 * @date 2023/3/28
 */
public class ByteCharTransferUtil {
    public static char[] byteToChar(byte[] bytes, Charset charset) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        CharBuffer charBuffer = charset.decode(byteBuffer);
        return charBuffer.array();
    }

    // char转byte
    public static byte[] charToBytes(char[] chars) {
        Charset charset = StandardCharsets.ISO_8859_1;
        CharBuffer charBuffer = CharBuffer.allocate(chars.length);
        charBuffer.put(chars);
        charBuffer.flip();
        ByteBuffer byteBuffer = charset.encode(charBuffer);
        return byteBuffer.array();
    }

}
