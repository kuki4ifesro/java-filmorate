package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void postEmptyBody_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/films")
						.contentType(MediaType.APPLICATION_JSON)
						.content(""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void postInvalidFilm_returnsBadRequest() throws Exception {
		Film film = new Film();
		film.setName("");
		film.setDescription("x");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(1);

		mockMvc.perform(post("/films")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(film)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").exists());
	}

	@Test
	void postValidFilm_returnsCreatedEntity() throws Exception {
		Film film = new Film();
		film.setName("Name");
		film.setDescription("D");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(100);

		String response = mockMvc.perform(post("/films")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(film)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").exists())
				.andReturn()
				.getResponse()
				.getContentAsString();

		Film created = objectMapper.readValue(response, Film.class);
		assertThat(created.getId()).isNotNull();
		assertThat(created.getName()).isEqualTo("Name");

		mockMvc.perform(get("/films"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(created.getId()))
				.andExpect(jsonPath("$[0].name").value("Name"));
	}

	@Test
	void putUnknownId_returnsBadRequest() throws Exception {
		Film film = new Film();
		film.setId(999L);
		film.setName("Name");
		film.setDescription("D");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(100);

		mockMvc.perform(put("/films")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(film)))
				.andExpect(status().isBadRequest())
				.andExpect(result -> assertThat(result.getResponse().getContentAsString()).contains("не найден"));
	}
}
