package ru.custom.blog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.custom.blog.model.PostModel;
import ru.custom.blog.service.PostService;

@Controller
public class AddPostController {
    private final PostService postService;

    @Autowired
    public AddPostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/posts/add")
    public String addPost(Model model) {
        return "add-post";
    }

    @GetMapping("/posts/{id}/edit")
    public String editPost(@PathVariable("id") Long id, Model model) {
        PostModel post = postService.getPost(id);
        model.addAttribute("post", post);
        return "add-post";
    }
}
