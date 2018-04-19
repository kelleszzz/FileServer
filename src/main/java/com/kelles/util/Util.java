package com.kelles.util;

import com.kelles.data.ResultDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

@Service
public class Util {
    @Autowired
    Charset defaultCharset;

    public InputStream stringToInputStream(String msg){
        if (msg==null) return null;
        return bytesToInputStream(msg.getBytes(defaultCharset));
    }

    public InputStream bytesToInputStream(byte[] bytes){
        if (bytes==null) return null;
        ByteArrayInputStream bais=new ByteArrayInputStream(bytes);
        return bais;
    }
}
