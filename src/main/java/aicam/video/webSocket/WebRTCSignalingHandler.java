package aicam.video.webSocket;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

public class WebRTCSignalingHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new HashMap<>();  // 연결된 세션 저장

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        System.out.println("New connection: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 메시지를 JSON으로 변환
        Map<String, String> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String type = payload.get("type");

        // SDP 또는 ICE Candidate를 모든 클라이언트에게 전달
        for (WebSocketSession s : sessions.values()) {
            if (!s.getId().equals(session.getId())) {
                s.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        System.out.println("Connection closed: " + session.getId());
    }
}
