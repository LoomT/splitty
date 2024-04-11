package client.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


@Service
public class EmailServiceImpl implements EmailService{

    private String fromEmail;

    private JavaMailSender mailSender;

    public void sendEmail(Properties properties, String toEmail, String subject, String body){

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setUsername((String)properties.get("spring.mail.username"));
        mailSender.setPassword((String)properties.get("spring.mail.password"));
        mailSender.setHost((String)properties.get("spring.mail.host"));
        mailSender.setPort(Integer.parseInt((String)properties.get("spring.mail.port")));
        mailSender.setDefaultEncoding("UTF-8");
        mailSender.setProtocol("smtp");
        mailSender.setJavaMailProperties(properties);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom((String)properties.get("spring.mail.username"));
            helper.setTo(toEmail);
            helper.setCc((String)properties.get("spring.mail.username"));
            helper.setSubject(subject);
            helper.setText(body);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        mailSender.send(mimeMessage);
        System.out.println("email sent");
    }

    public static void main(String[] args) {
        //SpringApplication.run(EmailService.class, args);
        Properties p = new Properties();
        try {
            p.load(new FileInputStream("C:/Users/guney/OOPP/project/oopp-team-69/client/src/main/resources/application.properties"));
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }
        EmailService es = new EmailServiceImpl();
        es.sendEmail(p,
                "guneybayindir2005@gmail.com",
                "test",
                "testing123456789");
    }
}
