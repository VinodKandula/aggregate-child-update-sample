package com.example.data;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import lombok.Data;

@Entity
@Data
public class Post {

	@Id
	@GeneratedValue
	private long id;

	@OneToMany(mappedBy = "post", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Comment> comments = new HashSet<>();

	private String title;

	@PrePersist
	private void maintainParentBackReference() {
		for (Comment comment : this.comments) {
			comment.setPost(this);
		}
	}
}
