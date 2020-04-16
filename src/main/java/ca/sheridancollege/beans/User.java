package ca.sheridancollege.beans;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
	public Long uid;
	public String username;
	public String encryptedPassword;
	public String email;
	public int numOfPosts;
	public String bio;
	public String signature;
	public boolean verified;
	public boolean enabled;
	public String profilePicture;
	public boolean hasProfilePicture;
	public String privacy;
	
	public User(String username, String email, String bio, String signature) {
	
		this.username = username;
		this.email = email;
		this.bio = bio;
		this.signature = signature;
	}
	
	
}
