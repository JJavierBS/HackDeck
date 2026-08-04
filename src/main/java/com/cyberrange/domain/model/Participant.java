package com.cyberrange.domain.model;

import java.util.Objects;

/**
 * Quien participa en una partida. El instructor arbitra y no tiene equipo;
 * un jugador representa a su equipo entero (un dispositivo por equipo).
 */
public record Participant(
        PlayerId id,
        ParticipantKind kind,
        TeamId team,
        String displayName) {

    public static final int MAX_DISPLAY_NAME_LENGTH = 24;

    public Participant {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(kind, "kind");
        if (kind == ParticipantKind.PLAYER && team == null) {
            throw new IllegalArgumentException("Un jugador siempre pertenece a un equipo");
        }
        if (kind == ParticipantKind.INSTRUCTOR && team != null) {
            throw new IllegalArgumentException("El instructor no juega ningun equipo");
        }
    }

    public static Participant instructor() {
        return new Participant(PlayerId.newId(), ParticipantKind.INSTRUCTOR, null, "instructor");
    }

    public static Participant player(TeamId team, String displayName) {
        return new Participant(PlayerId.newId(), ParticipantKind.PLAYER, team, normalizeName(displayName));
    }

    public boolean isInstructor() {
        return kind == ParticipantKind.INSTRUCTOR;
    }

    private static String normalizeName(String displayName) {
        String normalized = displayName == null ? "" : displayName.strip();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("El nombre del equipo no puede estar vacio");
        }
        return normalized.length() > MAX_DISPLAY_NAME_LENGTH
                ? normalized.substring(0, MAX_DISPLAY_NAME_LENGTH)
                : normalized;
    }
}
