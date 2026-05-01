package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Sql(scripts = {"/schema.sql", "/data.sql"})
class MpaDbStorageTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private MpaDbStorage mpaDbStorage;

	private MpaDbStorage getMpaStorage() {
		if (mpaDbStorage == null) {
			mpaDbStorage = new MpaDbStorage(jdbcTemplate);
		}
		return mpaDbStorage;
	}

    @Test
    public void testFindAllMpa() {
        List<Mpa> mpas = getMpaStorage().findAll();

        assertThat(mpas).isNotEmpty();
        assertThat(mpas).hasSize(5);
    }

    @Test
    public void testFindMpaById() {
        Optional<Mpa> mpaOptional = getMpaStorage().findById(1L);

        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa -> {
                    assertThat(mpa.getId()).isEqualTo(1L);
                    assertThat(mpa.getName()).isEqualTo("G");
                });
    }

    @Test
    public void testFindMpaByIdNotFound() {
        Optional<Mpa> mpaOptional = getMpaStorage().findById(999L);

        assertThat(mpaOptional).isEmpty();
    }

    @Test
    public void testMpaNamesMatchMPAA() {
        List<Mpa> mpas = getMpaStorage().findAll();

        assertThat(mpas).hasSize(5);
        assertThat(mpas).extracting("name").containsExactlyInAnyOrder("G", "PG", "PG-13", "R", "NC-17");
    }
}
