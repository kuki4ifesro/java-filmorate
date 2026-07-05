package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {
    private final UserRepository userRepository;
    private User testUser;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public UserRepository userRepository(JdbcTemplate jdbcTemplate) {
            return new UserRepository(jdbcTemplate, new UserRowMapper());
        }

        @Bean
        public UserRowMapper userRowMapper() {
            return new UserRowMapper();
        }
    }

    @BeforeEach
    void setUp() {
        // Создаём пользователя с полным набором параметров
        testUser = new User(
                null,
                "test@example.com",
                "testuser",
                "Test User",
                LocalDate.of(1990, 1, 1),
                new HashSet<>()
        );
        // Сохраняем и сохраняем ID созданного пользователя
        testUser = userRepository.createUser(testUser);
    }

    @AfterEach
    void tearDown() {
        // Очищаем БД после каждого теста
        userRepository.deleteAll();
    }

    @Test
    public void testFindUserById() {

        Optional<User> userOptional = userRepository.getById(1L);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
                );
    }


}