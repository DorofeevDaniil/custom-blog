package ru.custom.blog.service;

import org.springframework.stereotype.Service;
import ru.custom.blog.model.CommentModel;
import ru.custom.blog.repository.CommentRepository;

import java.util.List;

@Service
public class CommentsService {

    private final CommentRepository commentRepository;

    public CommentsService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<CommentModel> getByPostId(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    public void saveComment(CommentModel comment) {
        commentRepository.save(comment);
    }

    public void removeComment(Long id) {
        commentRepository.deleteById(id);
    }

    public void editComment(CommentModel comment) {
        commentRepository.update(comment);
    }

    public void deleteAllPostComments(Long postId) {
        commentRepository.deleteByPostId(postId);
    }

}
