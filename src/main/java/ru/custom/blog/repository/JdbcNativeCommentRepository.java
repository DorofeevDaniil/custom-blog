package ru.custom.blog.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.custom.blog.model.CommentModel;

import java.util.List;

@Repository
public class JdbcNativeCommentRepository implements CommentRepository {
    private static final String SELECT_ALL = "select id, post_id, text from comments where post_id = ?";
    private static final  String INSERT_COMMENT = "insert into comments(post_id, text) values (?, ?)";

    private static final String DELETE_COMMENT = "delete from comments where id = ?";

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
                rs.getLong("post_id"),
                rs.getString("text")
            ),
            postId);
    }

    @Override
    public void save(CommentModel comment) {
        jdbcTemplate.update(
            INSERT_COMMENT,
            comment.getPostId(),
            comment.getText()
        );
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update(DELETE_COMMENT, id);
    }

    @Override
    public Long getTotalCount() {
        return null;
    }
}
