package com.kelles.fileserver.fileserversdk.setting;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.kelles.fileserver.fileserversdk.data.FileDTO;
import com.kelles.fileserver.fileserversdk.data.ResultDO;
import org.apache.commons.beanutils.BeanUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * UserServer 继承此Util
 */
public class Util {

    protected final static int BUFFER_SIZE = 1024;


    /**
     * 利用反射,将updateDTO中所有非空的域更新到originDTO中
     *
     * @param updateDTO
     * @param originDTO
     */
    public static void updateDTO(Object updateDTO, Object originDTO) {
        if (updateDTO == null || originDTO == null) return;
        Field[] fields = updateDTO.getClass().getDeclaredFields();
        if (fields == null) return;
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(updateDTO);
                if (value != null) {
                    field.set(originDTO, value);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * [start,end]
     * end超过InputStream最后一字节范围时,读取全部InputStream
     * start超过InputStream最后一字节范围时,抛出ArrayIndexOutOfBoundsException
     *
     * @param inputStream
     * @param start
     * @param end
     * @return
     */
    public static byte[] inputStreamToBytes(InputStream inputStream, long start, long end) {
        if (inputStream == null) throw new NullPointerException("InputStream Null");
        if (start < 0 || start > end) throw new ArrayIndexOutOfBoundsException("inputStreamToBytes out of Bounds");
        try {
            //skip
            for (long remaining = start; remaining > 0; ) {
                long ns = inputStream.skip(remaining);
                if (ns == 0 && remaining > 0) {
                    throw new IOException("InputStream Aborted while Skipping Bytes");
                } else if (ns == 0) {
                    throw new ArrayIndexOutOfBoundsException("InputStream Read to End while Skipping Bytes");
                }
                remaining -= ns;
            }
            //read
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (long remaining = end - start + 1; remaining > 0; ) {
                int ntr = BUFFER_SIZE < remaining ? BUFFER_SIZE : (int) remaining;
                byte[] bytes = new byte[ntr];
                int nr = inputStream.read(bytes, 0, ntr);
                if (nr == -1) break;
                baos.write(bytes, 0, nr);
                remaining -= nr;
            }
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从InputStream读取字节,写入byte数组
     * InputStream中的数据不能过大
     *
     * @param inputStream
     * @return
     */
    public static byte[] inputStreamToBytes(InputStream inputStream) {
        return inputStreamToBytes(inputStream, 0, Long.MAX_VALUE - 1);
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

    public static FileDTO jsonToFileDTO(String json, Gson gson) {
        if (json == null || gson == null) return null;
        try {
            FileDTO fileDTO = gson.fromJson(json, FileDTO.class);
            return fileDTO;
        } catch (JsonSyntaxException e) {
            return null;
        }
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

    public static void log(String message) {
        if (message == null) return;
        System.out.println(message);
    }

    public static boolean isEmpty(Object str) {
        return str == null || "".equals(str);
    }
}