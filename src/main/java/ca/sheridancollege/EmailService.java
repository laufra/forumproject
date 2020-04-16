package ca.sheridancollege;


import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.TemplateEngine;
import ca.sheridancollege.beans.User;

@Component
public class EmailService {
	private JavaMailSender jms;
	
	@Autowired
	private TemplateEngine templateEngine;
	
	@Autowired
	public EmailService(JavaMailSender javaMailSender) {
		this.jms = javaMailSender;
	}
	
	public void sendConfirmation(User user) throws MailException {
		final User usr = user;
		final Context ctx = new Context();
		ctx.setVariable("name", usr.getUsername());
		ctx.setVariable("id", usr.getUid());
		final String htmlContent = templateEngine.process("emailTemplate.html", ctx);
		MimeMessagePreparator m = new MimeMessagePreparator() {
			public void prepare(MimeMessage m) throws Exception {
				m.setContent(htmlContent, "text/html");
				m.setRecipient(Message.RecipientType.TO, new InternetAddress(usr.getEmail()));
				m.setFrom("noreply.sheridanstutalk@gmail.com");
				m.setSubject("Sheridan Stutalk -- Please Confirm your Email");
				
			}
		};
		jms.send(m);
	}
}
