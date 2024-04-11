package client.utils;

import java.util.Properties;

public interface EmailService {

    void sendEmail(Properties properties, String toEmail, String subject, String body);
}
