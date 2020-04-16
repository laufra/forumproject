package ca.sheridancollege;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	protected LoggingAccessDeniedHandler accessDeniedHandler;
	
	@Autowired
	public BCryptPasswordEncoder passwordEncoder() {
		BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		return bCryptPasswordEncoder;
	}
	
	@Autowired
	UserDetailsServiceImpl userDetailsService;
	
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
		.antMatchers("/","/forum", "/thread", "/register", "/log")
			.permitAll()
		
		.antMatchers("/reply", "/newThread", "/account", "/following", "/modify", "/profile", "/tracking")
			.hasAnyAuthority("USER", "MODERATOR", "ADMIN")
		
		.antMatchers("/disableUsers", "/deleteThread", "/deletePost")
			.hasAnyAuthority("MODERATOR", "ADMIN")
		
		.antMatchers("/**")
			.permitAll()
		
		.anyRequest()
			.authenticated()
		
		.and()
			.formLogin()
				.loginPage("/login")
				.permitAll()
		
		.and()
			.logout()
				.invalidateHttpSession(true)
				.clearAuthentication(true)
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
				.logoutSuccessUrl("/login?logout")
				.permitAll()
		
		.and()
			.exceptionHandling()
				.accessDeniedHandler(accessDeniedHandler);
	}
}
