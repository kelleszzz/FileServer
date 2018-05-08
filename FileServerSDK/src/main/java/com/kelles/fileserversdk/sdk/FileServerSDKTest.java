package com.kelles.fileserversdk.sdk;

import com.google.gson.Gson;
import com.kelles.fileserversdk.data.FileDTO;
import com.kelles.fileserversdk.data.ResultDO;
import com.kelles.fileserversdk.setting.Setting;
import com.kelles.fileserversdk.setting.Util;
import okhttp3.Response;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FileServerSDKTest {
    Gson gson = new Gson();
    FileServerSDK fileServerSDK = new FileServerSDK();
    String testId = "kelleszzz";
    String testAccessCode = "tom44123";
    String testFileName = "congratulations.txt";
    String testContent = "Congratulations, you passed the test!";

    public static void main(String[] args){
        FileServerSDKTest test=new FileServerSDKTest();
        try {
            test.test1_Insert();
            test.test2_Get();
            test.test3_Update();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            test.test4_Remove();
        }
    }

    @Test
    public void test1_Insert() throws IOException {
        InputStream inputStream = null;
        ResultDO<Response> resultDO = null;
        try {
            FileDTO fileDTO = new FileDTO();
            fileDTO.setId(testId);
            fileDTO.setAccess_code(testAccessCode);
            fileDTO.setFile_name(testFileName);
            fileDTO.setInputStream(Util.bytesToInputStream(testContent.getBytes(Setting.DEFAULT_CHARSET)));
            resultDO = fileServerSDK.insert(fileDTO);
            Assert.assertTrue(resultDO.getSuccess() || resultDO.getCode() == Setting.STATUS_FILE_ALREADY_EXISTS);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    @Test
    public void test2_Get() throws IOException {
        InputStream inputStream = null;
        try {
            ResultDO<FileDTO> resultDO = fileServerSDK.get(testId, testAccessCode);
            Assert.assertTrue(resultDO.getSuccess() && resultDO.getData() != null && resultDO.getData().getInputStream() != null);
            FileDTO fileDTO = resultDO.getData();
            inputStream = fileDTO.getInputStream();
            byte[] bytes = Util.inputStreamToBytes(fileDTO.getInputStream());
            String responseContent = new String(bytes, Setting.DEFAULT_CHARSET);
            Assert.assertTrue(testContent.equals(responseContent) && testFileName.equals(fileDTO.getFile_name()));
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    @Test
    public void test3_Update() throws IOException {
        InputStream isUpdate=null, isGet = null;
        ResultDO<Response> resultUpdate = null;
        ResultDO<FileDTO> resultGet = null;
        try {
            resultUpdate = fileServerSDK.update(testId, testAccessCode,
                    null, "hucci.txt", Util.bytesToInputStream("Hello, Hucci!".getBytes(Setting.DEFAULT_CHARSET)));
            Assert.assertTrue(resultUpdate != null && resultUpdate.getSuccess());
            resultGet = fileServerSDK.get(testId, testAccessCode);
            Assert.assertTrue(resultGet != null && resultGet.getSuccess() && resultGet.getData() != null);
            FileDTO fileDTO=resultGet.getData();
            Assert.assertEquals("Hello, Hucci!",new String(Util.inputStreamToBytes(fileDTO.getInputStream()),Setting.DEFAULT_CHARSET));
        } finally {
            if (isUpdate != null) {
                isUpdate.close();
            }
            if (isGet != null) {
                isGet.close();
            }
        }
    }

    @Test
    public void test4_Remove(){
        ResultDO resultDO=fileServerSDK.remove(testId,testAccessCode);
        Assert.assertTrue(resultDO.getSuccess());
    }

    @Test
    public void testDraft() throws InvocationTargetException, IllegalAccessException {
    }

}
