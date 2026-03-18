package com.gathix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
public class GathixApplication {

	public static void main(String[] args) {
		SpringApplication.run(GathixApplication.class, args);
	}

	// Ping ourselves every 10 minutes to prevent Render free tier spin-down
	@Scheduled(fixedDelay = 600000)
	public void keepAlive() {
		try {
			String url = System.getenv().getOrDefault("RENDER_EXTERNAL_URL",
					"http://localhost:8082") + "/health";
			new RestTemplate().getForObject(url, String.class);
		} catch (Exception ignored) {
			// Ignore failures
		}
	}
}
