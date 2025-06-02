package ru.custom.blog.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.custom.blog.model.PostModel;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcNativePostRepository implements PostRepository{

    private final JdbcTemplate jdbcTemplate;
    private static final String SELECT_ALL = """
                                            select id, title, text, image_path, likes_count, tags
                                            from posts
                                            order by id desc
                                        """;

    private static final String SELECT_POST = """
                                            select id, title, text, image_path, likes_count, tags
                                            from posts
                                            where id = ?
                                        """;
    public static final String SELECT_IMAGE = "select image_path from posts where id = ?";
    public static final String INSERT_ROW = """
                                        insert into posts(title, text, image_path, likes_count, tags) 
                                        values (?, ?, ?, ?, ?)
                                    """;
    public static final String SELECT_COUNT = "select count(*) as cnt from posts";

    public JdbcNativePostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<PostModel> findPage(Integer limit, Integer offset) {
        String selectQuery = SELECT_ALL + String.format(" limit %d offset %d", limit, offset);

        return jdbcTemplate.query(selectQuery,
            (rs, rowNum) -> new PostModel(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("text"),
                rs.getString("image_path"),
                rs.getInt("likes_count"),
                Arrays.stream(rs.getString("tags").split(" ")).toList()
            ));
    }

    @Override
    public Long save(PostModel post) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_ROW, Statement.RETURN_GENERATED_KEYS);

            statement.setString(1, post.getTitle());
            statement.setString(2, post.getText());
            statement.setString(3, post.getImagePath());
            statement.setInt(4, post.getLikesCount());
            statement.setString(5, String.join(" ", post.getTags()));

            return statement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public String findImageById(Long id) {
        return jdbcTemplate.queryForObject(
            SELECT_IMAGE, new Object[]{id}, String.class);
    }

    @Override
    public PostModel findPostById(Long id) {
        return jdbcTemplate.queryForObject(SELECT_POST,
            (rs, rowNum) -> new PostModel(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("text"),
                rs.getString("image_path"),
                rs.getInt("likes_count"),
                Arrays.stream(rs.getString("tags").split(" ")).toList()
            ), id);
    }

    @Override
    public void deleteById(Long id) {
        //для дальнейшей реализации
    }

    @Override
    public Long getTotalCount() {
        return jdbcTemplate.query(SELECT_COUNT, (rs, rowNum) -> rs.getLong("cnt")).get(0);
    }
}
