package com.gathix.tournament;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tournaments")
@Getter
@Setter
@NoArgsConstructor
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guild_id", nullable = false)
    private Long guildId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String game;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "max_teams", nullable = false)
    private int maxTeams;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Tournament(Long guildId, String name, String game,
                      int maxTeams, Long createdBy) {
        this.guildId = guildId;
        this.name = name;
        this.game = game;
        this.maxTeams = maxTeams;
        this.createdBy = createdBy;
        this.status = "REGISTRATION";
        this.createdAt = LocalDateTime.now();
    }
}