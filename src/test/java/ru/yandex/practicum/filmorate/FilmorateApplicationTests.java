package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.sql.init.mode=always",
	"spring.sql.init.schema-locations=classpath:schema.sql",
	"spring.sql.init.data-locations=classpath:data.sql"
})
class FilmorateApplicationTests {

	@Test
	void contextLoads() {
	}

}
