package ru.custom.blog.repository;

import ru.custom.blog.model.CommentModel;

import java.util.List;

public interface CommentRepository {
    List<CommentModel> findByPostId(Long postId);
    void save(CommentModel comment);
    void deleteById(Long id);
    void update(CommentModel comment);
}
