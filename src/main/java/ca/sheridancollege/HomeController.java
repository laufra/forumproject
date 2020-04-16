package ca.sheridancollege;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import ca.sheridancollege.DAO.DAO;
import ca.sheridancollege.beans.Following;
import ca.sheridancollege.beans.Post;
import ca.sheridancollege.beans.Reward;
import ca.sheridancollege.beans.Thread;
import ca.sheridancollege.beans.User;
import ca.sheridancollege.beans.UserProfile;

@Controller
public class HomeController {
	
	@Autowired
	protected DAO dao;
	
	@GetMapping("/register")
	public String goReg() {
		return "register.html";
	}
	
	@GetMapping("/login")
	public String goLog() {
		return "login.html";
	}
	
	@PostMapping("/register")
	public String doReg(Model m, @RequestParam String username, @RequestParam String password, @RequestParam String email) {
		dao.addUser(username, password, email);
		m.addAttribute("username", username);
		User u = dao.getUserByUsername(username);	
		dao.addRole(u.getUid(), 1);
		dao.addRewardForUser(u.getUid(), 1);
		return "confirmation.html";
	}
	
	@GetMapping("/initialize")
	public String loadUser() {
		dao.initialize();
		return "home.html";
	}
	
	@GetMapping("/")
	public String root(Model model){ 
		//dao.addUser("An Administrator", "Password", "mezorno@gmail.com");
		model.addAttribute("categories", dao.getCategories());
		return "home.html";
	}
	
	@GetMapping("/confirm")
	public String confirmEmail(Model m, @RequestParam long id) {
		dao.confirmEmail(id);
		m.addAttribute("username", dao.getUserById(id).getUsername());
		dao.addRewardForUser(id, 2);
		return "confirmed.html";
	}
	
	@GetMapping("/forum{id}")
	public String displayForums(Model m, @RequestParam int id) {
		m.addAttribute("threads", dao.getThreadsByCategoryId(id));
		m.addAttribute("category", id);
		return "category.html";
	}
	
	@GetMapping("/thread{id}")
	public String displayThread(Model m, @RequestParam int id, Authentication auth) {
		m.addAttribute("thread", dao.getThreadById(id));
		m.addAttribute("posts", dao.getPostsByThreadId(id));
		if(null != auth) {
			ArrayList<String> roles = new ArrayList<String>();
			for (GrantedAuthority ga: auth.getAuthorities()) {
				roles.add(ga.getAuthority());
			}
			
			for(String r : roles) {
				if (r.equals("MODERATOR")) {
					m.addAttribute("mainRole", "MODERATOR");
					return "thread.html";
				} else if (r.equals("ADMIN")){
					m.addAttribute("mainRole", "ADMIN");
					return "thread.html";
				}
			}
		}
		
		m.addAttribute("mainRole", " ");
		return "thread.html";
	}
	
	@GetMapping("/newThread{categoryId}")
	public String goNewThread(Model m, @RequestParam long categoryId) {
		m.addAttribute("categoryid", categoryId);
		return "newThread.html";
	}
	
	@PostMapping("/newThread")
	public String doNewThread(Model m, @RequestParam long categoryId, @RequestParam String threadName, @RequestParam String text, @RequestParam MultipartFile f, Authentication auth) {
		if(f.getSize() == 0) {
			User usr = dao.getUserByUsername(auth.getName());
			Thread t = new Thread(auth.getName(), threadName);
			Post p = new Post(usr.getUid(), text);
			dao.newThread(t, p, categoryId);
			long threadid = dao.getThreadIdOfLastPostByUserId(usr.getUid());
			m.addAttribute("thread", dao.getThreadById(threadid));
			m.addAttribute("posts", dao.getPostsByThreadId(threadid));
		} else {
			User usr = dao.getUserByUsername(auth.getName());
			Thread t = new Thread(auth.getName(), threadName);
			Post p = new Post(usr.getUid(), text);
			dao.newThreadWithPicture(t, p, f, categoryId);
			long threadid = dao.getThreadIdOfLastPostByUserId(usr.getUid());
			m.addAttribute("thread", dao.getThreadById(threadid));
			m.addAttribute("posts", dao.getPostsByThreadId(threadid));
		}
		return "thread.html";
	}
	
	@GetMapping("/promote")
	public String promote(Model m, @RequestParam long id, Authentication auth) {
		dao.addRole(id, 2);
		
		m.addAttribute("user", dao.getUserById(id));
		if(null != auth) {
			ArrayList<String> roles = new ArrayList<String>();
			for (GrantedAuthority ga: auth.getAuthorities()) {
				roles.add(ga.getAuthority());
			}
			
			for(String r : roles) {
				if (r.equals("MODERATOR")) {
					m.addAttribute("mainRole", "MODERATOR");
				} else if (r.equals("ADMIN")){
					m.addAttribute("mainRole", "ADMIN");
				}
			}
			m.addAttribute("isFollower", dao.isFollowing(id, dao.getUserByUsername(auth.getName()).getUid()));
		} else {
			m.addAttribute("mainRole", "ADMIN");
			m.addAttribute("isFollower", false);
		}
		m.addAttribute("rewards", dao.getRewardsByUserId(id));
		
		return "profile";
	}
	@GetMapping("/reply")
	public String goReply(Model m, @RequestParam long threadId) {
		m.addAttribute("thread", dao.getThreadById(threadId));
		return "newPost.html";
	}
	
	@PostMapping("/reply")
	public String doReply(Model m, @RequestParam long threadId, @RequestParam String text, Authentication auth, @RequestParam MultipartFile f) {
		User usr = dao.getUserByUsername(auth.getName());
		if(null == f) {
			Post p = new Post(usr.getUid(), text);
			dao.newPost(threadId, p);
			m.addAttribute("thread", dao.getThreadById(threadId));
			m.addAttribute("posts", dao.getPostsByThreadId(threadId));
		} else {
			Post p = new Post(usr.getUid(), text);
			dao.newPostWithPicture(threadId, p, f);
			m.addAttribute("thread", dao.getThreadById(threadId));
			m.addAttribute("posts", dao.getPostsByThreadId(threadId));
		}
		return "thread.html";
	}
	
	@GetMapping("/deletePost")
	public String deletePost(Model m, @RequestParam long id) {
		long categoryId = dao.getCategoryIdFromPostId(id);
		long tId = dao.getThreadIdByPostId(id);
		dao.deletePostById(id);
		ArrayList<Post> p = dao.getPostsByThreadId(tId);
		if(p.isEmpty()) {
			dao.deleteThreadById(tId);
		}
		m.addAttribute("threads", dao.getThreadsByCategoryId(categoryId));
		m.addAttribute("category", categoryId);
		return "category.html";
	}
	
	@GetMapping("/deleteThread")
	public String deleteThread(Model m, @RequestParam long id) {
		long categoryId = dao.getCategoryIdFromThreadId(id);
		dao.deleteThreadById(id);
		m.addAttribute("threads", dao.getThreadsByCategoryId(categoryId));
		m.addAttribute("category", categoryId);
		return "category.html";
	}
	@GetMapping("/account")
	public String goAccount(Model m, Authentication auth) {
		String authUser = auth.getName();
		User loggedUser = dao.getUserByUsername(authUser);
		m.addAttribute("user", loggedUser);
		m.addAttribute("rewards", dao.getRewardsByUserId(loggedUser.uid));
		return "/user/account.html";
	}
	

    @GetMapping("/modify")
    public String goModify(Model m, User user, Authentication auth) {
        user = dao.getUserByUsername(auth.getName());
        m.addAttribute("user",dao.getUserById(user.getUid()));
        return "/user/modify.html";
    }
    
    @PostMapping("/modify")
    public String doModify(Model m, @RequestParam MultipartFile f, @RequestParam String bio, @RequestParam String signature, @RequestParam String privacy, @RequestParam long uid) {
    	User updatedUser = null;
    	if(f.getSize() == 0) {
    		dao.modifyUser(bio, signature, privacy, uid);
    	} else {
    		try {
    			dao.modifyUserWithPicture(bio, signature, f.getBytes(), privacy, uid);
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}
    	updatedUser = dao.getUserById(uid);
    	m.addAttribute("user",updatedUser);
        m.addAttribute("rewards",dao.getRewardsByUserId(updatedUser.getUid()));
    	return "/user/account.html";
    }
    
    @GetMapping("/follow")
    public String doTracking(Model m, @RequestParam String username, Authentication auth) {
        User loggedUser = dao.getUserByUsername(auth.getName());
        User followingUser = dao.getUserByUsername(username);
        dao.addFollower(loggedUser.username, followingUser.username);
        m.addAttribute("user",followingUser);
        m.addAttribute("rewards",dao.getRewardsByUserId(followingUser.getUid()));
        m.addAttribute("isFollower", true);
        return "profile.html";
    }
    
    @GetMapping("/tracking")
    public String goTracking(Model m, Authentication auth) {
        User loggedUser = dao.getUserByUsername(auth.getName());
        ArrayList<Following> followList = dao.getListFollowing(loggedUser.getUid());
        m.addAttribute("following",followList);
        return "/user/tracking.html";
    }
    
    @GetMapping("/profile")
	public String goProfile(Model m, @RequestParam long id, Authentication auth) {
		User usr = dao.getUserById(id);
    	m.addAttribute("user", usr);
		if(null != auth) {
			ArrayList<String> roles = new ArrayList<String>();
			for (GrantedAuthority ga: auth.getAuthorities()) {
				roles.add(ga.getAuthority());
			}
			
			for(String r : roles) {
				if (r.equals("MODERATOR")) {
					m.addAttribute("mainRole", "MODERATOR");
				} else if (r.equals("ADMIN")){
					m.addAttribute("mainRole", "ADMIN");
				}
			}
			m.addAttribute("isFollower", dao.isFollowing(id, dao.getUserByUsername(auth.getName()).getUid()));
		} else {
			m.addAttribute("mainRole", "ADMIN");
			m.addAttribute("isFollower", false);
		}
		m.addAttribute("rewards", dao.getRewardsByUserId(id));
		return "profile.html";
	}
    
    @GetMapping("/disableUser")
    public String disableUser(Model m, @RequestParam long uid) {
    	dao.disableUser(uid);
    	return "home.html";
    }
    
    @GetMapping("/logout")
    public String logout() {
    	return "login.html";
    }
}
