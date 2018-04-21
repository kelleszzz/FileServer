package com.kelles.sdk.okhttp;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.kelles.sdk.data.FileDTO;
import com.kelles.sdk.data.ResultDO;
import com.kelles.sdk.setting.Setting;
import com.kelles.sdk.setting.Util;
import okhttp3.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileServerSDK implements Closeable {

    protected OkHttpClient client = new OkHttpClient();
    protected Gson gson = new Gson();
    final static Pattern patternFileName = Pattern.compile("attachment; filename=\"(.+)\"");

    public ResultDO<FileDTO> get(String id, String access_code) {
        if (Util.isEmpty(id) || Util.isEmpty(access_code)) {
            return Util.<FileDTO>getResultDO(false, Setting.STATUS_INVALID_PARAMETER, Setting.MESSAGE_INVALID_PARAMETER);
        }
        FileDTO fileDTO = null;
        ResultDO<FileDTO> resultDO = null;
        Response response = null;
        try {
            HttpUrl httpUrl = HttpUrl.parse(Setting.URL_BASIC + Setting.URL_GET).newBuilder()
                    .addQueryParameter("id", id)
                    .addQueryParameter("access_code", access_code)
                    .build();
            Request request = new Request.Builder().url(httpUrl).build();
            response = client.newCall(request).execute();
            if (response != null && response.isSuccessful() && response.body() != null) {
                fileDTO = new FileDTO();
                fileDTO.setId(id);
                fileDTO.setAccess_code(access_code);
                String contentLength = response.header(Setting.HEADER_FILE_SIZE, "0");
                fileDTO.setSize(Long.valueOf(contentLength));
                String contentDisposition = response.header("Content-Disposition");
                if (!Util.isEmpty(contentDisposition)) {
                    Matcher matcherFileName = patternFileName.matcher(contentDisposition);
                    if (matcherFileName.matches()) fileDTO.setFile_name(matcherFileName.group(1));
                } else {
                    fileDTO.setFile_name(Setting.TYPE_NOT_SPECIFIED);
                }
                fileDTO.setInputStream(response.body().byteStream());
                resultDO = Util.getResultDO(true, null, null, fileDTO);
                resultDO.setData(fileDTO);
                Util.log("Get fileDTO = %s, \nresult = %s", gson.toJson(Util.fileDTOInfo(fileDTO)), gson.toJson(Util.resultDOInfo(resultDO)));
                return resultDO;
            }
            Util.log("Get Error, id = %s, access_code = %s, response_code = %s\nresult = %s", id, access_code,
                    response == null ? null : response.code(), gson.toJson(Util.resultDOInfo(resultDO)));
            return Util.getResultDO(false, Setting.STATUS_ERROR, "Response Error, code = " + (response == null ? null : response.code()));
        } catch (IOException e) {
            e.printStackTrace();
            Util.log("Get Error, id = %s, access_code = %s, \nresult = %s", id, access_code, gson.toJson(Util.resultDOInfo(resultDO)));
            return Util.getResultDO(false, Setting.STATUS_ERROR);
        } finally {
//            if (response != null) response.close();
        }
    }

    /**
     * 不要忘记关闭InputStream
     *
     * @param id
     * @param access_code
     * @param file_name
     * @param inputStream
     * @return
     */
    public ResultDO<Response> insert(String id, String access_code, String file_name, InputStream inputStream) {
        if (Util.isEmpty(id) || Util.isEmpty(access_code) || Util.isEmpty(file_name) || inputStream == null) {
            return Util.getResultDO(false, Setting.STATUS_INVALID_PARAMETER, Setting.MESSAGE_INVALID_PARAMETER);
        }
        FileDTO fileDTO = new FileDTO();
        fileDTO.setId(id);
        fileDTO.setAccess_code(access_code);
        fileDTO.setFile_name(file_name);
        fileDTO.setInputStream(inputStream);
        return insert(fileDTO);
    }

    @SuppressWarnings("unchecked")
    protected ResultDO<Response> insert(FileDTO fileDTO) {
        if (fileDTO == null || Util.isEmpty(fileDTO.getId()) || Util.isEmpty(fileDTO.getAccess_code()) || Util.isEmpty(fileDTO.getFile_name())) {
            return Util.<Response>getResultDO(false, Setting.STATUS_INVALID_PARAMETER, Setting.MESSAGE_INVALID_PARAMETER);
        }
        Response response = null;
        ResultDO resultDO = null;
        try {
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("id", fileDTO.getId())
                    .addFormDataPart("access_code", fileDTO.getAccess_code())
                    .addFormDataPart("file_name", fileDTO.getFile_name())
                    .addPart(MultipartBody.Part.createFormData("file", fileDTO.getFile_name(),
                            new InputStreamRequestBody(MediaType.parse("application/octet-stream"), fileDTO.getInputStream())))
                    .build();
            Request request = new Request.Builder()
                    .url(Setting.URL_BASIC + Setting.URL_INSERT)
                    .post(requestBody)
                    .build();
            response = client.newCall(request).execute();
            if (response != null && response.isSuccessful() && response.body() != null) {
                resultDO = responseToResultDO(response);
                resultDO.setData(response);
                Util.log("Insert fileDTO = %s, \nresult = %s", gson.toJson(Util.fileDTOInfo(fileDTO)), gson.toJson(Util.resultDOInfo(resultDO)));
                return resultDO;
            }
            Util.log("Insert Error, fileDTO = %s, response_code = %s\nresult = %s", gson.toJson(Util.fileDTOInfo(fileDTO)),
                    response == null ? null : response.code(), gson.toJson(Util.resultDOInfo(resultDO)));
            return Util.getResultDO(false, Setting.STATUS_ERROR, "Response Error, code = " + (response == null ? null : response.code()));
        } catch (IOException e) {
            e.printStackTrace();
            Util.log("Insert Error, fileDTO = %s, \nresult = %s", gson.toJson(Util.fileDTOInfo(fileDTO)), gson.toJson(Util.resultDOInfo(resultDO)));
            return Util.getResultDO(false, Setting.STATUS_ERROR);
        } finally {
            if (response != null) response.close();
        }
    }

    /**
     * 从ResponseBody中提取出json,转换为ResultDO
     *
     * @param response
     * @return
     */
    protected ResultDO responseToResultDO(Response response) {
        ResultDO resultDO = new ResultDO();
        try {
            if (response != null && response.isSuccessful() && response.body() != null) {
                String responseBody = new String(response.body().bytes(), Setting.DEFAULT_CHARSET);
                try {
                    resultDO = gson.fromJson(responseBody, ResultDO.class);
                } catch (JsonSyntaxException e) {
                    Util.log("ResponseToResultDO Error, json = %s", responseBody);
                    resultDO = Util.getResultDO(false, Setting.STATUS_PARSE_JSON_ERROR, Setting.MESSAGE_PARSE_JSON_ERROR);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (resultDO == null) {
            Util.log("ResponseToResultDO Error, code = %s", response == null ? "null" : String.valueOf(response.code()));
            resultDO = Util.getResultDO(false, Setting.STATUS_RESPONSE_FAILURE, Setting.MESSAGE_RESPONSE_FAILURE);
        }
        return resultDO;
    }

    @Override
    public void close() throws IOException {

    }
}
