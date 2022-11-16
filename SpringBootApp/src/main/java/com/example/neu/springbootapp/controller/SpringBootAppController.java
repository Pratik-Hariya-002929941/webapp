package com.example.neu.springbootapp.controller;

import com.example.neu.springbootapp.config.StatsdClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class SpringBootAppController {

    private static StatsdClient statsDClient;

    static {
        try {
            statsDClient = new StatsdClient("localhost", 8125);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Logger logger = LoggerFactory.getLogger(SpringBootAppController.class);

    @RequestMapping("/healtz")
    @ResponseStatus(code = HttpStatus.OK)
    public void appCheck(){
        logger.info("Reached: GET /healthz");
        statsDClient.increment("endpoint.http.getHealthz");
        return;
    }
}
