package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb"
})
@AutoConfigureMockMvc
@Import(ru.yandex.practicum.filmorate.config.TestStorageConfig.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void postEmptyBody_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void postUserWithBlankName_usesLoginAsName() throws Exception {
		User user = new User();
		user.setEmail("a@b.ru");
		user.setLogin("dolores");
		user.setName("");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(user)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("dolores"));
	}

	@Test
	void postUserWithNullName_usesLoginAsName() throws Exception {
		User user = new User();
		user.setEmail("a@b.ru");
		user.setLogin("dolores");
		user.setName(null);
		user.setBirthday(LocalDate.of(1990, 1, 1));

		String body = mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(user)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString(StandardCharsets.UTF_8);

		User created = objectMapper.readValue(body, User.class);
		assertThat(created.getName()).isEqualTo("dolores");
	}
}
