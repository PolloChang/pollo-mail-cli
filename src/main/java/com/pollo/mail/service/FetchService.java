package com.pollo.mail.service;

import com.pollo.mail.config.MailConfig;
import com.pollo.mail.util.CryptoUtil;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.FlagTerm;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Service
public class FetchService {

    private final MailConfig mailConfig;

    public FetchService(MailConfig mailConfig) {
        this.mailConfig = mailConfig;
    }

    public void fetchEmails(int count, String outputDir, boolean unreadOnly) throws Exception {
        MailConfig.ImapConfig imap = mailConfig.imap();
        if (imap == null || imap.host() == null || imap.host().isEmpty()) {
            throw new IllegalStateException("IMAP configuration is missing. Please run 'pollo-mail setup'.");
        }

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", imap.host());
        props.put("mail.imaps.port", String.valueOf(imap.port()));
        
        String password = CryptoUtil.decrypt(imap.password());

        Session session = Session.getInstance(props, null);
        Store store = session.getStore("imaps");
        System.out.println("Connecting to IMAP server...");
        store.connect(imap.host(), imap.username(), password);

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        Message[] messages;
        if (unreadOnly) {
            messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
        } else {
            messages = inbox.getMessages();
        }

        int total = messages.length;
        System.out.println("Found " + total + " messages. Fetching top " + Math.min(count, total) + "...");

        int start = Math.max(0, total - count);
        
        Path outPath = Paths.get(outputDir.replaceFirst("^~", System.getProperty("user.home")));
        Files.createDirectories(outPath);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

        // Iterate backwards to get latest first
        for (int i = total - 1; i >= start; i--) {
            Message msg = messages[i];
            
            String subject = msg.getSubject() != null ? msg.getSubject().replace("\n", "").replace("\r", "") : "No Subject";
            Date date = msg.getReceivedDate() != null ? msg.getReceivedDate() : new Date();
            String from = msg.getFrom() != null && msg.getFrom().length > 0 ? msg.getFrom()[0].toString() : "Unknown";
            String toStr = msg.getRecipients(Message.RecipientType.TO) != null && msg.getRecipients(Message.RecipientType.TO).length > 0 
                    ? msg.getRecipients(Message.RecipientType.TO)[0].toString() : "Unknown";

            System.out.println("Fetching: " + subject);

            // Clean subject for filename
            String safeSubject = subject.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
            if (safeSubject.isEmpty()) safeSubject = "Unknown";
            String folderName = sdf.format(date) + "_" + safeSubject;
            if (folderName.length() > 50) folderName = folderName.substring(0, 50);

            Path msgFolder = outPath.resolve(folderName);
            Files.createDirectories(msgFolder);

            StringBuilder contentBuilder = new StringBuilder();
            List<String> attachmentLinks = new ArrayList<>();

            processPart(msg, contentBuilder, msgFolder, attachmentLinks);

            // Create Markdown
            String mdContent = generateMarkdown(from, toStr, date, subject, contentBuilder.toString(), attachmentLinks);
            Path mdFile = msgFolder.resolve("email.md");
            Files.writeString(mdFile, mdContent);
        }

        inbox.close(false);
        store.close();
        System.out.println("Fetch complete. Emails saved to " + outPath.toAbsolutePath());
    }

    private void processPart(Part part, StringBuilder contentBuilder, Path msgFolder, List<String> attachmentLinks) throws Exception {
        if (part.isMimeType("text/plain")) {
            contentBuilder.append(part.getContent().toString()).append("\n");
        } else if (part.isMimeType("text/html")) {
            // Only use HTML if we haven't seen plain text (or just append)
            // A simple strip HTML logic
            String html = part.getContent().toString();
            String text = html.replaceAll("(?s)<style[^>]*>.*?</style>", "")
                              .replaceAll("(?s)<script[^>]*>.*?</script>", "")
                              .replaceAll("<br\\s*/?>", "\n")
                              .replaceAll("<p>", "\n\n")
                              .replaceAll("<[^>]+>", " ")
                              .replaceAll("&nbsp;", " ");
            
            // To prevent HTML duplicating plain text in multipart/alternative, we only append if contentBuilder is empty
            if (contentBuilder.length() == 0) {
                 contentBuilder.append(text).append("\n");
            }
        } else if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            boolean isAlternative = part.isMimeType("multipart/alternative");
            
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bodyPart = mp.getBodyPart(i);
                
                // For multipart/alternative, prefer plain text (which is usually the first part, but we can just process all, 
                // and the HTML logic above checks if content is already there).
                // Actually, text/html is usually the last part in multipart/alternative.
                if (isAlternative && bodyPart.isMimeType("text/html") && contentBuilder.length() > 0) {
                    continue; // Skip HTML if we already have plain text
                }
                processPart(bodyPart, contentBuilder, msgFolder, attachmentLinks);
            }
        } else if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) || part.getFileName() != null) {
            String fileName = part.getFileName();
            if (fileName != null && !fileName.isEmpty()) {
                Path attachmentPath = msgFolder.resolve(fileName);
                try (InputStream is = part.getInputStream();
                     FileOutputStream fos = new FileOutputStream(attachmentPath.toFile())) {
                    byte[] buf = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buf)) != -1) {
                        fos.write(buf, 0, bytesRead);
                    }
                }
                attachmentLinks.add("./" + fileName);
            }
        }
    }

    private String generateMarkdown(String from, String to, Date date, String subject, String body, List<String> attachmentLinks) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat yamlDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        sb.append("---\n");
        sb.append("date: ").append(yamlDate.format(date)).append("\n");
        sb.append("from: \"").append(from.replace("\"", "\\\"")).append("\"\n");
        sb.append("to: \"").append(to.replace("\"", "\\\"")).append("\"\n");
        sb.append("subject: \"").append(subject.replace("\"", "\\\"")).append("\"\n");
        sb.append("---\n\n");
        
        sb.append(body.trim()).append("\n\n");
        
        if (!attachmentLinks.isEmpty()) {
            sb.append("---\n**Attachments:**\n");
            for (String link : attachmentLinks) {
                String fileName = link.replace("./", "");
                sb.append("- [").append(fileName).append("](").append(link).append(")\n");
            }
        }
        
        return sb.toString();
    }
}
