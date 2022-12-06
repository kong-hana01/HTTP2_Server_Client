package com.FSSN.server;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.time.Duration;

@SpringBootApplication
@RestController
@Data
public class server {

	@Value("${server.port}")
	private String portNumber;
	@Value("${server.ssl.key-store}")
	private String serverP12;
	private static final int readTimeOutSeconds = 5;
	private static final int writeTimeOutSeconds = 10;

	public static void main(String[] args) {
		// Create a server on port 8000
		SpringApplication.run(server.class, args);
	}

	@GetMapping("/")
	public ResponseEntity<String> echo(HttpServletRequest request) {
		// Get request information
		String remoteProto = request.getProtocol();
		String remoteAddr = request.getRemoteAddr();
		int remotePort = request.getRemotePort();

		// Log the request protocol
		String receivedMessage = String.format("Got connection: %s from %s %d", remoteProto, remoteAddr, remotePort);
		System.out.println(receivedMessage);

		// Send a message back to the client
		String sendMessage = "Hello";
		return ResponseEntity.ok().body(sendMessage);
	}

	@Component
	public class PostConstructView {

		// 서버 구동 전 호출할 함수
		@PostConstruct
		public void printStartMessage() {
			String startMessage = String.format("Serving on https://localhost:%s/", portNumber);
			System.out.println(startMessage);
		}

		// 자바에선 crt와 key를 합친 p12를 사용하는 경우가 일반적\
		// X509KeyPair 체크는 application.property에서 체크하여 진행
		@PostConstruct
		public void checkP12() {
			File f = new File(serverP12);
			if (!f.exists()) {
				System.out.println("파일이 없거나 파일을 읽을 수 없습니다.");
			}
		}
	}

	@Configuration
	class RestTemplateConfig {

		@Bean
		public RestTemplate restTemplate() {
			return new RestTemplateBuilder()
					.setConnectTimeout(Duration.ofSeconds(writeTimeOutSeconds))
					.setReadTimeout(Duration.ofSeconds(readTimeOutSeconds))
					.build();
		}
	}
}
