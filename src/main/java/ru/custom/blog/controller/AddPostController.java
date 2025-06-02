package ru.custom.blog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AddPostController {

    @GetMapping("/posts/add")
    public String addPost(Model model) {
        return "add-post";
    }
}
