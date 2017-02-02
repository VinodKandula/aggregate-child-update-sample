package com.example.data;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AggregateCompositeKeyUpdateTests {

	private static final String POST_WITH_NO_COMMENTS_JSON = "{ \"title\": \"My first post\" }";
	private static final String POST_WITH_ONE_COMMENT_JSON = "{\"comments\": [{\"id\": 1, \"content\": \"hi\"}], \"title\": \"My first post\"}";

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void canCreatePostWithComment() throws Exception {
		this.mockMvc.perform(
				post("/posts")
						.contentType(MediaType.APPLICATION_JSON_UTF8)
						.content(POST_WITH_ONE_COMMENT_JSON))
				.andExpect(status().isCreated());
	}

	@Test
	public void canAddCommentWithPut() throws Exception {
		String location = createPost();

		this.mockMvc.perform(
				put(location)
						.contentType(MediaType.APPLICATION_JSON_UTF8)
						.content(POST_WITH_ONE_COMMENT_JSON))
				.andExpect(status().isOk());
	}


	private String createPost() throws Exception {
		return this.mockMvc.perform(
				post("/posts")
						.contentType(MediaType.APPLICATION_JSON_UTF8)
						.content(POST_WITH_NO_COMMENTS_JSON))
				.andReturn()
				.getResponse()
				.getHeader("Location");
	}
}
