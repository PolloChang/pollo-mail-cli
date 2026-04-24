package com.pollo.mail.command;

import com.pollo.mail.service.MailService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;

@Component
@Command(
        name = "send",
        description = "Send an email using configured SMTP server."
)
public class SendCommand implements Runnable {

    private final MailService mailService;

    public SendCommand(MailService mailService) {
        this.mailService = mailService;
    }

    @Option(names = {"-t", "--to"}, required = true, split = ",", description = "Recipient email address(es), comma-separated")
    private String[] to;

    @Option(names = {"-s", "--subject"}, description = "Email subject")
    private String subject;

    @Option(names = {"-b", "--body"}, description = "Email body text")
    private String body;

    @Option(names = {"--html"}, description = "Send the body as HTML format")
    private boolean isHtml;

    @Option(names = {"-a", "--attach"}, description = "File(s) to attach")
    private File[] attachments;

    @Override
    public void run() {
        try {
            System.out.println("Sending email...");
            long startTime = System.currentTimeMillis();

            mailService.sendEmail(to, subject, body, isHtml, attachments);

            long endTime = System.currentTimeMillis();
            System.out.println("Email successfully sent to " + String.join(", ", to) + " in " + (endTime - startTime) + "ms.");
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}
