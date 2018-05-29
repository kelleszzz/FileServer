package com.kelles.fileserver.fileservercloud.controller;

import com.kelles.fileserver.fileservercloud.component.BaseComponent;
import com.kelles.fileserver.fileservercloud.service.FileDatabaseService;
import com.kelles.fileserver.fileserversdk.setting.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class BaseController extends BaseComponent {
    @Autowired
    FileDatabaseService fileDatabaseService;

    @ModelAttribute
    void addUrlsToModel(Model model) {
        model.addAttribute("indexUrl", Setting.URL_INDEX);
        model.addAttribute("getUrl", Setting.URL_GET);
        model.addAttribute("insertUrl", Setting.URL_INSERT);
        model.addAttribute("updateUrl", Setting.URL_UPDATE);
        model.addAttribute("removeUrl", Setting.URL_REMOVE);
    }
}
