package com.pollo.mail.command;

import com.pollo.mail.util.CryptoUtil;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

import java.io.Console;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Command(
        name = "setup",
        description = "Interactive wizard to setup your mail server configuration."
)
public class SetupCommand implements Runnable {

    @Override
    public void run() {
        Console console = System.console();
        java.util.Scanner scanner = null;
        if (console == null) {
            System.out.println("Warning: No interactive console available (likely running via Gradle). Passwords will be visible.");
            scanner = new java.util.Scanner(System.in);
        }

        System.out.println("=== Pollo-Mail Setup Wizard ===");
        
        System.out.println("\n[SMTP Configuration]");
        String smtpHost = prompt(console, scanner, "SMTP Host (e.g. smtp.gmail.com): ");
        String smtpPortStr = prompt(console, scanner, "SMTP Port (e.g. 465, 587): ");
        String smtpUser = prompt(console, scanner, "SMTP Username: ");
        String smtpPass = promptPassword(console, scanner, "SMTP Password: ");

        System.out.println("\n[IMAP Configuration]");
        String imapHost = prompt(console, scanner, "IMAP Host (e.g. imap.gmail.com): ");
        String imapPortStr = prompt(console, scanner, "IMAP Port (e.g. 993): ");
        String imapUser = prompt(console, scanner, "IMAP Username: ");
        String imapPass = promptPassword(console, scanner, "IMAP Password: ");

        // Encrypt passwords
        String encSmtpPass = CryptoUtil.encrypt(smtpPass);
        String encImapPass = CryptoUtil.encrypt(imapPass);

        // Generate YAML content
        String yamlContent = String.format("""
                mail:
                  smtp:
                    host: "%s"
                    port: %s
                    username: "%s"
                    password: "%s"
                  imap:
                    host: "%s"
                    port: %s
                    username: "%s"
                    password: "%s"
                """,
                smtpHost, smtpPortStr, smtpUser, encSmtpPass,
                imapHost, imapPortStr, imapUser, encImapPass
        );

        // Save to ~/.config/pollo-mail/config.yaml
        try {
            String userHome = System.getProperty("user.home");
            Path configDir = Paths.get(userHome, ".config", "pollo-mail");
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            Path configFile = configDir.resolve("config.yaml");
            Files.writeString(configFile, yamlContent);
            System.out.println("\nConfiguration successfully saved to " + configFile);
            System.out.println("Passwords have been encrypted securely for this machine.");
        } catch (Exception e) {
            System.err.println("Failed to save configuration: " + e.getMessage());
        }
    }

    private String prompt(Console console, java.util.Scanner scanner, String message) {
        if (console != null) {
            return console.readLine(message);
        } else {
            System.out.print(message);
            return scanner.nextLine();
        }
    }

    private String promptPassword(Console console, java.util.Scanner scanner, String message) {
        if (console != null) {
            return new String(console.readPassword(message));
        } else {
            System.out.print(message);
            return scanner.nextLine();
        }
    }
}
