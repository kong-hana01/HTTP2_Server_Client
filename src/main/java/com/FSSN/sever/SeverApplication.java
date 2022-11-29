package com.FSSN.sever;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

@SpringBootApplication
@RestController
public class SeverApplication {

	public static void main(String[] args) {
		// Create a server on port 8000
		SpringApplication.run(SeverApplication.class, args);
	}

	// 멀티 커넥터 설정
	@Bean
	public ServletWebServerFactory servletContainer() {
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
		tomcat.addAdditionalTomcatConnectors(createStandardConnector());
		return tomcat;
	}

	// http 1.1 포트 번호 연결
	private Connector createStandardConnector() {
		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
		connector.setPort(8080);
		return connector;
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
			System.out.println("Serving on https://0.0.0.0:8000/");
		}
	}
}
