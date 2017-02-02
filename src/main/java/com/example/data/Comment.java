package com.example.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.rest.core.annotation.RestResource;

@Entity
@IdClass(Comment.CommentPk.class)
@Data
@EqualsAndHashCode(exclude = "post")
@ToString(exclude = "post")
public class Comment {

	@Id
	private long id;

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@RestResource(exported = false)
	@JsonIgnore
	private Post post;

	private String content;

	@Data
	static class CommentPk implements Serializable {
		private long id;

		private Post post;
	}
}
