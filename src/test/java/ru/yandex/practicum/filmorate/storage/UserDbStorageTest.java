package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Sql(scripts = {"/schema.sql", "/data.sql"})
class UserDbStorageTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private UserDbStorage userStorage;

	@AfterEach
	void tearDown() {
		jdbcTemplate.execute("DELETE FROM friends");
		jdbcTemplate.execute("DELETE FROM users");
	}

	private UserDbStorage getUserStorage() {
		if (userStorage == null) {
			userStorage = new UserDbStorage(jdbcTemplate);
		}
		return userStorage;
	}

    @Test
    public void testCreateUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = getUserStorage().create(user);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo("test@example.com");
        assertThat(created.getLogin()).isEqualTo("testlogin");
    }

    @Test
    public void testFindUserById() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User created = getUserStorage().create(user);

        Optional<User> userOptional = getUserStorage().findById(created.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u -> assertThat(u.getId()).isEqualTo(created.getId()));
    }

    @Test
    public void testFindAllUsers() {
        User user1 = new User();
        user1.setEmail("test1@example.com");
        user1.setLogin("testlogin1");
        user1.setName("Test User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        getUserStorage().create(user1);

        User user2 = new User();
        user2.setEmail("test2@example.com");
        user2.setLogin("testlogin2");
        user2.setName("Test User 2");
        user2.setBirthday(LocalDate.of(1995, 1, 1));
        getUserStorage().create(user2);

        List<User> users = getUserStorage().findAll();

        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User created = getUserStorage().create(user);

        created.setEmail("updated@example.com");
        created.setName("Updated Name");
        User updated = getUserStorage().update(created);

        assertThat(updated.getEmail()).isEqualTo("updated@example.com");
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    public void testDeleteUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User created = getUserStorage().create(user);

        getUserStorage().delete(created.getId());

        Optional<User> userOptional = getUserStorage().findById(created.getId());
        assertThat(userOptional).isEmpty();
    }

    @Test
    public void testAddFriend() {
        User user1 = new User();
        user1.setEmail("test1@example.com");
        user1.setLogin("testlogin1");
        user1.setName("Test User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User created1 = getUserStorage().create(user1);

        User user2 = new User();
        user2.setEmail("test2@example.com");
        user2.setLogin("testlogin2");
        user2.setName("Test User 2");
        user2.setBirthday(LocalDate.of(1995, 1, 1));
        User created2 = getUserStorage().create(user2);

        getUserStorage().addFriend(created1.getId(), created2.getId());

        Optional<User> userOptional = getUserStorage().findById(created1.getId());
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u -> assertThat(u.getFriends()).contains(created2.getId()));
    }

    @Test
    public void testRemoveFriend() {
        User user1 = new User();
        user1.setEmail("test1@example.com");
        user1.setLogin("testlogin1");
        user1.setName("Test User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User created1 = getUserStorage().create(user1);

        User user2 = new User();
        user2.setEmail("test2@example.com");
        user2.setLogin("testlogin2");
        user2.setName("Test User 2");
        user2.setBirthday(LocalDate.of(1995, 1, 1));
        User created2 = getUserStorage().create(user2);

        getUserStorage().addFriend(created1.getId(), created2.getId());
        getUserStorage().removeFriend(created1.getId(), created2.getId());

        Optional<User> userOptional = getUserStorage().findById(created1.getId());
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u -> assertThat(u.getFriends()).doesNotContain(created2.getId()));
    }
}
