package com.gd.self.tool.tx.controller;

import com.gd.self.tool.common.Result;
import com.gd.self.tool.tx.service.SoftwareCrawlerService;
import com.gd.self.tool.tx.service.SoftwareDetailCrawlerService;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guodun
 * @date 2026/3/19 16:41
 * @description
 */
@RestController
public class TestController {
    @Autowired
    private SoftwareCrawlerService softwareCrawlerService;
    @Autowired
    private SoftwareDetailCrawlerService softwareDetailCrawlerService;

    @GetMapping("/tx")
    public Result<String> txTest(@PathParam("url") String url){
        softwareCrawlerService.parseFromUrl(url);
        return Result.success();
    }
    @GetMapping("/update")
    public Result<String> update(){
        softwareDetailCrawlerService.update();
        return Result.success();
    }

}
