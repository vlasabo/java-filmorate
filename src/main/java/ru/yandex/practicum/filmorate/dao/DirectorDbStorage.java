package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Director get(int id) {
        String sql = "SELECT * FROM DIRECTORS WHERE ID = ?";
        Director director;
        try {
            director = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeDirector(rs), id);
        } catch (DataAccessException e){
            log.debug("Director with id={} not found", id);
            throw new NotFoundException("Director with id=" + id + " not found.");
        }

        log.debug("Director received by id={}", id);

        return director;
    }

    @Override
    public Director update(Director director) {
        String sql = "UPDATE DIRECTORS SET NAME = ? WHERE ID = ?";
        int updatedCount = jdbcTemplate.update(sql, director.getName(), director.getId());

        if (updatedCount == 0){
            log.debug("Director with id={} not found", director.getId());
            throw new NotFoundException("Director with id=" + director.getId() + " not found");
        }

        log.debug("Director with id={} updated", director.getId());

        return director;
    }

    @Override
    public Director add(Director director) {
        String sql = "INSERT INTO DIRECTORS (NAME) VALUES(?)";
        KeyHolder key = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, key);

        director.setId(Objects.requireNonNull(key.getKey()).intValue());

        log.debug("Added director with id={}", director.getId());

        return director;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM DIRECTORS WHERE ID = ?";
        int deletedCount = jdbcTemplate.update(sql, id);

        if (deletedCount == 0){
            log.debug("Director with id={} not found", id);
            throw new NotFoundException("Director with id=" + id + " not found");
        }

        log.debug("Director with id={} removed", id);

    }

    @Override
    public List<Director> getAll() {
        String sql = "SELECT * FROM DIRECTORS";

        List<Director> directors = jdbcTemplate.query(sql, (rs, rowNum) -> makeDirector(rs));

        log.debug("Directors received");

        return directors;
    }

    private Director makeDirector(ResultSet rs) throws SQLException {
        Director director = new Director();
        director.setId(rs.getInt("id"));
        director.setName(rs.getString("name"));

        return director;
    }
}
