package com.hanyang.belieme.demoserver.general;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GeneralApiController {
    @GetMapping("/updateVer/")
    public String getVer() {
        String result = "1.0";
        return result;
    }
}