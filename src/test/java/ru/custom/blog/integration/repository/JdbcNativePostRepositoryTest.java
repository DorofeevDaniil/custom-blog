package ru.custom.blog.integration.repository;

import org.junit.jupiter.api.Test;
import ru.custom.blog.model.PostModel;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class JdbcNativePostRepositoryTest extends BaseRepositoryTest {

    @Override
    protected void additionalSetup() {
        IDLE_POSTS.clear();

        populatePosts();
    }

    @Test
    void getTotalCount_shouldReturnPostsCount() {
        long postsCount = postRepository.getTotalCount();

        assertEquals(3, postsCount);
    }

    @Test
    void savePostById_shouldReturnPost() {
        PostModel post = postRepository.findPostById(IDLE_POSTS.get(0).getId()).orElseGet(PostModel::new);

        assertNotNull(post);
        assertEquals(IDLE_POSTS.get(0), post);
    }

    @Test
    void save_shouldAddPost() {
        PostModel model = populateTestPost();
        long count = postRepository.getTotalCount();

        Long postId = postRepository.save(model);
        model.setId(postId);

        long updatedCount = postRepository.getTotalCount();
        PostModel savedPost = postRepository.findPostById(model.getId()).orElseGet(PostModel::new);

        assertEquals(count + 1, updatedCount);
        assertNotNull(savedPost);
        assertEquals(model, savedPost);
    }

    @Test
    void findPage_shouldReturnIdlePosts_reverseOrder() {
        List<PostModel> posts = postRepository.findPage(10, 0);

        posts.sort((o1, o2) -> Math.toIntExact(o1.getId() - o2.getId()));

        assertNotNull(posts);
        assertFalse(posts.isEmpty());

        for (int i = 0; i < IDLE_POSTS.size(); i++) {
            assertEquals(IDLE_POSTS.get(i), posts.get(i));
            assertEquals(IDLE_POSTS.get(i), posts.get(i));
            assertEquals(IDLE_POSTS.get(i), posts.get(i));
        }
    }

    @Test
    void findPageByTag_shouldReturnPostByTag() {
        List<PostModel> posts = postRepository.findPageByTag(IDLE_POSTS.get(0).getTags().get(0), 10, 0);

        assertNotNull(posts);
        assertFalse(posts.isEmpty());
        assertEquals(1, posts.size());
        assertEquals(IDLE_POSTS.get(0), posts.get(0));
    }

    @Test
    void update_shouldUpdatePost() {
        PostModel newPost = populateTestPost();
        newPost.setId(1L);

        postRepository.update(newPost);

        PostModel resultPost = postRepository.findPostById(newPost.getId()).orElseGet(PostModel::new);

        assertNotNull(resultPost.getId());
        assertEquals(newPost, resultPost);
    }

    @Test
    void findImageById_shouldReturnImage() {
        Optional<String> imagePath = postRepository.findImageById(IDLE_POSTS.get(0).getId());

        assertTrue(imagePath.isPresent());
        assertEquals(IDLE_POSTS.get(0).getImagePath(), imagePath.get());
    }

    @Test
    void incrementLikesCount_shouldAddLike() {
        PostModel idlePost = postRepository.findPostById(IDLE_POSTS.get(0).getId()).orElseGet(PostModel::new);
        Integer idleLikesCount = idlePost.getLikesCount();

        postRepository.incrementLikesCount(IDLE_POSTS.get(0).getId());

        PostModel updatedPost = postRepository.findPostById(IDLE_POSTS.get(0).getId()).orElseGet(PostModel::new);
        Integer resultLikesCount = updatedPost.getLikesCount();

        assertNotNull(resultLikesCount);
        assertEquals(idleLikesCount + 1, resultLikesCount);
    }

    @Test
    void decrementLikesCount_shouldAddLike() {
        PostModel idlePost = postRepository.findPostById(IDLE_POSTS.get(0).getId()).orElseGet(PostModel::new);
        Integer idleLikesCount = idlePost.getLikesCount();

        postRepository.decrementLikesCount(IDLE_POSTS.get(0).getId());

        PostModel updatedPost = postRepository.findPostById(IDLE_POSTS.get(0).getId()).orElseGet(PostModel::new);
        Integer resultLikesCount = updatedPost.getLikesCount();

        assertNotNull(resultLikesCount);
        assertEquals(idleLikesCount - 1, resultLikesCount);
    }

    @Test
    void deleteById_shouldDeletePost() {
        long count = postRepository.getTotalCount();

        postRepository.deleteById(IDLE_POSTS.get(0).getId());

        long updatedCount = postRepository.getTotalCount();

        Optional<PostModel> deletedPost = postRepository.findPostById(IDLE_POSTS.get(0).getId());

        assertFalse(deletedPost.isPresent());
        assertEquals(count - 1, updatedCount);
    }

    private PostModel populateTestPost() {
        PostModel model = new PostModel();

        model.setTitle("test4");
        model.setText("example string 4");
        model.setImagePath("path4");
        model.setLikesCount(1);
        model.setTags(List.of("some4","tag4"));

        return model;
    }
}
