package com.kelles.sdk.setting;

import com.kelles.sdk.data.FileDTO;
import com.kelles.sdk.data.ResultDO;
import com.sun.istack.internal.Nullable;
import org.apache.commons.beanutils.BeanUtils;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

public class Util {
    /**
     * 从InputStream读取字节,写入byte数组
     * InputStream中的数据不能过大
     * @param inputStream
     * @return
     */
    public static byte[] inputStreamToBytes(InputStream inputStream) {
        if (inputStream == null) return null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (byte[] bytes = new byte[1024]; ; ) {
                int bytesRead = inputStream.read(bytes);
                if (bytesRead == -1) break;
                baos.write(bytes, 0, bytesRead);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static InputStream stringToInputStream(String msg) {
        if (msg == null) return null;
        return bytesToInputStream(msg.getBytes(Setting.DEFAULT_CHARSET));
    }

    public static InputStream bytesToInputStream(byte[] bytes) {
        if (bytes == null) return null;
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return bais;
    }

    public static <T> ResultDO<T> getResultDO(boolean success, Integer code, String message, T data) {
        ResultDO<T> resultDO = new ResultDO<T>();
        resultDO.setSuccess(success);
        resultDO.setCode(code);
        resultDO.setMessage(message);
        resultDO.setData(data);
        return resultDO;
    }

    public static <T> ResultDO<T> getResultDO(boolean success, int code, String message) {
        return getResultDO(success, code, message, null);
    }

    public static <T> ResultDO<T> getResultDO(boolean success, String message) {
        return getResultDO(success, null, message, null);
    }

    public static <T> ResultDO<T> getResultDO(boolean success, int code) {
        return getResultDO(success, code, null, null);
    }

    public static <T> ResultDO<T> getResultDO(boolean success) {
        return getResultDO(success, null, null, null);
    }

    public static FileDTO fileDTOInfo(FileDTO fileDTO) {
        if (fileDTO == null) return null;
        try {
            FileDTO fileDTOInfo = new FileDTO();
            BeanUtils.copyProperties(fileDTOInfo, fileDTO);
            fileDTOInfo.setInputStream(null);
            return fileDTOInfo;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> ResultDO<T> resultDOInfo(ResultDO resultDO) {
        if (resultDO == null) return null;
        try {
            ResultDO<T> resultDOInfo = new ResultDO<>();
            BeanUtils.copyProperties(resultDOInfo, resultDO);
            resultDOInfo.setData(null);
            return resultDOInfo;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void log(String format, Object... args) {
        if (format == null) return;
        System.out.println(String.format(format, args));
    }

    public static boolean isEmpty(@Nullable Object str) {
        return str == null || "".equals(str);
    }
}