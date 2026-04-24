package com.pollo.mail;

import com.pollo.mail.command.PolloMailCommand;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

import java.util.Map;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PolloMailApplication implements CommandLineRunner {

    private final IFactory factory;
    private final PolloMailCommand rootCommand;

    public PolloMailApplication(IFactory factory, PolloMailCommand rootCommand) {
        this.factory = factory;
        this.rootCommand = rootCommand;
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PolloMailApplication.class);
        
        // Define the external config directory per PRD (US-1.1)
        String userHome = System.getProperty("user.home");
        app.setDefaultProperties(Map.of(
                "spring.config.additional-location", "optional:file:" + userHome + "/.config/pollo-mail/",
                "spring.config.name", "application,config" // Support config.yaml
        ));
        
        app.run(args);
    }

    @Override
    public void run(String... args) {
        int exitCode = new CommandLine(rootCommand, factory).execute(args);
        // Avoid calling System.exit() here to let Spring Boot shutdown gracefully,
        // unless you specifically need the JVM exit code to map to the CLI exit code.
        // For CLI tools, it's often good to exit with the correct code:
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
