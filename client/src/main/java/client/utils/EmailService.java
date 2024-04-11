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
    private boolean valid;

    @Inject
    public EmailService(UserConfig userConfig){
        this.userConfig = userConfig;
        initializeMailSender();
        valid = checkInitialization();
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

    public boolean checkInitialization(){
        if(fromEmail == null || fromEmail.isEmpty()) return false;
        if(mailSender == null) return false;
        return mailSender.getHost() != null && !mailSender.getHost().isEmpty();
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
        System.out.println("Email Sent");
    }

    public boolean sendTestEmail(){

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(userConfig.getUsername());
            helper.setTo(userConfig.getUsername());
            helper.setSubject("Test Email");
            helper.setText("This is a test email to see if it has been configured correctly");
        } catch (MessagingException e) {
            System.out.println("Test Email couldn't be sent");
            return false;
        }
        mailSender.send(mimeMessage);
        System.out.println("Test Email Sent");
        return true;
    }

    public boolean isValid() {
        return valid;
    }

    public void setFromEmail(){
        valid = true;
    }
}
