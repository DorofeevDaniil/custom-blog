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

    public void savePost(PostModel post, MultipartFile imageFile, String basePath) {
        String filePath = saveFile(imageFile, basePath);
        post.setImagePath(filePath);

        long postId = postRepository.save(post);
        //set tags
    }

    public List<PostModel> getPage(Integer pageNumber, Integer pageSize) {
        List<PostModel> posts = postRepository.findPage(pageSize, (pageNumber - 1) * pageSize);

        return posts.stream().map(this::getPostComments).toList();
    }

    public PostModel getPost(Long id) {
        PostModel post = postRepository.findPostById(id);
        return getPostComments(post);
    }

    private String saveFile(MultipartFile imageFile, String basePath) {
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

    private PostModel getPostComments(PostModel post) {
        List<CommentModel> comments = commentsService.getByPostId(post.getId());
        post.setComments(comments);
        return post;
    }

    public void savePost(PostModel post) {
        long postId = postRepository.save(post);
    }
}
