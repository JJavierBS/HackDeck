package com.cyberrange.adapter.websocket;

import com.cyberrange.domain.model.ParticipantSession;
import org.springframework.web.socket.WebSocketSession;

/**
 * Una conexion abierta junto a la identidad ya verificada de quien esta al
 * otro lado. Sin esto no se puede difundir un estado distinto por rol.
 */
public record ConnectedParticipant(WebSocketSession socket, ParticipantSession session) {
}
