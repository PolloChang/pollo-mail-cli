package com.pollo.mail.service;

import com.pollo.mail.config.MailConfig;
import com.pollo.mail.util.CryptoUtil;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.Properties;

@Service
public class MailService {

    private final MailConfig mailConfig;

    public MailService(MailConfig mailConfig) {
        this.mailConfig = mailConfig;
    }

    private Session createSmtpSession() {
        MailConfig.SmtpConfig smtp = mailConfig.smtp();
        if (smtp == null || smtp.host() == null || smtp.host().isEmpty()) {
            throw new IllegalStateException("SMTP configuration is missing. Please run 'pollo-mail setup'.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", smtp.host());
        props.put("mail.smtp.port", String.valueOf(smtp.port()));
        
        // Basic auth and TLS defaults
        props.put("mail.smtp.auth", "true");
        if (smtp.port() == 465) {
            props.put("mail.smtp.ssl.enable", "true");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }

        String password = CryptoUtil.decrypt(smtp.password());

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtp.username(), password);
            }
        });
    }

    public void sendEmail(String[] to, String subject, String body, boolean isHtml, File[] attachments) throws Exception {
        Session session = createSmtpSession();

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mailConfig.smtp().username()));
        
        InternetAddress[] toAddresses = new InternetAddress[to.length];
        for (int i = 0; i < to.length; i++) {
            toAddresses[i] = new InternetAddress(to[i].trim());
        }
        message.setRecipients(Message.RecipientType.TO, toAddresses);
        
        message.setSubject(subject != null ? subject : "");
        message.setSentDate(new Date());

        if (attachments == null || attachments.length == 0) {
            if (isHtml) {
                message.setContent(body != null ? body : "", "text/html; charset=utf-8");
            } else {
                message.setText(body != null ? body : "");
            }
        } else {
            Multipart multipart = new MimeMultipart();

            MimeBodyPart textPart = new MimeBodyPart();
            if (isHtml) {
                textPart.setContent(body != null ? body : "", "text/html; charset=utf-8");
            } else {
                textPart.setText(body != null ? body : "");
            }
            multipart.addBodyPart(textPart);

            for (File file : attachments) {
                if (!file.exists() || !file.isFile()) {
                    throw new IllegalArgumentException("Attachment file not found: " + file.getAbsolutePath());
                }
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(file);
                multipart.addBodyPart(attachmentPart);
            }

            message.setContent(multipart);
        }

        Transport.send(message);
    }
}
