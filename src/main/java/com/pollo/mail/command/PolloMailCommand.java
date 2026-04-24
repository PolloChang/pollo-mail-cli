package com.pollo.mail.command;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(
        name = "pollo-mail",
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "A fast, cross-platform CLI email tool.",
        subcommands = {
                SetupCommand.class,
                SendCommand.class,
                FetchCommand.class
        }
)
public class PolloMailCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("Welcome to Pollo-Mail CLI! Use --help to see available commands.");
    }
}
