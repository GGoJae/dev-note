package com.gj.dev_note;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class DevNoteApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevNoteApplication.class, args);
	}

}
