package controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class SocketController {

	@MessageMapping("/message")
	@SendTo("/socketresponse/status")
	public String progress(String progress) throws Exception {
		return progress;
	}

}
