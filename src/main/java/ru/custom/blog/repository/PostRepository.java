package ru.custom.blog.repository;

import ru.custom.blog.model.PostModel;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    List<PostModel> findPage(Integer limit, Integer offset);
    List<PostModel> findPageByTag(String tag, Integer limit, Integer offset);
    Optional<PostModel> findPostById(Long id);
    Long save(PostModel post);
    void update(PostModel post);
    void deleteById(Long id);
    Long getTotalCount();
    Optional<String> findImageById(Long l);
    void incrementLikesCount(Long id);
    void decrementLikesCount(Long id);
}
