package ru.custom.blog.service;

import org.springframework.stereotype.Service;
import ru.custom.blog.model.CommentModel;
import ru.custom.blog.repository.JdbcNativeCommentRepository;

import java.util.List;

@Service
public class CommentsService {

    private final JdbcNativeCommentRepository commentRepository;

    public CommentsService(JdbcNativeCommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<CommentModel> getByPostId(Long postId) {
        return commentRepository.findByPostId(postId);
    }

}
