package com.epam.lstrsum.controllers;

import com.epam.lstrsum.model.User;
import com.epam.lstrsum.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    final private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping
    public List<User> getListOfUsers() {
        return userService.findAll();
    }
}
