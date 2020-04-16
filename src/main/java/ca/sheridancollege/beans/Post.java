package ca.sheridancollege.beans;

import java.sql.Timestamp;
import lombok.*;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Post {
	private long post_id;
	private long user_id;
	private String username;
	private String post_text;
	private Timestamp post_time;
	private String postPhoto;
	private boolean photo;
	private int numOfPosts;
	private String profilePicture;
	private boolean picture;
	private String signature;
	
	public Post(long user_id, String post_text) {
		super();
		this.user_id = user_id;
		this.post_text = post_text;
	}
}
