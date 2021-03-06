package com.kelles.fileserver.fileservercloud.controller;

import com.kelles.fileserver.fileservercloud.service.FileDatabaseService;
import com.kelles.fileserver.fileserversdk.data.FileDTO;
import com.kelles.fileserver.fileserversdk.setting.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.sql.Connection;

@Controller
@RequestMapping(Setting.PATH_UI)
public class UIController extends BaseController {

    @Autowired
    FileDatabaseService fileDatabaseService;

    @RequestMapping(Setting.PATH_INDEX)
    public String index(@RequestParam(required = false) String id,
                        @RequestParam(required = false) String access_code,
                        Model model) {
        if (!StringUtils.isEmpty(id) && !StringUtils.isEmpty(access_code)) {
            UriComponents uriComponents = UriComponentsBuilder.fromPath(Setting.URL_DISPLAY)
                    .queryParam("id", id)
                    .queryParam("access_code", access_code)
                    .build();
            model.addAttribute("showFileUrl", uriComponents.toString());
        }
        return "uploadForm";
    }

    @RequestMapping(Setting.PATH_DISPLAY)
    public String display(@RequestParam(required = false) String id,
                          @RequestParam(required = false) String access_code,
                          Model model) {
        if (!StringUtils.isEmpty(id) && !StringUtils.isEmpty(access_code)) {
            Connection conn = null;
            try {
                conn = fileDatabaseService.getConnection();
                FileDTO fileDTO = fileDatabaseService.getFileDTO(id, false, conn);
                if (securityCheck(id, access_code, fileDTO)) {
                    model.addAttribute("file_name", fileDTO.getFile_name());
                    UriComponentsBuilder builder = UriComponentsBuilder.fromPath(Setting.URL_GET)
                            .queryParam("id", id)
                            .queryParam("access_code", access_code);
                    model.addAttribute("getCurrentFileUrl", builder.build().toString());
                    model.addAttribute("getCurrentVideoUrl",builder.queryParam("video","true").build().toString());
                }
            } finally {
                closeConnection(conn);
            }
        }
        return "displayGet";
    }

}
