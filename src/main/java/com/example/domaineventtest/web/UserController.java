package com.example.domaineventtest.web;

import com.example.domaineventtest.application.UserService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    public void createUser() {
        userService.createUser("Jennie");
    }

    @PutMapping("/users/{id}")
    public void changeUserName(@PathVariable Long id) {
        userService.changeUserName(id, "Rose");
    }

}
