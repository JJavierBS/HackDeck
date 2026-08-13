package com.hackdeck.adapter.websocket;

import com.hackdeck.domain.model.ParticipantSession;
import org.springframework.web.socket.WebSocketSession;

public record ConnectedParticipant(WebSocketSession socket, ParticipantSession session) {
}
