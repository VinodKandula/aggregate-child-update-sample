package com.example.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Comment {

	@Id
	@GeneratedValue
	private long id;

	private String content;
}
