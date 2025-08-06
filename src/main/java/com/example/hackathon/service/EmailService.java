package com.example.hackathon.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private Properties emailProperties = new Properties();

    public EmailService() {
        try (FileInputStream fis = new FileInputStream("src/main/resources/email.properties")) {
            emailProperties.load(fis);
        } catch (IOException e) {
            System.out.println("❌ Failed to load email configuration: " + e.getMessage());
        }
    }

    public void sendEmailWithAttachment(String toEmail, File attachmentFile) {
        final String fromEmail = emailProperties.getProperty("sender.email");
        final String password = emailProperties.getProperty("sender.password");

        Properties props = new Properties();
        props.put("mail.smtp.host", emailProperties.getProperty("mail.smtp.host"));
        props.put("mail.smtp.port", emailProperties.getProperty("mail.smtp.port"));
        props.put("mail.smtp.auth", emailProperties.getProperty("mail.smtp.auth"));
        props.put("mail.smtp.starttls.enable", emailProperties.getProperty("mail.smtp.starttls.enable"));

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("AppCleaner Log Report");

            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Please find attached the log report from AppCleaner.");

            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(attachmentFile);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            Transport.send(message);
            System.out.println("📧 Email sent successfully to: " + toEmail);

        } catch (Exception e) {
            System.out.println("❌ Failed to send email: " + e.getMessage());
        }
    }
}
