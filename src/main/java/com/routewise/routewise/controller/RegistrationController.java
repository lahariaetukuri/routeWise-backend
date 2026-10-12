package com.routewise.routewise.controller;

import com.routewise.routewise.entity.User;
import com.routewise.routewise.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register")
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User registerUser(@RequestBody User user) {
        return userService.createUser(user);
    }
}