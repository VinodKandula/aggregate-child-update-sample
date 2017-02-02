package com.example.data;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.SpringApplicationConfiguration;
//import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
//@WebIntegrationTest
//@SpringApplicationConfiguration(AggregateChildUpdateSampleApplication.class)
public class AggregateChildUpdateSampleApplicationTests {

	private static final String CREATE_POST_JSON = "{ \"title\": \"My first post\" }";
	private static final String REPLACE_COMMENTS_JSON_PATCH = "[{\"op\":\"add\", \"path\":\"/comments\", \"value\":[{\"content\": \"Cool post!\"}]}]";
	private static final String ADD_COMMENT_JSON_PATCH = "[{\"op\":\"add\", \"path\":\"/comments/-\", \"value\":{\"content\": \"Cool post\"}}]";
	private static final String JSON_PATCH_CONTENT_TYPE = "application/json-patch+json";

	private MockMvc mockMvc;

	@Autowired
	private WebApplicationContext wac;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders
				.webAppContextSetup(this.wac)
				.build();
	}

	@Test
	public void canReplaceCommentsWithPatch() throws Exception {
		String location = createPost();

		this.mockMvc.perform(
				patch(location)
						.contentType(JSON_PATCH_CONTENT_TYPE)
						.content(REPLACE_COMMENTS_JSON_PATCH))
				.andExpect(status().is2xxSuccessful());
	}

	@Test
	public void canAppendCommentWithPatch() throws Exception {
		String location = createPost();

		this.mockMvc.perform(
				patch(location)
						.contentType(JSON_PATCH_CONTENT_TYPE)
						.content(ADD_COMMENT_JSON_PATCH))
				.andExpect(status().is2xxSuccessful());
	}

	private String createPost() throws Exception {
		return this.mockMvc.perform(
				post("/posts")
						.contentType(MediaType.APPLICATION_JSON_UTF8)
						.content(CREATE_POST_JSON))
				.andReturn()
				.getResponse()
				.getHeader("Location");
	}

}
