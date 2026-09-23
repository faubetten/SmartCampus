// SmartCampus Application

package com.smartcampus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

@SpringBootApplication
@RestController
public class SmartCampusApplication {

    @Value("${INSTANCE_ID:Unknown-Node}")
    private String instanceId;

    @GetMapping("/health")
    public String healthCheck() {
        return "SmartCampus API is online, handled by: " + instanceId;
    }

    public static void main(String[] args) {
        SpringApplication.run(SmartCampusApplication.class, args);
    }
}