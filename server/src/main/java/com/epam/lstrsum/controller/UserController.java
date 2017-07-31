package com.epam.lstrsum.controller;

import com.epam.lstrsum.model.User;
import com.epam.lstrsum.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    final private UserService userService;

    @RequestMapping
    public List<User> getListOfUsers() {
        return userService.findAll();
    }
}
