package com.cyberrange.adapter.websocket;

import com.cyberrange.domain.model.ParticipantSession;
import org.springframework.web.socket.WebSocketSession;

public record ConnectedParticipant(WebSocketSession socket, ParticipantSession session) {
}
