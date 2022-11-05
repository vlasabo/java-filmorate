package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Review add(Review review) {

        String sqlCheck =   "SELECT 1 FROM dual WHERE " +
                            "    EXISTS(SELECT 1 FROM users WHERE id = ?) AND " +
                            "    EXISTS(SELECT 1 FROM FILMS where ID = ?)";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sqlCheck, review.getUserId(), review.getFilmId());
        rowSet.last();
        if (rowSet.getRow() == 0){
            log.debug("Incorrect userId or filmId.");
            throw new NotFoundException("Incorrect data.");
        }

        String sql = "INSERT INTO REVIEWS (CONTENT, ISPOSITIVE, USEFUL, USER_ID, FILM_ID) " +
                        "VALUES (?1, ?2, ?3, ?4, ?5)";

        KeyHolder key = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setInt(3, 0);
            ps.setInt(4, review.getUserId());
            ps.setInt(5, review.getFilmId());

            return ps;
            }, key);

        review.setId(Objects.requireNonNull(key.getKey()).intValue());

        log.debug("Added review with id={}", review.getId());

        return review;
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE REVIEWS " +
                     "SET CONTENT = ?, ISPOSITIVE = ? " +
                     "WHERE ID = ?";

        int updateCount;

        try {
            updateCount = jdbcTemplate.update(sql,
                    review.getContent(),
                    review.getIsPositive(),
                    review.getId());
        } catch (DataAccessException e){
            throw new NotFoundException(e.getMessage());
        }

        if (updateCount == 0){
            log.debug("Review with id={} not found", review.getId());
            throw new NotFoundException("Review with id="  + review.getId() + " not found");
        }

        log.debug("Updated review with id={}", review.getId());

        return review;
    }

    @Override
    public Review get(int id) {
        String sql = "SELECT * FROM REVIEWS WHERE ID = ?";

        List<Review> reviews = jdbcTemplate.query(sql,
                (rs, rowNum) -> makeReview(rs),
                id);

        if (reviews.size() == 0){
            log.debug("Review with id={} not found", id);
            throw new NotFoundException("Review with id="  + id + " not found");
        }

        log.debug("Review received with id={}", id);

        return reviews.get(0);
    }

    @Override
    public void remove(int id) {
        String sql =    "DELETE FROM LIKES_REVIEW WHERE review_id = ?1; " +
                        "DELETE FROM REVIEWS WHERE ID = ?1";

        jdbcTemplate.update(sql, id);

        log.debug("Removed review with id={}", id);
    }

    @Override
    public void like(int id, int userId) {
        setLikeOrDislike(id, userId, 1);

        log.debug("Liked for review with id={} from user id ={}", id, userId);

        updateUseful(id);
    }

    @Override
    public void dislike(int id, int userId) {
        setLikeOrDislike(id, userId, -1);

        log.debug("Disliked for review with id={} from user id ={}", id, userId);

        updateUseful(id);
    }

    @Override
    public void deleteLike(int id, int userId) {
        deleteLikeOrDislike(id, userId);

        log.debug("Deleted like for review with id={} from user id ={}", id, userId);

        updateUseful(id);
    }

    @Override
    public void deleteDislike(int id, int userId) {
        deleteLikeOrDislike(id, userId);

        log.debug("Deleted dislike for review with id={} from user id ={}", id, userId);

        updateUseful(id);
    }

    @Override
    public List<Review> getReviews(int count) {
        String sql = "SELECT * " +
                     "FROM REVIEWS " +
                     "ORDER BY USEFUL DESC " +
                     "LIMIT ?";

        log.debug("Received list of reviews with counter = {}", count);

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeReview(rs), count);
    }

    @Override
    public List<Review> getReviewsByFilm(int filmId, int count) {
        String sql = "SELECT * " +
                     "FROM REVIEWS " +
                     "WHERE FILM_ID = ?1 " +
                     "ORDER BY USEFUL DESC " +
                     "LIMIT ?2";

        log.debug("Received list of reviews by film id = {} with counter = {}", filmId, count);

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeReview(rs), filmId, count);
    }

    private void updateUseful(int id){

        String sql =    "SELECT " +
                        "    SUM(GRADE) AS grade " +
                        "FROM LIKES_REVIEW " +
                        "WHERE REVIEW_ID = ?";

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, id);
        rowSet.last();

        int useful = 0;
        if (rowSet.getRow() != 0){
            useful = rowSet.getInt("grade");
        }

        log.debug("Updated useful for review id = {}", id);

        jdbcTemplate.update("UPDATE REVIEWS SET USEFUL = ? WHERE ID = ?", useful, id);
    }

    private void setLikeOrDislike(int id, int userId, int value){
        String sql = "INSERT INTO LIKES_REVIEW(REVIEW_ID, USER_ID, GRADE)( " +
                "SELECT ?1, ?2, ?3 " +
                "FROM dual " +
                "WHERE NOT EXISTS ( " +
                "        SELECT 1 " +
                "        FROM LIKES_REVIEW " +
                "        WHERE REVIEW_ID = ?1 AND USER_ID = ?2 " +
                "    ))";

        jdbcTemplate.update(sql, id, userId, value);
    }

    private void deleteLikeOrDislike(int id, int userId){
        String sql = "DELETE FROM LIKES_REVIEW where REVIEW_ID = ?1 AND USER_ID = ?2";
        jdbcTemplate.update(sql, id, userId);
    }

    private Review makeReview(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setId(rs.getInt("id"));
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("isPositive"));
        review.setUserId(rs.getInt("user_id"));
        review.setFilmId(rs.getInt("film_id"));
        review.setUseful(rs.getInt("useful"));

        return review;
    }
}
