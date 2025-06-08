package ru.custom.blog.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.custom.blog.model.CommentModel;
import ru.custom.blog.model.PostModel;
import ru.custom.blog.repository.PostRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final CommentService commentService;
    private final ImageService imageService;

    public PostService(PostRepository postRepository, CommentService commentService, ImageService imageService) {
        this.postRepository = postRepository;
        this.commentService = commentService;
        this.imageService = imageService;
    }

    public long getTotalPostsCount() {
        return postRepository.getTotalCount();
    }

    public Long savePost(PostModel post, MultipartFile imageFile, String basePath) {
        String filePath = imageService.saveImage(imageFile, basePath);
        post.setImagePath(filePath);

        return postRepository.save(post);
    }

    public void editPost(PostModel post, MultipartFile imageFile, String basePath) {
        Optional<String> previousImagePath = postRepository.findImageById(post.getId());

        if (!imageFile.getOriginalFilename().isEmpty()) {
            previousImagePath.ifPresent(imageService::removeImage);
            post.setImagePath(imageService.saveImage(imageFile, basePath));
        } else {
            previousImagePath.ifPresent(post::setImagePath);
        }

        postRepository.update(post);
    }

    public List<PostModel> getPage(Integer pageNumber, Integer pageSize) {
        List<PostModel> posts = postRepository.findPage(pageSize, (pageNumber - 1) * pageSize);

        return posts.stream().map(this::getPostComments).toList();
    }

    public List<PostModel> getPageByTag(String tagName, Integer pageSize) {
        List<PostModel> posts =  postRepository.findPageByTag(tagName, pageSize, 0);
        return posts.stream().map(this::getPostComments).toList();
    }

    public PostModel getPost(Long id) {
        PostModel post = postRepository.findPostById(id).orElseGet(PostModel::new);
        return getPostComments(post);
    }

    public void updateLikesCount(Long id, boolean like) {
        if (like) {
            postRepository.incrementLikesCount(id);
        } else {
            postRepository.decrementLikesCount(id);
        }
    }

    public void deletePost(Long id) {
        commentService.deleteAllPostComments(id);
        postRepository.deleteById(id);
    }

    private PostModel getPostComments(PostModel post) {
        List<CommentModel> comments = commentService.getByPostId(post.getId());
        post.setComments(comments);
        return post;
    }

    public void savePost(PostModel post) {
        postRepository.save(post);
    }
}
