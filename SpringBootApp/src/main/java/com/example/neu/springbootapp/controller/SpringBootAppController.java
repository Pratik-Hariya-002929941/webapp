package com.example.neu.springbootapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpringBootAppController {

    @RequestMapping("/healtz")
    @ResponseStatus(code = HttpStatus.OK)
    public void appCheck(){
        return;
    }
}
