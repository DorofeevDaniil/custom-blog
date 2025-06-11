package ru.custom.blog.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.custom.blog.model.PostModel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcNativePostRepository implements PostRepository{
    private static final Logger logger = LoggerFactory.getLogger(JdbcNativePostRepository.class);

    private final JdbcTemplate jdbcTemplate;

    private static final String SELECT_ALL = """
                                            SELECT 
                                                id, title, text, image_path, likes_count, tags
                                            FROM 
                                                posts
                                            ORDER BY id DESC
                                            LIMIT ? OFFSET ?
                                        """;
    private static final String SELECT_ALL_TAG = """
                                            SELECT 
                                                id, title, text, image_path, likes_count, tags
                                            FROM 
                                                posts
                                            WHERE 
                                                tags LIKE CONCAT('% ', ?, ' %')
                                            OR
                                                tags LIKE CONCAT(?, ' %')
                                            OR
                                                tags LIKE CONCAT('% ', ?)
                                            OR
                                                tags LIKE ?
                                            ORDER BY id DESC
                                            LIMIT ? OFFSET ?
                                        """;

    private static final String SELECT_POST = """
                                            SELECT 
                                                id, title, text, image_path, likes_count, tags
                                            FROM 
                                                posts
                                            WHERE 
                                                id = ?
                                        """;
    private static final String SELECT_IMAGE = "SELECT image_path FROM posts WHERE id = ?";
    private static final String INSERT_ROW = """
                                        INSERT INTO 
                                            posts(title, text, image_path, likes_count, tags) 
                                        VALUES 
                                            (?, ?, ?, ?, ?)
                                    """;
    private static final String UPDATE_LIKES = "UPDATE posts SET likes_count = likes_count";

    private static final String SELECT_COUNT = "SELECT COUNT(*) AS cnt FROM posts";
    private static final String UPDATE_POST = """
                                UPDATE 
                                    posts
                                SET 
                                     title = ?, text = ?, image_path = ?, tags = ?
                                WHERE 
                                    id = ?
                            """;
    private static final String DELETE_POST = "DELETE FROM posts WHERE id = ?";

    private static final String TITLE_FIELD = "title";
    private static final String IMAGE_PATH_FIELD = "image_path";
    private static final String LIKES_COUNT_FIELD = "likes_count";
    private static final String ID_FIELD = "id";
    private static final String TEXT_FIELD = "text";

    public JdbcNativePostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<PostModel> findPage(Integer limit, Integer offset) {
        return jdbcTemplate.query(SELECT_ALL,
            (PreparedStatement statement) -> {
                statement.setInt(1, limit);
                statement.setInt(2, offset);
            },
            (rs, rowNum) -> populatePost(rs));
    }

    @Override
    public List<PostModel> findPageByTag(String tag, Integer limit, Integer offset) {
        return jdbcTemplate.query(
            SELECT_ALL_TAG,
            (PreparedStatement statement) -> {
                statement.setString(1, tag);
                statement.setString(2, tag);
                statement.setString(3, tag);
                statement.setString(4, tag);
                statement.setInt(5, limit);
                statement.setInt(6, offset);
            },
            (rs, rowNum) -> populatePost(rs));
    }

    @Override
    public Long save(PostModel post) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_ROW, Statement.RETURN_GENERATED_KEYS);

            statement.setString(1, post.getTitle());
            statement.setBlob(2, new ByteArrayInputStream(post.getText().getBytes(StandardCharsets.UTF_8)));
            statement.setString(3, post.getImagePath());
            statement.setInt(4, post.getLikesCount());
            statement.setString(5, post.getTagsAsText());

            return statement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public void update(PostModel post) {
        jdbcTemplate.update(UPDATE_POST,
            post.getTitle(),
            new ByteArrayInputStream(post.getText().getBytes(StandardCharsets.UTF_8)),
            post.getImagePath(),
            post.getTagsAsText(),
            post.getId());
    }

    @Override
    public Optional<String> findImageById(Long id) {
        List<String> images =  jdbcTemplate.query(
            SELECT_IMAGE,
            (PreparedStatement ps) -> ps.setLong(1, id),
            (rs, rowNum) -> rs.getString(IMAGE_PATH_FIELD)
        );

        return images.stream().findFirst();
    }

    @Override
    public Optional<PostModel> findPostById(Long id) {
        List<PostModel> posts = jdbcTemplate.query(SELECT_POST,
            (rs, rowNum) -> populatePost(rs), id);

        return posts.stream().findFirst();
    }

    @Override
    public void incrementLikesCount(Long id) {
        String query = UPDATE_LIKES + " + 1 " + "WHERE id = ?";
        jdbcTemplate.update(query, id);
    }

    @Override
    public void decrementLikesCount(Long id) {
        String query = UPDATE_LIKES + " - 1 " + "WHERE id = ?";
        jdbcTemplate.update(query, id);
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update(DELETE_POST, id);
    }

    @Override
    public Long getTotalCount() {
        return jdbcTemplate.query(
            SELECT_COUNT,
            (rs, rowNum) -> rs.getLong("cnt")
        ).get(0);
    }

    private PostModel populatePost(ResultSet resultSet) throws SQLException {
        PostModel post = new PostModel();

        post.setId(resultSet.getLong(ID_FIELD));
        post.setTitle(resultSet.getString(TITLE_FIELD));
        post.setText(transformBlob(resultSet.getBlob(TEXT_FIELD)));
        post.setImagePath(resultSet.getString(IMAGE_PATH_FIELD));
        post.setLikesCount(resultSet.getInt(LIKES_COUNT_FIELD));
        post.setTags(Arrays.stream(resultSet.getString("tags").split(" ")).toList());

        return post;
    }

    private String transformBlob(Blob blob) throws SQLException {
        StringBuilder builder = new StringBuilder();

        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(blob.getBinaryStream(), StandardCharsets.UTF_8))) {

            int c;
            while ((c = br.read()) != -1) {
                builder.append((char) c);
            }

        } catch (IOException e) {
            logger.error(String.format("Failed to read blob. Got error: %s", e.getMessage()));
        }

        return builder.toString();
    }
}
