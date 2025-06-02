package ru.custom.blog.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.custom.blog.model.CommentModel;
import ru.custom.blog.model.PostModel;
import ru.custom.blog.repository.PostRepository;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class PostService {
    private static final Logger logger = LoggerFactory.getLogger(PostService.class);
    private final PostRepository postRepository;
    private final CommentsService commentsService;

    public PostService(PostRepository postRepository, CommentsService commentsService) {
        this.postRepository = postRepository;
        this.commentsService = commentsService;
    }

    public long getTotalPostsCount() {
        return postRepository.getTotalCount();
    }

    //addComment

    //addTags

    public Long savePost(PostModel post, MultipartFile imageFile, String basePath) {
        String filePath = saveImage(imageFile, basePath);
        post.setImagePath(filePath);

        return postRepository.save(post);
    }

    public void editPost(PostModel post, MultipartFile imageFile, String basePath) {
        String previousImagePath = postRepository.findImageById(post.getId());


        String filePath = saveImage(imageFile, basePath);
        post.setImagePath(filePath);
        postRepository.update(post);
    }

    public List<PostModel> getPage(Integer pageNumber, Integer pageSize) {
        List<PostModel> posts = postRepository.findPage(pageSize, (pageNumber - 1) * pageSize);

        return posts.stream().map(this::getPostComments).toList();
    }

    public PostModel getPost(Long id) {
        PostModel post = postRepository.findPostById(id);
        return getPostComments(post);
    }

    public void updateLikesCount(Long id, boolean like) {
        if (like) {
            postRepository.incrementLikesCount(id);
        } else {
            postRepository.decrementLikesCount(id);
        }
    }

    private String saveImage(MultipartFile imageFile, String basePath) {
        String uploadDir = basePath + "images";

        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }

        String filePath = uploadDir + File.separator + imageFile.getOriginalFilename();

        File destinationFile = new File(filePath);

        try {
            imageFile.transferTo(destinationFile);
        } catch (IOException e) {
            logger.error("Файл не записан");
        }

        return filePath;
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    private PostModel getPostComments(PostModel post) {
        List<CommentModel> comments = commentsService.getByPostId(post.getId());
        post.setComments(comments);
        return post;
    }

    public void savePost(PostModel post) {
        postRepository.save(post);
    }
}
