package com.worldatlas.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class WorldAtlasApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorldAtlasApplication.class, args);
    }
}
