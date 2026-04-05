package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
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
				.getContentAsString(StandardCharsets.UTF_8);

		Film created = objectMapper.readValue(response, Film.class);
		assertThat(created.getId()).isNotNull();
		assertThat(created.getName()).isEqualTo("Name");

		mockMvc.perform(get("/films"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$..id", hasItem(created.getId().intValue())));
	}

	@Test
	void partialUpdate_changesOnlySentFields() throws Exception {
		Film full = new Film();
		full.setName("Original");
		full.setDescription("Desc");
		full.setReleaseDate(LocalDate.of(2000, 1, 1));
		full.setDuration(100);

		String createdJson = mockMvc.perform(post("/films")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(full)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString(StandardCharsets.UTF_8);

		Film created = objectMapper.readValue(createdJson, Film.class);

		Film patch = new Film();
		patch.setId(created.getId());
		patch.setDuration(200);

		mockMvc.perform(put("/films")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(patch)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Original"))
				.andExpect(jsonPath("$.duration").value(200));
	}

	@Test
	void putUnknownId_returnsNotFound() throws Exception {
		Film film = new Film();
		film.setId(999L);
		film.setName("Name");
		film.setDescription("D");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(100);

		mockMvc.perform(put("/films")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(film)))
				.andExpect(status().isNotFound())
				.andExpect(result -> assertThat(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
						.contains("не найден"));
	}
}
