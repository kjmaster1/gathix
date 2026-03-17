package com.gathix.lfg;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "lfg_posts")
@Getter
@Setter
@NoArgsConstructor
public class LfgPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guild_id", nullable = false)
    private Long guildId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String game;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "players_needed", nullable = false)
    private int playersNeeded;

    @Column(name = "players_joined", nullable = false)
    private int playersJoined;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public LfgPost(Long guildId, Long userId, String game,
                   String description, int playersNeeded) {
        this.guildId = guildId;
        this.userId = userId;
        this.game = game;
        this.description = description;
        this.playersNeeded = playersNeeded;
        this.playersJoined = 0;
        this.status = "OPEN";
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(2);
    }

    public boolean isFull() {
        return playersJoined >= playersNeeded;
    }

    public int getSpotsRemaining() {
        return playersNeeded - playersJoined;
    }
}