package ru.custom.blog.repository;

import ru.custom.blog.model.CommentModel;

import java.util.List;

public interface CommentRepository {
    List<CommentModel> findByPostId(Long postId);
    Long save(CommentModel comment);
    void deleteById(Long id);
    Long getTotalCount();
}
