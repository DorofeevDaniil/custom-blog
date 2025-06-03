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
import ru.custom.blog.model.CommentModel;
import ru.custom.blog.model.PostModel;
import ru.custom.blog.pagination.Paging;
import ru.custom.blog.service.CommentsService;
import ru.custom.blog.service.PostService;

import java.util.Arrays;
import java.util.List;

@Controller
public class PostsController {

    private final PostService postService;
    private final CommentsService commentsService;

    private static final String REDIRECT_POSTS = "redirect:/posts";

    @Autowired
    public PostsController(PostService postService, CommentsService commentsService) {
        this.postService = postService;
        this.commentsService = commentsService;
    }

    @GetMapping("/")
    public String redirectToPosts() {
        return "redirect:posts";
    }

    @GetMapping("/posts")
    public String showPosts(
        @RequestParam(name = "pageNumber", defaultValue = "1") int page,
        @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
        @RequestParam(name = "search", required = false) String tag,
        Model model) {

        List<PostModel> posts;
        Paging paging;

        long totalElements = postService.getTotalPostsCount();

        if (tag != null && !tag.isEmpty()) {
            posts = postService.getPageByTag(tag, pageSize);
            paging = new Paging(posts, pageSize, totalElements);
        } else {
            posts = postService.getPage(page, pageSize);
            paging = new Paging(posts, page, pageSize, totalElements);
        }

        model.addAttribute("posts", posts);
        model.addAttribute("paging",paging);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("currentPage", page);

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
        Long postId = postService.savePost(post, image, request.getServletContext().getRealPath(""));

        return REDIRECT_POSTS + "/" + postId;
    }

    @PostMapping("/posts/{id}")
    public String handleUpdatePost(
        @PathVariable("id") Long id,
        @RequestParam("title") String title,
        @RequestParam("tags") String tags,
        @RequestParam("text") String text,
        @RequestParam("image") MultipartFile image,
        HttpServletRequest request) {

        PostModel post = new PostModel(id, title, Arrays.stream(tags.split(" ")).toList(), text);
        postService.editPost(post, image, request.getServletContext().getRealPath(""));

        return REDIRECT_POSTS + "/" + id;
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

        return REDIRECT_POSTS + "/" + id;
    }

    @PostMapping("/posts/{id}/delete")
    public String handleDeletePost(
        @PathVariable("id") Long id) {

        postService.deletePost(id);

        return REDIRECT_POSTS;
    }

    @PostMapping("/posts/{id}/comments")
    public String handlAddComment(
        @PathVariable("id") Long postId,
        @RequestParam("text") String text) {


        CommentModel comment = new CommentModel();
        comment.setPostId(postId);
        comment.setText(text);

        commentsService.saveComment(comment);

        return REDIRECT_POSTS + "/" + postId;
    }

    @PostMapping("/posts/{id}/comments/{commentId}/delete")
    public String handlAddComment(
        @PathVariable("id") Long postId,
        @PathVariable("commentId") Long commentId) {

        commentsService.removeComment(commentId);

        return REDIRECT_POSTS + "/" + postId;
    }

    @PostMapping("/posts/{id}/comments/{commentId}")
    public String handlEditComment(
        @PathVariable("id") Long postId,
        @PathVariable("commentId") Long commentId,
        @RequestParam("text") String text) {

        CommentModel comment = new CommentModel(commentId, postId, text);

        commentsService.editComment(comment);

        return REDIRECT_POSTS + "/" + postId;
    }
}
