package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Qualifier("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

	private final Map<Long, User> users = new ConcurrentHashMap<>();
	private final AtomicLong idGenerator = new AtomicLong(1);

	@Override
	public User create(User user) {
		user.setId(idGenerator.getAndIncrement());
		users.put(user.getId(), user);
		return user;
	}

	@Override
	public User update(User user) {
		users.put(user.getId(), user);
		return user;
	}

	@Override
	public void delete(Long userId) {
		users.remove(userId);
	}

	@Override
	public List<User> findAll() {
		return new ArrayList<>(users.values());
	}

	@Override
	public Optional<User> findById(Long userId) {
		return Optional.ofNullable(users.get(userId));
	}

	@Override
	public void addFriend(Long userId, Long friendId) {
		User user = users.get(userId);
		if (user != null) {
			user.getFriends().add(friendId);
		}
	}

	@Override
	public void removeFriend(Long userId, Long friendId) {
		User user = users.get(userId);
		if (user != null) {
			user.getFriends().remove(friendId);
		}
	}
}
