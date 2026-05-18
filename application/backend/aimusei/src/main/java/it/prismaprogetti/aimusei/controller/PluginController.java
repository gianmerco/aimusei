package it.prismaprogetti.aimusei.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
public class PluginController {

    @Value("${APP_BASE_URL:https://api.museiitaliani.it}")
    private String baseUrl;

    @GetMapping(value = "/pluginIframe.js", produces = "application/javascript")
    public String pluginIframe() throws IOException {
        ClassPathResource resource = new ClassPathResource("plugin/pluginIframe.js");
        String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        return content.replace("__APP_BASE_URL__", baseUrl);
    }
}
