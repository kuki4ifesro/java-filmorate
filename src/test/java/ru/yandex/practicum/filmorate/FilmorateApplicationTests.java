package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.config.TestStorageConfig;

@SpringBootTest(properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb"
})
@Import(TestStorageConfig.class)
class FilmorateApplicationTests {

	@Test
	void contextLoads() {
	}

}
