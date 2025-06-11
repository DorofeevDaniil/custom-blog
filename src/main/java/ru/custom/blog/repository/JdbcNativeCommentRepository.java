package ru.custom.blog.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.custom.blog.model.CommentModel;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class JdbcNativeCommentRepository implements CommentRepository {
    private static final String SELECT_ALL = "SELECT id, post_id, text FROM comments WHERE post_id = ? ORDER BY id ASC";
    private static final  String INSERT_COMMENT = "INSERT INTO comments(post_id, text) VALUES (?, ?)";
    private static final String UPDATE_COMMENT = "UPDATE comments SET text = ? WHERE id = ?";

    private static final String DELETE_COMMENT = "DELETE FROM comments WHERE id = ?";
    private static final String DELETE_POST_COMMENTS = "DELETE FROM comments WHERE post_id = ?";

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
    public Long save(CommentModel comment) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
            connection -> {
                PreparedStatement statement = connection.prepareStatement(INSERT_COMMENT, Statement.RETURN_GENERATED_KEYS);

                statement.setLong(1, comment.getPostId());
                statement.setString(2, comment.getText());

                return statement;
            }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public void update(CommentModel comment) {
        jdbcTemplate.update(UPDATE_COMMENT,
            comment.getText(),
            comment.getId()
        );
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update(DELETE_COMMENT, id);
    }

    @Override
    public void deleteByPostId(Long postId) {
        jdbcTemplate.update(DELETE_POST_COMMENTS, postId);
    }
}
