package ru.custom.blog.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.custom.blog.model.CommentModel;

import java.util.List;

@Repository
public class JdbcNativeCommentRepository implements CommentRepository {
    private static final String SELECT_ALL = "select id, text from comments where post_id = ?";

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativeCommentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public List<CommentModel> findByPostId(Long postId) {
        return jdbcTemplate.query(
            SELECT_ALL,
            (rs, rowNum) -> new CommentModel(
                rs.getLong("id"),
                rs.getString("text")
            ),
            postId);
    }

    @Override
    public Long save(CommentModel user) {
        return null;
    }

    @Override
    public void deleteById(Long id) {
        //для дальнейшей реализации
    }

    @Override
    public Long getTotalCount() {
        return null;
    }
}
