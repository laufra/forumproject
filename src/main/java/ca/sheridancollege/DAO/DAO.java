package ca.sheridancollege.DAO;


import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import ca.sheridancollege.EmailService;
import ca.sheridancollege.beans.Category;
import ca.sheridancollege.beans.Following;
import ca.sheridancollege.beans.Post;
import ca.sheridancollege.beans.Reward;
import ca.sheridancollege.beans.User;
import ca.sheridancollege.beans.UserProfile;
import ca.sheridancollege.beans.Thread;

@Repository
public class DAO {
	
	@Autowired
	protected JdbcTemplate jdbc;
	
	@Autowired
	private EmailService es;
	
	
	//Initialization
	//_________________________________________________________________________//
	public void initialize() {
		//Creating initial users
		addUser("Admin", "Password", "email@gmail.com");
		addRole(1,3);
		addUser("Moderator", "Password", "email@gmail.com");
		addRole(2,2);
		addUser("User", "Password", "email@gmail.com");
		addRole(3,1);
		
		
		//Creating a post for each forum
		Thread t1 = new Thread("Admin", "What is jQuery?");
		Post p1 = new Post(1, "I really don't know.");
		newThread(t1, p1, 1);
		
		Thread t2 = new Thread("Moderator", "Why isn't my code compiling?");
		Post p2 = new Post(2, "Edit -- Sorry forgot a semi-colon.");
		newThread(t2, p2, 2);
		
		Thread t3 = new Thread("User", "Is HTML a Programming Language?");
		Post p3 = new Post(3, "I definately think it is.");
		newThread(t3, p3, 3);
		
		Thread t4 = new Thread("Moderator", "I was going to make myself a belt out of watches ...");
		Post p4 = new Post(2, "But then I realized it would be a WAIST of time.");
		newThread(t4, p4, 4);
		
		Thread t5 = new Thread("User", "I just blew my college funds on a Black Lotus");
		Post p5 = new Post(3, ":(");
		newThread(t5, p5, 5);
		
		//Add Follower
		addFollower("Admin","User");
        addFollower("User","Admin");
        addFollower("User","Moderator");
        addFollower("Moderator","User");
		
		//Create Rewards
		addReward("Registered", "Registered with the website!");
        addReward("Confirmed", "Verified your email!");
        addReward("Threadsmith", "Create your first thread!");
        Reward r = new Reward(1, 1);
        Reward r2 = new Reward(2, 1);
        Reward r3 = new Reward(3, 1);
        Reward r4 = new Reward(1, 2);
        Reward r5 = new Reward(2, 2);
        Reward r6 = new Reward(3, 2);
        Reward r7 = new Reward(1, 3);
        Reward r8 = new Reward(2, 3);
        Reward r9 = new Reward(3, 3);
        addUserReward(r);
        addUserReward(r2);
        addUserReward(r3);
        addUserReward(r4);
        addUserReward(r5);
        addUserReward(r6);
        addUserReward(r7);
        addUserReward(r8);
        addUserReward(r9);
	}
	//_________________________________________________________________________//
	
	
	//User Methods
	//_________________________________________________________________________//
	public static String encryptPassword(String password) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder.encode(password);
	}
	
	public User getUserByUsername(String user_name) {
		String q = "SELECT * FROM users WHERE username='" + user_name +"'";
		Map<String, Object> row = this.jdbc.queryForMap(q);
		long uid = (Long) row.get("user_id");
		User user = null;
		try {
			user = new User(uid, 
								 (String) row.get("username"),
								 (String) row.get("encryptedPassword"),
								 (String) row.get("email"),
								 getNumOfPostsByUserId(uid),
								 (String) row.get("bio"),
								 (String) row.get("signature"),
								 (Boolean) row.get("verified"),
								 (Boolean) row.get("enabled"),
								 (String) getPictureByBlob((byte[]) row.get("profilePicture")),
								 (Boolean) row.get("hasProfilePicture"),
								 (String) row.get("privacy"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return user;
	}
	
	public User getUserById(long id) {
		//String q = ;
		Map<String, Object> row = this.jdbc.queryForMap("SELECT * FROM users WHERE user_id=" + id);
		User user = null;
		long uid = (Long) row.get("user_id");
		try {
			user = new User(uid, 
								 (String) row.get("username"),
								 (String) row.get("encryptedPassword"),
								 (String) row.get("email"),
								 getNumOfPostsByUserId(uid),
								 (String) row.get("bio"),
								 (String) row.get("signature"),
								 (Boolean) row.get("verified"),
								 (Boolean) row.get("enabled"),
								 (String) getPictureByBlob((byte[]) row.get("profilePicture")),
								 (Boolean) row.get("hasProfilePicture"),
								 (String) row.get("privacy"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return user;
	}
	
	public void addRole(long uid, long roleid) {
        String query = "INSERT INTO user_roles (user_id, role_id) VALUES (?,?)"; 
		Object[] params = new Object[] { uid, roleid };
		jdbc.update(query, params);
	}
	
	public ArrayList<String> getRoleNames(Long uid){
		ArrayList<String> roles = new ArrayList<String>();
		String query = "SELECT r.role_name FROM roles r "
					 + "INNER JOIN user_roles ur ON ur.role_id=r.role_id "
					 + "INNER JOIN users u ON u.user_id=ur.user_id "
					 + "WHERE u.user_id=" + uid; 
		List<Map<String, Object>> rows = this.jdbc.queryForList(query);
		for(Map<String, Object> row : rows) {
			roles.add((String) row.get("role_name"));
		}
		return roles;
	}
	
	public void enableUser(long id) {
		String q = "UPDATE users SET enabled=b'1' WHERE user_id=" + id;
		this.jdbc.update(q);
	}
	
	public void addUser(String user_name, String password, String email) {
		String q = "INSERT INTO users (username, encryptedPassword, email) VALUES (?,?,?)";
		Object[] p = {
				user_name,
				encryptPassword(password),
				email
		};
		this.jdbc.update(q,p);
		try {
			es.sendConfirmation(getUserByUsername(user_name));
		} catch(MailException e){
			System.out.println(e);
		}
	}
	
	public void confirmEmail(Long id) {
		String q = "UPDATE users SET verified=b'1' WHERE user_id=" + id;
		this.jdbc.update(q);
	}
	
	public void modifyUserWithPicture(String bio, String signature, byte[] profilePicture, String privacy, long uid) {
		String q = "UPDATE users SET bio=?, signature=?, profilePicture=?, privacy=?, hasProfilePicture=b'1' WHERE user_id=?";
		Object[] params = new Object[] {bio, signature, profilePicture, privacy, uid};
		this.jdbc.update(q,params);
	}
	
	public void modifyUser(String bio, String signature, String privacy, long uid) {
		String q = "UPDATE users SET bio=?, signature=?, privacy=? WHERE user_id=?";
		Object[] params = new Object[] {bio, signature, privacy, uid,};
		this.jdbc.update(q,params);
		System.out.println("We used the one without picture");
	}
	public String getMainRoleOfUser(long userId) {
		DAO dao = new DAO();
		ArrayList<String> roles = dao.getRoleNames(userId);
		for(String s: roles) {
			if (s.equals("MODERATOR")) {
				return "Moderator";
			} else if (s.equals("ADMIN")){
				return "Admin";
			}
		}
		
		return "User";
	}
	
	public String getPictureByBlob(byte[] profileBlob) throws SQLException {
			return "data:image/jpg;base64," + Base64.encodeBase64String(profileBlob);

	}
	
	public void addRewardForUser(long uid, long rewardId) {
		String q = "INSERT INTO user_rewards (reward_id, user_id) VALUES (?,?)";
		Object[] params = {rewardId, uid};
		this.jdbc.update(q, params);
	}
	//_________________________________________________________________________//
	
	//Forum Methods
	//_________________________________________________________________________//
	public ArrayList<Thread> getThreadsByCategoryId(long id){
		ArrayList<Thread> threads = new ArrayList<Thread>();
		List<Map<String, Object>> rows = this.jdbc.queryForList("SELECT * FROM thread WHERE category_id=" + id);
		for (Map<String, Object> r : rows) {
			long threadId = (Long) r.get("thread_id");
			long uid = (Long) r.get("user_id");
			User author = getUserById(uid);
			String threadName = (String) r.get("thread_name");
			int numOfPosts = getNumOfPostsByThreadId(threadId);
			Thread t = new Thread(threadId, author.getUsername(), threadName, numOfPosts);
			threads.add(t);
		}
		return threads;
	}
	
	public int getNumOfThreadsByCategoryId(long id) {
		List<Map<String, Object>> rows = this.jdbc.queryForList("SELECT * FROM thread WHERE category_id=" + id);
		int n = 0;
		for(Map<String, Object> r : rows) {
			n++;
		}
		return n;
	}
	
	public int getNumOfPostsByCategoryId(long id) {
		int n = 0;
		String q = "SELECT * FROM post "
				 + "INNER JOIN thread t ON post.thread_id=t.thread_id "
				 + "INNER JOIN category ON t.category_id=category.category_id "
				 + "WHERE category.category_id=" + id;
		List<Map<String, Object>> rows = this.jdbc.queryForList(q);
		for(Map<String, Object> r : rows) {
			n++;
		}
		return n;
	}
	
	public ArrayList<Post> getPostsByThreadId(long id){
		ArrayList<Post> posts = new ArrayList<Post>();
		
		String q = "SELECT * FROM post "
				 + "WHERE thread_id=" + id;
		List<Map<String, Object>> rows = this.jdbc.queryForList(q);
		for(Map<String, Object> r : rows) {
			Post post = null;
			long user_id = (Long) r.get("user_id");
			long post_id = (Long) r.get("post_id");
			User usr = getUserById(user_id);
			try {
				post = new Post(
						  post_id,
						  user_id,
						  usr.getUsername(),
						  (String) r.get("post_text"),
						  (Timestamp) r.get("post_time"),
						  getPictureByBlob((byte[]) r.get("attachedPicture")),
						  (Boolean) r.get("hasPicture"),
						  getNumOfPostsByUserId(user_id),
						  usr.getProfilePicture(),
						  usr.isHasProfilePicture(),
						  usr.getSignature());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			posts.add(post);
		}
		return posts;
	}
	
	public int getNumOfPostsByThreadId(long id) {
		String q = "SELECT * FROM post WHERE thread_id="+id;
		List<Map<String, Object>> rows = this.jdbc.queryForList(q);
		int n = 0;
		for(Map<String, Object> r : rows) {
			n++;
		}
		return n;
	}
	
	
	public long getIdOfLastPostInCategory(long categoryId) {
		String q = "SELECT * FROM thread WHERE category_id=" + categoryId + " ORDER BY thread_id DESC LIMIT 1";
		Map<String, Object> r = this.jdbc.queryForMap(q);
		return (Long) r.get("thread_id");
	}
	
	public ArrayList<Category> getCategories(){
		ArrayList<Category> categories = new ArrayList<Category>();
		List<Map<String, Object>> rows = this.jdbc.queryForList("SELECT * FROM category");
		for(Map<String, Object> r : rows) {
			Long cId = (Long) r.get("category_id");
			String name = (String) r.get("category_name");
			int numOfThreads = getNumOfThreadsByCategoryId(cId);
			int numOfPosts = getNumOfPostsByCategoryId(cId);
			long lastThread = getIdOfLastPostInCategory(cId);
			Category c = new Category(cId, name, numOfThreads, numOfPosts, lastThread);
			categories.add(c);
		}
		return categories;
	}
	
	public long getThreadIdOfLastPostByUserId(long id) {
		String q = "SELECT thread_id FROM thread WHERE user_id=" + id + " ORDER BY thread_id DESC LIMIT 1";
		Map<String, Object> r = this.jdbc.queryForMap(q);
		return (Long) r.get("thread_id");
	}
	
	public void newThread(Thread t, Post p, long categoryId) {
		String q1 = "INSERT INTO thread (user_id, category_id, thread_name) VALUES (?,?,?)";
		Object[] params1 = { p.getUser_id(), categoryId, t.getName() };
		this.jdbc.update(q1, params1);
		long thread_id = getThreadIdOfLastPostByUserId(p.getUser_id());
		String q2 = "INSERT INTO post (user_id, thread_id, post_text, post_time) VALUES (?,?,?, {fn NOW()})";
		Object[] params2 = { p.getUser_id(), thread_id, p.getPost_text() };
		this.jdbc.update(q2, params2);
		System.out.println("This is a post without an image");
	}
	
	public void newThreadWithPicture(Thread t, Post p, MultipartFile f, long category_id) {
		if(isFirstThread(p.getUser_id())) {
			addRewardForUser(p.getUser_id(), 3);
		}
		String q1 = "INSERT INTO thread (user_id, category_id, thread_name) VALUES (?,?,?)";
		Object[] params1 = { p.getUser_id(), category_id, t.getName() };
		this.jdbc.update(q1, params1);
		long thread_id = getThreadIdOfLastPostByUserId(p.getUser_id());
		String q2 = "INSERT INTO post (user_id, thread_id, post_text, post_time, attachedPicture, hasPicture) VALUES (?,?,?, {fn NOW()},?, b'1')";
		try {
			Object[] params2 = { p.getUser_id(), thread_id, p.getPost_text(), f.getBytes() };
			this.jdbc.update(q2, params2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("This is a post with an image");
	}
	
	public boolean isFirstThread(long uid) {
		String testQ = "SELECT * FROM thread WHERE user_id=" + uid;
		List<Map<String, Object>> rows = this.jdbc.queryForList(testQ);
		for(Map<String, Object> r: rows) {
			return false;
		}
		return true;
	}
	public void newPost(long thread_id, Post p) {
		String q = "INSERT INTO post (user_id, thread_id, post_text, post_time) VALUES (?,?,?, {fn NOW()})";
		Object[] param = { p.getUser_id(), thread_id, p.getPost_text()};
		this.jdbc.update(q, param);
	}
	
	public void newPostWithPicture(long thread_id, Post p, MultipartFile f) {
		String q = "INSERT INTO post (user_id, thread_id, post_text, post_time, attachedPicture, hasPicture) VALUES (?,?,?, {fn NOW()}, ?, b'1')";
		try {
			Object[] param = { p.getUser_id(), thread_id, p.getPost_text(), f.getBytes()};
			this.jdbc.update(q, param);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Thread getThreadById(long id) {
		String q = "SELECT * FROM thread WHERE thread_id=" + id;
		Map<String, Object> r = this.jdbc.queryForMap(q);
		User usr = getUserById((Long) r.get("user_id"));
		long thread_id = (Long) r.get("thread_id");
		Thread t = new Thread(thread_id, usr.getUsername(), (String) r.get("thread_name"), getNumOfPostsByThreadId(thread_id));
		return t;
	}
	
	public long getThreadIdByPostId(long id) {
		String q = "SELECT * FROM post INNER JOIN thread ON post.thread_id=thread.thread_id WHERE post.post_id=" + id + " LIMIT 1";
		Map<String, Object> r = this.jdbc.queryForMap(q);
		long thread_id = (Long) r.get("thread_id");
		return thread_id;
	}
	
	public int getNumOfPostsByUserId(long id) {
		List<Map<String, Object>> rows = this.jdbc.queryForList("SELECT * FROM post WHERE user_id=" + id);
		int n = 0;
		for(Map<String, Object> r : rows) {
			n++;;
		}
		return n;
	}
	
	public long getCategoryIdFromPostId(long id) {
		Map<String, Object> r = this.jdbc.queryForMap("SELECT thread.category_id FROM post INNER JOIN thread ON post.thread_id=thread.thread_id WHERE post_id="+id);
		return (Long) r.get("category_id");
	}
	
	public long getCategoryIdFromThreadId(long id) {
		Map<String, Object> r = this.jdbc.queryForMap("SELECT category_id FROM thread WHERE thread_id="+id);
		return (Long) r.get("category_id");
	}
	public void deletePostById(long id) {
		this.jdbc.update("DELETE FROM post WHERE post_id="+id);
	}
	
	public void deleteThreadById(long id) {
		this.jdbc.update("DELETE FROM post WHERE thread_id="+id);
		this.jdbc.update("DELETE FROM thread WHERE thread_id="+id);
	}
	//_________________________________________________________________________//
	
	//Reward Methods//
	//_________________________________________________________________________//
	
	public void addReward(String rewardName, String rewardDesc) {
		String q = "INSERT INTO reward (reward_name, reward_description) VALUES(?,?)";
		Object[] params = {rewardName, rewardDesc};
		this.jdbc.update(q,params);
	}
	
	public void addUserReward(Reward r) {
		String q = "INSERT INTO user_rewards(reward_id,user_id) VALUES(?,?)";
		Object[] params = {r.getReward_id(), r.getUser_id()};
		this.jdbc.update(q,params);
	}
	
	public ArrayList<Reward> getRewardsByUserId(long id) {
		ArrayList<Reward> rewards = new ArrayList<Reward>();
		String q = "SELECT reward.reward_name, reward.reward_description " + 
				"FROM reward " + 
				"LEFT JOIN user_rewards ON reward.reward_id = user_rewards.reward_id " + 
				"Where user_id=" + id;
		List<Map<String, Object>> row = this.jdbc.queryForList(q);
		for(Map<String, Object> r : row) {
			Reward reward = new Reward((String) r.get("reward_name"), (String) r.get("reward_description"));
			rewards.add(reward);
		}
		
		return rewards;
	}
	
	//TODO:
	
	public ArrayList<Long> getThreadsByUserId(long uid){
		ArrayList<Long> threads = new ArrayList<Long>();
		String q = "SELECT * FROM thread WHERE user_id=" + uid;
		List<Map<String, Object>> rows = this.jdbc.queryForList(q);
		for(Map<String, Object> r : rows) {
			threads.add((Long) r.get("thread_id"));
		}
		return threads;
	}
	
	public void disableUser(long uid) {
		ArrayList<Long> threadIds = getThreadsByUserId(uid);
		for(long thread_id : threadIds) {
			deleteThreadById(thread_id);
		}
		String q = "UPDATE users SET enabled=b'0' WHERE user_id=" + uid;
		this.jdbc.update(q);
	}
	public void checkForRewards() {
		
	}
	//_________________________________________________________________________//
	
	//Tracking Methods//
	//_________________________________________________________________________//
		
	public void addFollower(String username, String followUser) {
        String q = "INSERT into followers (follower,followee) VALUES(?,?)";
        User user = this.getUserByUsername(username);
        User followedUser = this.getUserByUsername(followUser);
        Object[] params = {user.getUid(),followedUser.getUid()};
        this.jdbc.update(q,params);
    }
    
	public ArrayList<Following> getListFollowing(long id){
        ArrayList<Following> followingList = new ArrayList<Following>();
        String q = "SELECT followers.follower, followers.followee, users.username FROM followers"
                + " LEFT JOIN users ON followers.followee = users.user_id WHERE follower="+id;
        List<Map<String, Object>> rows = this.jdbc.queryForList(q);
        for (Map<String, Object> r : rows) {
            long follower = (long) r.get("follower");
            long followee = (long) r.get("followee");
            String username = (String) r.get("username");
            User usr = getUserById(followee);
            String q1 = "SELECT thread_id, post_time FROM post WHERE user_id=" + usr.getUid() + " ORDER BY post_time DESC LIMIT 1";
            Map<String, Object> tempRow = this.jdbc.queryForMap(q1);
            Thread t = getThreadById((Long) tempRow.get("thread_id"));
            Following following = new Following(follower,followee,username, t.getThreadId(), t.getName(), (Timestamp) tempRow.get("post_time"));
            followingList.add(following);
            System.out.println(following.toString());
        }
        return followingList;
    }
	
	public Following getFollowing(long id) {
		User user = this.getUserById(id);
		String q ="SELECT follower, followee FROM followers WHERE followee=" + user.getUid();
		Map<String, Object> r = this.jdbc.queryForMap(q);
		long followerId = (long) r.get("follower");
		long followeeId = (long) r.get("followee");
		Following follow = new Following(followerId, followeeId);
		return follow;
	}
	
	public UserProfile getProfileTrackingThread(long id) {
    	String q = "SELECT thread.thread_id, post_time, thread.thread_name, users.username, users.user_id\r\n" + 
    			"FROM post INNER JOIN thread ON\r\n" + 
    			"post.thread_id=thread.thread_id\r\n" + 
    			"INNER JOIN users ON\r\n" + 
    			"post.user_id = users.user_id WHERE post.user_id =" + id+" ORDER BY post_time DESC LIMIT 1";
		Map<String, Object> r = this.jdbc.queryForMap(q);
		long threadId = (long) r.get("thread_id");
		Timestamp postTime = (Timestamp) r.get("post_time");
		String threadName = (String) r.get("thread_name");
		String username = (String) r.get("username");
		long uid = (long) r.get("user_id");
		UserProfile profile = new UserProfile(threadId,postTime,threadName,username,uid);
    	
    	return profile;
    }
    
    public ArrayList<UserProfile> getUserProfileLatestThread(Following follow){
    	ArrayList<UserProfile> latestThread = new ArrayList<UserProfile>();
    	String q = "SELECT thread.thread_id, post.post_time, thread.thread_name FROM post INNER JOIN thread ON post.thread_id = thread.thread_id \r\n" + 
    			"WHERE post.user_id=" + follow.getFollowee() + " ORDER BY post_time DESC";
    	List<Map<String, Object>> rows = this.jdbc.queryForList(q);
    	for (Map<String, Object> r: rows) {
    		long threadId = (Long) r.get("thread_id");
    		Timestamp postTime = (Timestamp) r.get("post_time");
    		String threadName = (String) r.get("thread_name");
    		UserProfile profile = new UserProfile(threadId,postTime,threadName);
    		latestThread.add(profile);
    	}
    	return latestThread;
    	
    }
	//_________________________________________________________________________//


	public boolean isFollowing(long id, Long uid) {
		String q = "SELECT * FROM followers WHERE follower=" + uid + " AND followee=" + id;
		List<Map<String, Object>> rows = this.jdbc.queryForList(q);
		for(Map<String, Object> r : rows) {
			return true;
		}
		return false;
	}
}
