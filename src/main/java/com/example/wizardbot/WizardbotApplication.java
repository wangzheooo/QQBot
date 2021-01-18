package com.example.wizardbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WizardbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(WizardbotApplication.class, args);
    }

}
