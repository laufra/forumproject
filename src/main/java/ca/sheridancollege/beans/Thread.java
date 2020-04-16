package ca.sheridancollege.beans;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Thread {
	private long threadId;
	private String author;
	private String name;
	private int numOfPosts;
	
	public Thread(String author, String name) {
		this.author = author;
		this.name = name;
	}
}
