package ru.yandex.practicum.filmorate.dal.mappers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Slf4j
@Component
public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setName(resultSet.getString("name"));
        user.setEmail(resultSet.getString("email"));
        user.setLogin(resultSet.getString("login"));
        log.debug("Извлекли из БД: id={}, login из БД={}", user.getId(), resultSet.getString("login")); // Отладка
        Timestamp birthdayTs = resultSet.getTimestamp("birthday");
        if (birthdayTs != null) {
            user.setBirthday(birthdayTs.toLocalDateTime().toLocalDate());
        }
        return user;
    }
}