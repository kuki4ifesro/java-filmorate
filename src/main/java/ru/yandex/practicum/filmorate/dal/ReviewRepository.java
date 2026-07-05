package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewRepository {

    private final JdbcTemplate jdbc;
    private final ReviewRowMapper reviewRowMapper;

    private static final String SELECT_REVIEW_WITH_USEFUL = """
            SELECT r.review_id,
                   r.content,
                   r.is_positive,
                   r.user_id,
                   r.film_id,
                   COALESCE(SUM(CASE
                       WHEN rr.is_like = TRUE THEN 1
                       WHEN rr.is_like = FALSE THEN -1
                       ELSE 0
                   END), 0) AS useful
            FROM reviews r
            LEFT JOIN review_reactions rr ON r.review_id = rr.review_id
            """;

    private static final String GROUP_BY_REVIEW = """
            GROUP BY r.review_id, r.content, r.is_positive, r.user_id, r.film_id
            """;

    public Review create(Review review) {
        String sql = """
                INSERT INTO reviews (content, is_positive, user_id, film_id)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, review.getContent());
            statement.setBoolean(2, review.getIsPositive());
            statement.setLong(3, review.getUserId());
            statement.setLong(4, review.getFilmId());
            return statement;
        }, keyHolder);

        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        return getById(review.getReviewId()).orElseThrow();
    }

    public boolean update(Review review) {
        String sql = """
                UPDATE reviews
                SET content = ?,
                    is_positive = ?
                WHERE review_id = ?
                """;

        return jdbc.update(
                sql,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        ) > 0;
    }

    public boolean deleteById(Long reviewId) {
        String sql = """
                DELETE FROM reviews
                WHERE review_id = ?
                """;

        return jdbc.update(sql, reviewId) > 0;
    }

    public Optional<Review> getById(Long reviewId) {
        String sql = SELECT_REVIEW_WITH_USEFUL + """
                WHERE r.review_id = ?
                """ + GROUP_BY_REVIEW;

        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, reviewRowMapper, reviewId));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public List<Review> getReviews(Long filmId, Integer count) {
        if (filmId == null) {
            String sql = SELECT_REVIEW_WITH_USEFUL
                    + GROUP_BY_REVIEW
                    + """
                    ORDER BY useful DESC, r.review_id
                    LIMIT ?
                    """;

            return jdbc.query(sql, reviewRowMapper, count);
        }

        String sql = SELECT_REVIEW_WITH_USEFUL + """
                WHERE r.film_id = ?
                """ + GROUP_BY_REVIEW + """
                ORDER BY useful DESC, r.review_id
                LIMIT ?
                """;

        return jdbc.query(sql, reviewRowMapper, filmId, count);
    }

    public boolean existsById(Long reviewId) {
        String sql = """
                SELECT COUNT(*)
                FROM reviews
                WHERE review_id = ?
                """;

        Integer count = jdbc.queryForObject(sql, Integer.class, reviewId);
        return count != null && count > 0;
    }

    public void addReaction(Long reviewId, Long userId, boolean isLike) {
        String sql = """
                MERGE INTO review_reactions (review_id, user_id, is_like)
                KEY (review_id, user_id)
                VALUES (?, ?, ?)
                """;

        jdbc.update(sql, reviewId, userId, isLike);
    }

    public void deleteReaction(Long reviewId, Long userId, boolean isLike) {
        String sql = """
                DELETE FROM review_reactions
                WHERE review_id = ?
                  AND user_id = ?
                  AND is_like = ?
                """;

        jdbc.update(sql, reviewId, userId, isLike);
    }
}