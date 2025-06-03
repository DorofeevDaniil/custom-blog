package ru.custom.blog.repository;

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

@Repository
public class JdbcNativePostRepository implements PostRepository{

    private final JdbcTemplate jdbcTemplate;

    private static final String SELECT_ALL = """
                                            select id, title, text, image_path, likes_count, tags
                                            from posts
                                            order by id desc
                                        """;
    private static final String SELECT_ALL_TAG = """
                                            select id, title, text, image_path, likes_count, tags
                                            from posts
                                            where 
                                                tags like concat('% ', ?, ' %')
                                            or
                                                tags like concat(?, ' %')
                                            or
                                                tags like concat('% ', ?)
                                            or
                                                tags like ?
                                            order by id desc
                                        """;

    private static final String SELECT_POST = """
                                            select id, title, text, image_path, likes_count, tags
                                            from posts
                                            where id = ?
                                        """;
    private static final String SELECT_IMAGE = "select image_path from posts where id = ?";
    private static final String INSERT_ROW = """
                                        insert into posts(title, text, image_path, likes_count, tags) 
                                        values (?, ?, ?, ?, ?)
                                    """;
    private static final String UPDATE_LIKES = "update posts set likes_count = likes_count";

    private static final String SELECT_COUNT = "select count(*) as cnt from posts";
    private static final String UPDATE_POST = """
                                UPDATE posts
                                SET 
                                     title = ?
                                    ,text = ?
                                    ,image_path = ?
                                    ,tags = ?
                                WHERE id = ?
                            """;
    private static final String DELETE_POST = "delete from posts where id = ?";

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
        String selectQuery = SELECT_ALL + String.format(" limit %d offset %d", limit, offset);

        return jdbcTemplate.query(selectQuery,
            (rs, rowNum) -> {
                try {
                    return populatePost(rs);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    @Override
    public List<PostModel> findPageByTag(String tag, Integer limit, Integer offset) {
        String selectQuery = SELECT_ALL_TAG + String.format(" limit %d offset %d", limit, offset);

        return jdbcTemplate.query(
            selectQuery,
            (PreparedStatement statement) -> {
                statement.setString(1, tag);
                statement.setString(2, tag);
                statement.setString(3, tag);
                statement.setString(4, tag);
            },
            (rs, rowNum) -> {
                try {
                    return populatePost(rs);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
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
    public String findImageById(Long id) {
        return jdbcTemplate.query(
            SELECT_IMAGE,
            (PreparedStatement ps) -> ps.setLong(1, id),
            (rs, rowNum) -> rs.getString(IMAGE_PATH_FIELD)
        ).get(0);
    }

    @Override
    public PostModel findPostById(Long id) {
        return jdbcTemplate.queryForObject(SELECT_POST,
            (rs, rowNum) -> {
                try {
                    return populatePost(rs);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, id);
    }

    @Override
    public void incrementLikesCount(Long id) {
        String query = UPDATE_LIKES + " + 1 " + "where id = ?";
        jdbcTemplate.update(query, id);
    }

    @Override
    public void decrementLikesCount(Long id) {
        String query = UPDATE_LIKES + " - 1 " + "where id = ?";
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

    private PostModel populatePost(ResultSet resultSet) throws SQLException, IOException {
        return new PostModel(
            resultSet.getLong(ID_FIELD),
            resultSet.getString(TITLE_FIELD),
            transformBlob(resultSet.getBlob(TEXT_FIELD)),
            resultSet.getString(IMAGE_PATH_FIELD),
            resultSet.getInt(LIKES_COUNT_FIELD),
            Arrays.stream(resultSet.getString("tags").split(" ")).toList()
        );
    }

    private String transformBlob(Blob blob) throws SQLException, IOException {
        StringBuilder builder = new StringBuilder();

        BufferedReader br = new BufferedReader(
            new InputStreamReader(blob.getBinaryStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line).append(System.lineSeparator());
            }
        }

        return builder.toString();
    }
}
