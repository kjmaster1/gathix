package com.gathix.tournament;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tournament_teams")
@Getter
@Setter
@NoArgsConstructor
public class TournamentTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tournament_id", nullable = false)
    private Long tournamentId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "captain_id", nullable = false)
    private Long captainId;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    public TournamentTeam(Long tournamentId, String name, Long captainId) {
        this.tournamentId = tournamentId;
        this.name = name;
        this.captainId = captainId;
        this.registeredAt = LocalDateTime.now();
    }
}