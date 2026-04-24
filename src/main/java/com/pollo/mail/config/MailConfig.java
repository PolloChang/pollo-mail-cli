package com.pollo.mail.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mail")
public record MailConfig(
        SmtpConfig smtp,
        ImapConfig imap
) {
    public record SmtpConfig(
            String host,
            Integer port,
            String username,
            String password
    ) {}

    public record ImapConfig(
            String host,
            Integer port,
            String username,
            String password
    ) {}
}
