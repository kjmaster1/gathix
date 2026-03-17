package com.gathix.moderation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mod_actions")
@Getter
@Setter
@NoArgsConstructor
public class ModAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guild_id", nullable = false)
    private Long guildId;

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "moderator_id", nullable = false)
    private Long moderatorId;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ModAction(Long guildId, String actionType, Long userId,
                     Long moderatorId, String reason) {
        this.guildId = guildId;
        this.actionType = actionType;
        this.userId = userId;
        this.moderatorId = moderatorId;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }
}