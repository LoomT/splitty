package client.utils;

import com.google.inject.Inject;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    private String fromEmail;
    private JavaMailSenderImpl mailSender;
    private final UserConfig userConfig;
    private boolean initializationStatus;

    /**
     * injects userConfig to the EmailService
     * @param userConfig configuration of the user
     */
    @Inject
    public EmailService(UserConfig userConfig){
        this.userConfig = userConfig;
        initializationStatus = true;
        initializeMailSender();
    }

    /**
     * Initializes EmailService
     */
    public void initializeMailSender(){
        try{
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
        catch (Exception e){
            initializationStatus = false;
        }
    }

    /**
     * Sends email with the specified attributes
     * @param toEmail email address to be sent to
     * @param subject subject of the email
     * @param body body of the email
     * @return true iff the email has been successfully sent
     */
    public boolean sendEmail(String toEmail, String subject, String body){
        if(!initializationStatus) return false;
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setCc(fromEmail);
            helper.setSubject(subject);
            helper.setText(body);
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.out.println("Email couldn't be sent");
            return false;
        }

        System.out.println("Email Sent");
        return true;
    }

    /**
     * Sends a default email to the users email to test
     * @return true iff the email has been successfully sent
     */
    public boolean sendTestEmail(){
        if(!initializationStatus) return false;
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(userConfig.getUsername());
            helper.setTo(userConfig.getUsername());
            helper.setSubject("Test Email");
            helper.setText("This is a test email to see if it has been configured correctly");
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.out.println("Test Email couldn't be sent");
            return false;
        }
        System.out.println("Test Email Sent");
        return true;
    }
}
