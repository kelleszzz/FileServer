package com.kelles.fileservercloud.controller;

import com.kelles.fileserversdk.data.FileDTO;
import com.kelles.fileserversdk.setting.Setting;
import com.kelles.fileserversdk.setting.Util;
import com.kelles.fileserversdk.data.*;
import com.kelles.fileserversdk.setting.*;
import com.kelles.fileservercloud.component.BaseComponent;
import com.kelles.fileservercloud.service.FileDatabaseService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.sql.Connection;

/**
 * 改变路径时,同时改变Setting中URL部分
 */
@Controller
@RequestMapping("/file")
public class FileController extends BaseComponent {

    @Autowired
    FileDatabaseService fileDatabaseService;

    @ModelAttribute
    void addUrisToModel(Model model) {
        model.addAttribute("insertUri", Setting.URL_INSERT);
        model.addAttribute("updateUri", Setting.URL_UPDATE);
        model.addAttribute("indexUri", Setting.URL_INDEX);
        model.addAttribute("removeUri", Setting.URL_REMOVE);
    }

    @RequestMapping("/remove")
    @ResponseBody
    public Object remove(@RequestParam String id,
                         @RequestParam String access_code) {
        Connection conn = null;
        FileDTO fileDTO = null;
        try {
            conn = fileDatabaseService.getConnection();
            fileDTO = fileDatabaseService.getFileDTO(id, false, conn);
            if (fileDTO == null) {
                return gson.toJson(Util.getResultDO(true, Setting.STATUS_FILE_NOT_FOUND, Setting.MESSAGE_FILE_NOT_FOUND));
            }
            if (!securityCheck(id, access_code, fileDTO)) {
                return gson.toJson(Util.getResultDO(false, Setting.STATUS_ACCESS_DENIED, Setting.MESSAGE_ACCESS_DENIED));
            }
            int rowsAffected = fileDatabaseService.removeFileDTO(id, conn);
            logger.info("Remove File, FileDTO = {}", gson.toJson(Util.fileDTOInfo(fileDTO)));
            return gson.toJson(Util.getResultDO(rowsAffected > 0, rowsAffected));
        } finally {
            closeConnection(conn);
        }
    }

    @RequestMapping("/index")
    public String index(@RequestParam(required = false) String id,
                        @RequestParam(required = false) String access_code,
                        Model model) {
        if (!StringUtils.isEmpty(id) && !StringUtils.isEmpty(access_code)) {
            Connection conn = null;
            try {
                conn = fileDatabaseService.getConnection();
                FileDTO fileDTO = fileDatabaseService.getFileDTO(id, false, conn);
                if (securityCheck(id, access_code, fileDTO)) {
                    model.addAttribute("file_name", fileDTO.getFile_name());
                    UriComponents uriComponents = UriComponentsBuilder.fromPath(Setting.URL_GET)
                            .queryParam("id", id)
                            .queryParam("access_code", access_code)
                            .build();
                    model.addAttribute("getCurrentFileUri", uriComponents.toString());
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
     *
     * @param id
     * @param access_code
     * @param new_access_code
     * @param file_name
     * @param file
     * @return
     */
    @RequestMapping("/update")
    @ResponseBody
    public Object update(@RequestParam String id,
                         @RequestParam String access_code,
                         @RequestParam(required = false) String new_access_code,
                         @RequestParam(required = false) String file_name,
                         @RequestParam(required = false) MultipartFile file,
                         Model model) {
        Connection conn = null;
        FileDTO fileDTO = null;
        try {
            conn = fileDatabaseService.getConnection();
            FileDTO infoFileDTO = fileDatabaseService.getFileDTO(id, false, conn);
            if (infoFileDTO == null) {
                //插入
                if (file == null)
                    return gson.toJson(Util.getResultDO(false, Setting.STATUS_FILE_NOT_FOUND, Setting.MESSAGE_FILE_NOT_FOUND));
                model.addAttribute("id", id);
                model.addAttribute("access_code", access_code);
                model.addAttribute("file", file);
                return new ModelAndView("forward:" + Setting.URL_INSERT, model.asMap());
            }
            if (!securityCheck(id, access_code, infoFileDTO)) {
                //无权限
                return gson.toJson(Util.getResultDO(false, Setting.STATUS_ACCESS_DENIED, Setting.MESSAGE_ACCESS_DENIED));
            }
            //更新
            fileDTO = new FileDTO();
            fileDTO.setId(id);
            fileDTO.setAccess_code(new_access_code);
            fileDTO.setFile_name(file_name);
            fileDTO.setInputStream(file.getInputStream());
            fileDTO.setSize(file.getSize());
            int rowsAffected = fileDatabaseService.updateFileDTO(fileDTO, infoFileDTO, conn);
            logger.info("Update File, FileDTO = {}", gson.toJson(Util.fileDTOInfo(fileDTO)));
            return gson.toJson(Util.getResultDO(rowsAffected > 0, rowsAffected));
        } catch (IOException e) {
            logger.error("Update File, FileDTO = {}", gson.toJson(Util.fileDTOInfo(fileDTO)));
            e.printStackTrace();
            return gson.toJson(Util.getResultDO(false, Setting.STATUS_ERROR));
        } finally {
            closeConnection(conn);
        }
    }

    @RequestMapping("/insert")
    @ResponseBody
    public Object insert(@RequestParam String id,
                         @RequestParam String access_code,
                         @RequestParam(required = false) String file_name,
                         @RequestParam MultipartFile file) {
        Connection conn = null;
        FileDTO fileDTO = null;
        try {
            conn = fileDatabaseService.getConnection();
            fileDTO = fileDatabaseService.getFileDTO(id, false, conn);
            if (fileDTO != null) {
                return gson.toJson(Util.getResultDO(false, Setting.STATUS_FILE_ALREADY_EXISTS, Setting.MESSAGE_FILE_ALREADY_EXISTS));
            }
            fileDTO = new FileDTO();
            fileDTO.setId(id);
            fileDTO.setAccess_code(access_code);
            if (!StringUtils.isEmpty(file_name)) fileDTO.setFile_name(file_name);
            else fileDTO.setFile_name(file.getOriginalFilename());
            fileDTO.setInputStream(file.getInputStream());
            fileDTO.setSize(file.getSize());
            int rowsAffected = fileDatabaseService.insertFileDTO(fileDTO, conn);
            logger.info("Insert File, FileDTO = {}", gson.toJson(Util.fileDTOInfo(fileDTO)));
            return gson.toJson(Util.getResultDO(rowsAffected > 0, rowsAffected));
        } catch (IOException e) {
            logger.error("Insert File, FileDTO = {}", gson.toJson(Util.fileDTOInfo(fileDTO)));
            e.printStackTrace();
            return gson.toJson(Util.getResultDO(false, Setting.STATUS_ERROR));
        } finally {
            closeConnection(conn);
        }
    }

    @RequestMapping("/get")
    @ResponseBody
    public Object get(@RequestParam String id,
                      @RequestParam String access_code) {
        Connection conn = null;
        try {
            conn = fileDatabaseService.getConnection();
            FileDTO fileDTO = fileDatabaseService.getFileDTO(id, true, conn);
            if (fileDTO == null) {
                return ResponseEntity.ok().body(gson.toJson(Util.getResultDO(false, Setting.STATUS_FILE_NOT_FOUND, Setting.MESSAGE_FILE_NOT_FOUND)));
            }
            if (!securityCheck(id, access_code, fileDTO)) {
                return ResponseEntity.ok().body(gson.toJson(Util.getResultDO(false, Setting.STATUS_ACCESS_DENIED, Setting.MESSAGE_ACCESS_DENIED)));
            }
            if (StringUtils.isEmpty(fileDTO.getFile_name()) || Setting.TYPE_MESSAGE.equals(fileDTO.getFile_name())) {
                //修改文件名
                fileDTO.setFile_name(fileDTO.getId());
            }
            InputStreamResource resource = new InputStreamResource(fileDTO.getInputStream());
            //TODO 大文件无法下载
            byte[] bytes = Util.inputStreamToBytes(resource.getInputStream());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDTO.getFile_name() + "\"")
                    .header(Setting.HEADER_FILEDTO_INFO, gson.toJson(Util.fileDTOInfo(fileDTO)))
                    .body(bytes);
        } catch (IOException e) {
            logger.error("Get File, id = {}, access_code = {}", id, access_code);
            e.printStackTrace();
            return gson.toJson(Util.getResultDO(false, Setting.STATUS_ERROR));
        } finally {
            closeConnection(conn);
        }
    }
}
