package ru.custom.blog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.custom.blog.model.CommentModel;
import ru.custom.blog.service.CommentService;

@Controller
public class CommentController {
    private final CommentService commentService;

    private static final String REDIRECT_POSTS = "redirect:/posts";

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/posts/{id}/comments")
    public String handleAddComment(
        @PathVariable("id") Long postId,
        @RequestParam("text") String text) {

        CommentModel comment = new CommentModel();
        comment.setPostId(postId);
        comment.setText(text);

        commentService.saveComment(comment);

        return REDIRECT_POSTS + "/" + postId;
    }

    @PostMapping("/posts/{id}/comments/{commentId}/delete")
    public String handleDeleteComment(
        @PathVariable("id") Long postId,
        @PathVariable("commentId") Long commentId) {

        commentService.removeComment(commentId);

        return REDIRECT_POSTS + "/" + postId;
    }

    @PostMapping("/posts/{id}/comments/{commentId}")
    public String handleEditComment(
        @PathVariable("id") Long postId,
        @PathVariable("commentId") Long commentId,
        @RequestParam("text") String text) {

        CommentModel comment = new CommentModel(commentId, postId, text);

        commentService.editComment(comment);

        return REDIRECT_POSTS + "/" + postId;
    }
}
