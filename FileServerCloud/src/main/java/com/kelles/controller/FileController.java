package com.kelles.controller;

import com.kelles.sdk.data.*;
import com.kelles.sdk.setting.*;
import com.kelles.component.BaseComponent;
import com.kelles.service.FileDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import java.io.IOException;
import java.sql.Connection;

@Controller
@RequestMapping("/file")
public class FileController extends BaseComponent {

    @Autowired
    FileDatabaseService fileDatabaseService;

    void addUrisToModel(Model model) {
        UriComponents uriComponents=MvcUriComponentsBuilder.fromController(FileController.class).build();
        model.addAttribute("insertUri",uriComponents.toString()+"/insert");
        model.addAttribute("updateUri",uriComponents.toString()+"/update");
        model.addAttribute("indexUri",uriComponents.toString()+"/index");
        model.addAttribute("removeUri",uriComponents.toString()+"/remove");
    }

    @RequestMapping("/remove")
    @ResponseBody
    public String remove(@RequestParam String id,
                         @RequestParam String access_code) {
        Connection conn = null;
        FileDTO fileDTO = null;
        try {
            conn = fileDatabaseService.getConnection();
            fileDTO=fileDatabaseService.getFileDTO(id,false,conn);
            if (fileDTO==null){
                return gson.toJson(getResultDO(true,Setting.STATUS_FILE_NOT_FOUND,Setting.MESSAGE_FILE_NOT_FOUND));
            }
            if (!securityCheck(id,access_code,fileDTO)){
                return gson.toJson(getResultDO(false,Setting.STATUS_ACCESS_DENIED,Setting.MESSAGE_ACCESS_DENIED));
            }
            int rowsAffected = fileDatabaseService.removeFileDTO(id, conn);
            logger.info("Remove File, FileDTO = {}", gson.toJson(fileDTOInfo(fileDTO)));
            return gson.toJson(getResultDO(rowsAffected > 0, rowsAffected));
        } finally {
            closeConnection(conn);
        }
    }

    @RequestMapping("/index")
    public String index(@RequestParam(required = false) String id,
                        @RequestParam(required = false) String access_code,
                        Model model) {
        addUrisToModel(model);
        if (!StringUtils.isEmpty(id) && !StringUtils.isEmpty(access_code)) {
            Connection conn = null;
            try {
                conn = fileDatabaseService.getConnection();
                FileDTO fileDTO = fileDatabaseService.getFileDTO(id, false, conn);
                if (securityCheck(id, access_code, fileDTO)) {
                    model.addAttribute("file_name", fileDTO.getFile_name());
                    model.addAttribute("getCurrentFileUri", MvcUriComponentsBuilder
                            .fromMethodName(FileController.class, "get", fileDTO.getId(), fileDTO.getAccess_code())
                            .build());
                }
            } finally {
                closeConnection(conn);
            }
        }
        return "uploadForm";
    }

    /**
     * 不存在文件则创建,存在文件则更新
     * 这里File不会为null,只会为空文件
     * @param id
     * @param access_code
     * @param new_access_code
     * @param file_name
     * @param file
     * @return
     */
    @RequestMapping("/update")
    @ResponseBody
    public String update(@RequestParam String id,
                         @RequestParam String access_code,
                         @RequestParam(required = false) String new_access_code,
                         @RequestParam(required = false) String file_name,
                         @RequestParam(required = false) MultipartFile file) {
        Connection conn = null;
        FileDTO fileDTO = null;
        try {
            conn = fileDatabaseService.getConnection();
            FileDTO infoFileDTO = fileDatabaseService.getFileDTO(id, false, conn);
            if (infoFileDTO == null) {
                //插入
                if (file==null) return gson.toJson(getResultDO(false,Setting.STATUS_FILE_NOT_FOUND,Setting.MESSAGE_FILE_NOT_FOUND));
                return insert(id, access_code, file_name, file);
            }
            if (!securityCheck(id, access_code, infoFileDTO)) {
                //无权限
                return gson.toJson(getResultDO(false, Setting.STATUS_ACCESS_DENIED, Setting.MESSAGE_ACCESS_DENIED));
            }
            //更新
            fileDTO = new FileDTO();
            fileDTO.setId(id);
            fileDTO.setAccess_code(new_access_code);
            fileDTO.setFile_name(file_name);
            fileDTO.setInputStream(file.getInputStream());
            fileDTO.setSize(file.getSize());
            int rowsAffected = fileDatabaseService.updateFileDTO(fileDTO, infoFileDTO, conn);
            logger.info("Update File, FileDTO = {}", gson.toJson(fileDTOInfo(fileDTO)));
            return gson.toJson(getResultDO(rowsAffected > 0, rowsAffected));
        } catch (IOException e) {
            logger.error("Update File, FileDTO = {}", gson.toJson(fileDTOInfo(fileDTO)));
            e.printStackTrace();
            return gson.toJson(getResultDO(false, Setting.STATUS_ERROR));
        } finally {
            closeConnection(conn);
        }
    }

    @RequestMapping("/insert")
    @ResponseBody
    public String insert(@RequestParam String id,
                         @RequestParam String access_code,
                         @RequestParam(required = false) String file_name,
                         @RequestParam MultipartFile file) {
        Connection conn = null;
        FileDTO fileDTO = null;
        try {
            conn = fileDatabaseService.getConnection();
            fileDTO = fileDatabaseService.getFileDTO(id, false, conn);
            if (fileDTO != null) {
                return gson.toJson(getResultDO(false, Setting.STATUS_FILE_ALREADY_EXISTS, Setting.MESSAGE_FILE_ALREADY_EXISTS));
            }
            fileDTO = new FileDTO();
            fileDTO.setId(id);
            fileDTO.setAccess_code(access_code);
            if (!StringUtils.isEmpty(file_name)) fileDTO.setFile_name(file_name); else fileDTO.setFile_name(file.getOriginalFilename());
            fileDTO.setInputStream(file.getInputStream());
            fileDTO.setSize(file.getSize());
            int rowsAffected = fileDatabaseService.insertFileDTO(fileDTO, conn);
            logger.info("Insert File, FileDTO = {}", gson.toJson(fileDTOInfo(fileDTO)));
            return gson.toJson(getResultDO(rowsAffected > 0, rowsAffected));
        } catch (IOException e) {
            logger.error("Insert File, FileDTO = {}", gson.toJson(fileDTOInfo(fileDTO)));
            e.printStackTrace();
            return gson.toJson(getResultDO(false, Setting.STATUS_ERROR));
        } finally {
            closeConnection(conn);
        }
    }

    @RequestMapping("/get")
    @ResponseBody
    public ResponseEntity get(@RequestParam(required = false) String id,
                              @RequestParam(required = false) String access_code) {
        Connection conn = null;
        try {
            conn = fileDatabaseService.getConnection();
            FileDTO fileDTO = fileDatabaseService.getFileDTO(id, true, conn);
            if (fileDTO == null) {
                return ResponseEntity.ok().body(gson.toJson(getResultDO(false, Setting.STATUS_FILE_NOT_FOUND, Setting.MESSAGE_FILE_NOT_FOUND)));
            }
            if (!securityCheck(id, access_code, fileDTO)) {
                return ResponseEntity.ok().body(gson.toJson(getResultDO(false, Setting.STATUS_ACCESS_DENIED, Setting.MESSAGE_ACCESS_DENIED)));
            }
            if (StringUtils.isEmpty(fileDTO.getFile_name()) || Setting.TYPE_MESSAGE.equals(fileDTO.getFile_name())) {
                //修改文件名
                fileDTO.setFile_name(fileDTO.getId());
            }
            InputStreamResource resource = new InputStreamResource(fileDTO.getInputStream());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDTO.getFile_name() + "\"")
                    .body(resource);
        } finally {
            closeConnection(conn);
        }
    }
}
