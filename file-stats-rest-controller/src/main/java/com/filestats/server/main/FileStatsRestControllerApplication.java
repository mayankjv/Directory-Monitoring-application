package com.filestats.server.main;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
@EnableAutoConfiguration
@ComponentScan({ "controller", "model" })
public class FileStatsRestControllerApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(FileStatsRestControllerApplication.class);
		builder.headless(false).run(args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(FileStatsRestControllerApplication.class);
	}
}
