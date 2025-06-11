package ru.custom.blog.integration.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.custom.blog.repository.CommentRepository;
import ru.custom.blog.repository.PostRepository;

@SpringBootTest
@ActiveProfiles("test")
abstract class BaseRepositoryTest {
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected PostRepository postRepository;

    @Autowired
    protected CommentRepository commentRepository;
}
