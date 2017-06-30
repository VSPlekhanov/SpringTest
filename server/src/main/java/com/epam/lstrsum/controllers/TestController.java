package com.epam.lstrsum.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hop")
public class TestController {

    @GetMapping("/{name}")
    public String find(@PathVariable("name") String name) {
        return "dev says " + name;
    }

}
