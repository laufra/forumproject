package ca.sheridancollege;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ca.sheridancollege.DAO.DAO;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	@Autowired
	private DAO dao;
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		ca.sheridancollege.beans.User user = dao.getUserByUsername(username);
		
		if (user == null) {
			System.out.println("User not found! " + username);
			throw new UsernameNotFoundException("User " + username + " was not found in the database");
		}
		System.out.println("Found User: " + user);
		
		ArrayList<String> roleNames = dao.getRoleNames(user.getUid());
		
		List<GrantedAuthority> grantList = new ArrayList<GrantedAuthority>();
			if (roleNames != null) {
			for (String role : roleNames) {
				System.out.println(role);
				GrantedAuthority authority = new SimpleGrantedAuthority(role);
				grantList.add(authority);
			}
		}
		
		UserDetails userDetails = (UserDetails) new User(user.getUsername(), user.getEncryptedPassword(), grantList);
		return userDetails;
	}

}
