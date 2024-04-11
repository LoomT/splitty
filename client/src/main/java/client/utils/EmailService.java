package client.utils;

import com.google.inject.Inject;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    private String fromEmail;
    private JavaMailSenderImpl mailSender;
    private final UserConfig userConfig;

    @Inject
    public EmailService(UserConfig userConfig){
        this.userConfig = userConfig;
        initializeMailSender();

    }

    public void initializeMailSender(){
        this.mailSender = new JavaMailSenderImpl();
        this.fromEmail = userConfig.getUsername();
        mailSender.setUsername(userConfig.getUsername());
        mailSender.setPassword(userConfig.getMailPassword());
        mailSender.setHost(userConfig.getHost());
        mailSender.setPort(userConfig.getPort());
        mailSender.setDefaultEncoding("UTF-8");
        mailSender.setProtocol("smtp");
        mailSender.setJavaMailProperties(userConfig.getMailProperties());
    }

    public void sendEmail(String toEmail, String subject, String body){

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setCc(fromEmail);
            helper.setSubject(subject);
            helper.setText(body);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        mailSender.send(mimeMessage);
        System.out.println("email sent");
    }

    public void sendTestEmail(){

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(userConfig.getUsername());
            helper.setTo(userConfig.getUsername());
            helper.setSubject("Test Email");
            helper.setText("This is a test email to see if it has been configured correctly");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        mailSender.send(mimeMessage);
        System.out.println("email sent");
    }
}
