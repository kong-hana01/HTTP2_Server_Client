package com.FSSN.server;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

@SpringBootApplication
@RestController
@Data
public class server {

	@Value("${server.port}")
	private String portNumber;

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
			String startMessage = String.format("Serving on https://localhost: %s/", portNumber);
			System.out.println(startMessage);
		}
	}

}
