package com.kelles.sdk.okhttp;

import com.kelles.sdk.data.FileDTO;
import com.kelles.sdk.data.ResultDO;
import com.kelles.sdk.setting.Setting;
import com.kelles.sdk.setting.Util;
import okhttp3.Response;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FileServerSDKTest {
    FileServerSDK fileServerSDK = new FileServerSDK();
    String testId="kelleszzz";
    String testAccessCode="tom44123";
    String testFileName="congratulations.txt";
    String testContent="Congratulations, you passed the test!";

    @Test
    public void testAInsert() throws IOException {
        InputStream inputStream = null;
        ResultDO<Response> resultDO = null;
        try {
            FileDTO fileDTO = new FileDTO();
            fileDTO.setId(testId);
            fileDTO.setAccess_code(testAccessCode);
            fileDTO.setFile_name(testFileName);
            inputStream = new ByteArrayInputStream(testContent.getBytes(Setting.DEFAULT_CHARSET));
            fileDTO.setInputStream(inputStream);
            resultDO = fileServerSDK.insert(fileDTO);
            Assert.assertTrue(resultDO.getSuccess() || resultDO.getCode()==Setting.STATUS_FILE_ALREADY_EXISTS);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    @Test
    public void testBGet() throws IOException {
        InputStream inputStream=null;
        try {
            ResultDO<FileDTO> resultDO=fileServerSDK.get(testId,testAccessCode);
            Assert.assertTrue(resultDO.getSuccess() && resultDO.getData()!=null && resultDO.getData().getInputStream()!=null);
            FileDTO fileDTO=resultDO.getData();
            inputStream=fileDTO.getInputStream();
            byte[] bytes=Util.inputStreamToBytes(fileDTO.getInputStream());
            String responseContent=new String(bytes,Setting.DEFAULT_CHARSET);
            Assert.assertTrue(testContent.equals(responseContent));
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

}
