package ca.sheridancollege.beans;

import java.sql.Timestamp;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Following {

    private long follower, followee;
    private String username;
    private long lastThreadId;
    private String lastThreadName;
    private Timestamp lastThreadPostTime;
    
    public Following(long follower, long followee) {
        
        this.follower = follower;
        this.followee = followee;
    }

    public Following(long followee, String username) {
        this.followee = followee;
        this.username = username;
    }
    
    
}