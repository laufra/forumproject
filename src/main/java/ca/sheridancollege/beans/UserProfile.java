package ca.sheridancollege.beans;

import java.sql.Timestamp;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserProfile {

	private Long uid;
	private String username;
	private String profilePic;
	private Long numOfPosts;
	private String bio;
	private String signature;
	private long threadId;
	private Timestamp postTime;
	private String threadName;
	
	public UserProfile(long threadId, Timestamp postTime, String threadName, String username, long uid) {
		this.username = username;
		this.uid = uid;
		this.threadId = threadId;
		this.postTime = postTime;
		this.threadName = threadName;
	}

	public UserProfile(long threadId, Timestamp postTime, String threadName) {
		this.threadId = threadId;
		this.postTime = postTime;
		this.threadName = threadName;
	}
	
	
	
}
