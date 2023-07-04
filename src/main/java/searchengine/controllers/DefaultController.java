package searchengine.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import searchengine.config.SitesList;

@Controller
public class DefaultController {

    private final SitesList sitesList;

    public DefaultController(SitesList sitesList) {
        this.sitesList = sitesList;
    }

    /**
     * Метод формирует страницу из HTML-файла index.html,
     * который находится в папке resources/templates.
     * Это делает библиотека Thymeleaf.
     */
    @RequestMapping("/")
    public String index() {
        return "index";
    }
}
