package com.kelles.fileservercloud.controller;

import com.kelles.fileserversdk.data.FileDTO;
import com.kelles.fileserversdk.setting.Setting;
import com.kelles.fileserversdk.setting.Util;
import com.kelles.fileserversdk.data.*;
import com.kelles.fileserversdk.setting.*;
import com.kelles.fileservercloud.component.BaseComponent;
import com.kelles.fileservercloud.service.FileDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;

@Deprecated
@Controller
@RequestMapping("/message")
public class MessageController extends BaseComponent {
    @Autowired
    FileDatabaseService fileDatabaseService;


    /**
     * 不存在则插入,存在则更新
     * @param id
     * @param message
     * @param access_code
     * @return
     */
    @RequestMapping("/update")
    @ResponseBody
    public String update(@RequestParam String id,
                         @RequestParam String message,
                         @RequestParam String access_code,
                         @RequestParam(required = false) String new_access_code) {
        Connection conn = null;
        try {
            conn = fileDatabaseService.getConnection();
            FileDTO infoFileDTO=fileDatabaseService.getFileDTO(id,false,conn);
            if (infoFileDTO!=null && !securityCheck(id,access_code,infoFileDTO)){
                //文件存在且密码不一致
                return gson.toJson(Util.getResultDO(false, Setting.MESSAGE_ACCESS_DENIED));
            }
            FileDTO fileDTO = new FileDTO();
            fileDTO.setId(id);
            fileDTO.setAccess_code(new_access_code);
            fileDTO.setFile_name(Setting.TYPE_MESSAGE);
            byte[] bytes = message.getBytes(defaultCharset);
            InputStream is = util.bytesToInputStream(bytes);
            fileDTO.setInputStream(is);
            fileDTO.setSize((long)bytes.length);

            int rowsAffected = fileDatabaseService.updateFileDTO(fileDTO, infoFileDTO,conn);
            if (rowsAffected==Setting.STATUS_FILE_NOT_FOUND){
                //不存在则插入
                rowsAffected=fileDatabaseService.insertFileDTO(fileDTO,conn);
            }
            if (rowsAffected >= 0) {
                logger.info("Update Message, FileDTO = {}",gson.toJson(Util.fileDTOInfo(fileDTO)));
                return gson.toJson(Util.getResultDO(true, rowsAffected));
            } else {
                return gson.toJson(Util.getResultDO(false, rowsAffected));
            }
        } finally {
            closeConnection(conn);
        }
    }

    @RequestMapping("/insert")
    @ResponseBody
    public String insert(@RequestParam String id,
                         @RequestParam String message,
                         @RequestParam String access_code) {
        Connection conn = null;
        try {
            FileDTO fileDTO = new FileDTO();
            fileDTO.setId(id);
            fileDTO.setAccess_code(access_code);
            fileDTO.setCreate_time(System.currentTimeMillis());
            fileDTO.setFile_name(Setting.TYPE_MESSAGE);
            byte[] bytes = message.getBytes(defaultCharset);
            InputStream is = util.bytesToInputStream(bytes);
            fileDTO.setInputStream(is);
            fileDTO.setSize((long)bytes.length);
            conn = fileDatabaseService.getConnection();
            int rowsAffected = fileDatabaseService.insertFileDTO(fileDTO, conn);
            if (rowsAffected >= 0) {
                logger.info("Insert Message, FileDTO = {}",gson.toJson(Util.fileDTOInfo(fileDTO)));
                return gson.toJson(Util.getResultDO(true, rowsAffected));
            } else {
                return gson.toJson(Util.getResultDO(false, rowsAffected));
            }
        } finally {
            closeConnection(conn);
        }
    }

    @RequestMapping("/get")
    @ResponseBody
    public String get(@RequestParam String id,
                      @RequestParam String access_code) {
        Connection conn = null;
        try {
            conn = fileDatabaseService.getConnection();
            FileDTO fileDTO = fileDatabaseService.getFileDTO(id, true, conn);
            if (fileDTO==null){
                return gson.toJson(Util.getResultDO(false,Setting.MESSAGE_FILE_NOT_FOUND));
            }
            if (!securityCheck(id,access_code,fileDTO)){
                //安全检查
                return gson.toJson(Util.getResultDO(false,Setting.MESSAGE_ACCESS_DENIED));
            }
            if (fileDTO != null && fileDTO.getInputStream() != null) {
                logger.info("Get Message, FileDTO = {}",gson.toJson(fileDTO));
                InputStream is = fileDTO.getInputStream();
                if (is != null) {
                    //TODO 长文本这样获取是有问题的
                    int count = is.available();
                    byte[] bytes = new byte[count];
                    is.read(bytes);
                    String msg = new String(bytes, defaultCharset);
                    return gson.toJson(Util.getResultDO(true, null, null, msg));
                }
            }
            return gson.toJson(Util.getResultDO(false));
        } catch (IOException e) {
            e.printStackTrace();
            return gson.toJson(Util.getResultDO(false,"Exception "+e.getMessage()));
        } finally {
            closeConnection(conn);
        }
    }
}
