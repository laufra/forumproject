package ca.sheridancollege.beans;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Category {
	private long categoryId;
	private String name;
	private long totalThreads;
	private long totalPosts;
	private long lastThreadId;
}
