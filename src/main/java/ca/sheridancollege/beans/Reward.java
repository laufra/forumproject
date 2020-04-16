package ca.sheridancollege.beans;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Reward {
	private long id;
	private long reward_id;
	private String reward_name;
	private long user_id;
	private String reward_description;
	
	public Reward(long reward_id, String reward_name, long user_id) {
	
		this.reward_id = reward_id;
		this.reward_name = reward_name;
		this.user_id = user_id;
		
	}
	
	public Reward(String reward_name, String reward_description) {
		this.reward_name = reward_name;
		this.reward_description = reward_description;
	}
	public Reward(long reward_id, String reward_name) {
		
		this.reward_id = reward_id;
		this.reward_name = reward_name;
	}

	public Reward(long reward_id, long user_id) {
		this.reward_id = reward_id;
		this.user_id = user_id;
	}

	
	
	
}
