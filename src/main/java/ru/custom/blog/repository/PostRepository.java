package ru.custom.blog.repository;

import ru.custom.blog.model.PostModel;

import java.util.List;

public interface PostRepository {
    List<PostModel> findPage(Integer limit, Integer offset);
    PostModel findPostById(Long id);
    Long save(PostModel post);
    void update(PostModel post);
    Long getTotalCount();
    String findImageById(Long l);
    void incrementLikesCount(Long id);
    void decrementLikesCount(Long id);

}
