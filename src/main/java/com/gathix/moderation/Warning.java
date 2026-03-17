package com.gathix.moderation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "warnings")
@Getter
@Setter
@NoArgsConstructor
public class Warning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guild_id", nullable = false)
    private Long guildId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "moderator_id", nullable = false)
    private Long moderatorId;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Warning(Long guildId, Long userId, Long moderatorId, String reason) {
        this.guildId = guildId;
        this.userId = userId;
        this.moderatorId = moderatorId;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }
}