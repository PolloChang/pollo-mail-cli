package com.pollo.mail.command;

import com.pollo.mail.service.FetchService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(
        name = "fetch",
        description = "Fetch emails from IMAP and save them as Markdown files with attachments."
)
public class FetchCommand implements Runnable {

    private final FetchService fetchService;

    public FetchCommand(FetchService fetchService) {
        this.fetchService = fetchService;
    }

    @Option(names = {"-n", "--count"}, description = "Number of latest emails to fetch (default: 10)", defaultValue = "10")
    private int count;

    @Option(names = {"-o", "--output"}, description = "Output directory for Markdown files", defaultValue = "~/.pollo-mail/inbox")
    private String outputDir;

    @Option(names = {"--unread-only"}, description = "Fetch only unread emails")
    private boolean unreadOnly;

    @Override
    public void run() {
        try {
            System.out.println("Starting email fetch operation...");
            long startTime = System.currentTimeMillis();

            fetchService.fetchEmails(count, outputDir, unreadOnly);

            long endTime = System.currentTimeMillis();
            System.out.println("\nFetch operation completed in " + (endTime - startTime) + "ms.");
        } catch (Exception e) {
            System.err.println("\nFailed to fetch emails: " + e.getMessage());
        }
    }
}
