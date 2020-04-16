package ca.sheridancollege.DAO;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class DatabaseConfig {
	
	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
	
	/*
	 Class.forName("com.mysql.cj.jdbc.Driver");
     Connection con = null;
     con = DriverManager.getConnection("jdbc:mysql://localhost/contacts", "root", "root");
	 */
	
	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost/forum");
		dataSource.setUsername("root");
		dataSource.setPassword("root");
		return dataSource;
	}
}
