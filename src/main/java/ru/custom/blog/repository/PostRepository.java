package ru.custom.blog.repository;

import ru.custom.blog.model.PostModel;

import java.util.List;

public interface PostRepository {
    List<PostModel> findPage(Integer limit, Integer offset);
    PostModel findPostById(Long id);
    Long save(PostModel post);
    void deleteById(Long id);
    Long getTotalCount();
    String findImageById(Long l);
}
