package com.naatalvote.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.naatalvote")
public class NaatalVoteApiApplication {
  public static void main(String[] args) {
    SpringApplication.run(NaatalVoteApiApplication.class, args);
  }
}

