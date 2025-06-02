package ru.custom.blog.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ru.custom.blog.model.PostModel;
import ru.custom.blog.pagination.Paging;
import ru.custom.blog.service.PostService;

import java.util.Arrays;
import java.util.List;

@Controller
public class PostsController {

    private final PostService postService;

    @Autowired
    public PostsController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/")
    public String redirectToPosts() {
        return "redirect:posts";
    }

    @GetMapping("/posts")
    public String showPosts(
                        @RequestParam(name = "pageNumber", defaultValue = "1") int page,
                        @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
                        Model model) {

        long totalElements = postService.getTotalPostsCount();
        List<PostModel> posts = postService.getPage(page, pageSize);

        model.addAttribute("paging", new Paging(posts, page, pageSize, totalElements));
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("posts", posts);

        return "posts";
    }

    @PostMapping("/posts")
    public String handleCreatePost(
        @RequestParam("title") String title,
        @RequestParam("tags") String tags,
        @RequestParam("text") String text,
        @RequestParam("image") MultipartFile image,
        HttpServletRequest request) {

        PostModel post = new PostModel(title, 0, Arrays.stream(tags.split(" ")).toList(), text);
        postService.savePost(post, image, request.getServletContext().getRealPath(""));

        return "redirect:/posts";
    }

    @GetMapping("/posts/{id}")
    public String showPost(
        @PathVariable("id") Long id,
        Model model) {

        model.addAttribute("post", postService.getPost(id));

        return "post";
    }

    @PostMapping("/posts/{id}/like")
    public String handleUpdatePost(
        @PathVariable("id") Long id,
        @RequestParam("like") boolean like) {

        postService.updateLikesCount(id, like);

        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}")
    public String handleUpdatePost(
        @PathVariable("id") Long id,
        @RequestParam("title") String title,
        @RequestParam("tags") String tags,
        @RequestParam("text") String text,
        @RequestParam("image") MultipartFile image,
        Model model) {

        return "post";
    }
}
