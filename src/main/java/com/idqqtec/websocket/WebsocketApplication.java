package com.idqqtec.websocket;

import org.java_websocket.WebSocketImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebsocketApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebsocketApplication.class, args);
		WebSocketImpl.DEBUG = false;
		int port = 8082; // 端口
		WsServer s = new WsServer(port);
		s.start();
	}
}
