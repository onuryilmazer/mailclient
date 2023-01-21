package mailclient.backend;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SmtpJavaMailImplementation implements SmtpClient {
    private String username;
    private String password;
    private boolean encryptedConnection;
    private boolean loggedIn = false;
    private Properties mailProperties;
    Session emailSession;

    public SmtpJavaMailImplementation(String serverAddress, int serverPort, boolean encryptedConnection, String username, String password) {
        mailProperties = new Properties();
        mailProperties.put("mail.smtp.auth", "true");
        mailProperties.put("mail.smtp.starttls.enable", encryptedConnection ? "true" : "false");
        mailProperties.put("mail.smtp.host", serverAddress);
        mailProperties.put("mail.smtp.port", serverPort);
        this.username = username;
        this.password = password;
        this.encryptedConnection = encryptedConnection;
        establishConnection();

    }

    private void establishConnection() {
        emailSession = Session.getInstance(mailProperties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        loggedIn = true;
    }

    @Override
    public void sendMail(String sender, String[] recipients, String subject, String mailBody) {
        try {
            Message message = new MimeMessage(emailSession);

            message.setFrom(new InternetAddress(sender));

            for (String r : recipients) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(r));
            }

            message.setSubject(subject);

            message.setText(mailBody);

            Transport.send(message);
            System.out.println("Mail was sent succesfully.");

        } catch (MessagingException e) {
            System.out.println("Error while sending the mail: " + e.toString());
        }
    }

    @Override
    public void endConnection() {
        try {
            emailSession.getTransport("smtp").close();
            loggedIn = false;
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean connectionIsReadyToUse() {
        return isLoggedIn();
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    @Override
    public void reconnect(int retryCount) {
        for (int i = 0; i < retryCount; i++) {
            System.out.println("Trying to reconnect. Attempt nr. " + i);
            establishConnection();
            if (connectionIsReadyToUse()) {
                break;
            }
        }
    }
}