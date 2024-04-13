package client.utils;

import com.google.inject.Inject;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
public class EmailService {

    private String fromEmail;
    private JavaMailSenderImpl mailSender;
    private final UserConfig userConfig;
    private boolean initializationStatus;
    private final LanguageConf languageConf;

    /**
     * injects userConfig to the EmailService
     *
     * @param userConfig   configuration of the user
     * @param languageConf language configuration
     */
    @Inject
    public EmailService(UserConfig userConfig, LanguageConf languageConf) {
        this.userConfig = userConfig;
        initializationStatus = true;
        initializeMailSender();
        this.languageConf = languageConf;
    }

    /**
     * Initializes EmailService
     */
    public void initializeMailSender() {
        try {
            this.mailSender = new JavaMailSenderImpl();
            this.fromEmail = userConfig.getUsername();
            mailSender.setUsername(userConfig.getUsername());
            mailSender.setPassword(userConfig.getMailPassword());
            mailSender.setHost(userConfig.getHost());
            mailSender.setPort(userConfig.getPort());
            mailSender.setDefaultEncoding("UTF-8");
            mailSender.setProtocol("smtp");
            mailSender.setJavaMailProperties(userConfig.getMailProperties());
        } catch (Exception e) {
            initializationStatus = false;
        }
    }

    /**
     * Sends email with the specified attributes
     *
     * @param toEmail email address to be sent to
     * @param subject subject of the email
     * @param body    body of the email
     * @return true iff the email has been successfully sent
     */
    public boolean sendEmail(String toEmail, String subject, String body) {
        if (!initializationStatus) return false;
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
     *
     * @return true iff the email has been successfully sent
     */
    public boolean sendTestEmail() {
        if (!initializationStatus) return false;
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(userConfig.getUsername());
            helper.setTo(userConfig.getUsername());
            helper.setSubject(languageConf.get("EmailService.testEmailSubject"));
            helper.setText(languageConf.get("EmailService.testEmailBody"));
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.out.println("Test Email couldn't be sent");
            return false;
        }
        System.out.println("Test Email Sent");
        return true;
    }

    /**
     * Sets the username and the password for the mailSender
     *
     * @param username username
     * @param password password
     * @throws IOException if IO exception occurs
     */
    public void setConfiguration(String username, String password) throws IOException {
        this.mailSender.setUsername(username);
        this.mailSender.setPassword(password.replaceAll(" ", ""));
        userConfig.setUsername(username);
        userConfig.setMailPassword(password);
    }

    /**
     * checks the initialization of the emailService
     *
     * @return true iff username and password are initialized
     */
    public boolean isNotInitialized() {
        return mailSender.getUsername() == null
                || mailSender.getUsername().isEmpty()
                || mailSender.getPassword() == null
                || mailSender.getPassword().isEmpty();
    }
}
