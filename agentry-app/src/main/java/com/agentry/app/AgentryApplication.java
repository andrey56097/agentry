package com.agentry.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.agentry.core",
    "com.agentry.persistence",
    "com.agentry.api",
    "com.agentry.cigateway",
    "com.agentry.cli",
    "com.agentry.dashboard",
    "com.agentry.app"
})
public class AgentryApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentryApplication.class, args);
    }
}
