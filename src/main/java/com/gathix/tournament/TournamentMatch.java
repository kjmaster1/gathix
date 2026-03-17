package com.gathix.tournament;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tournament_matches")
@Getter
@Setter
@NoArgsConstructor
public class TournamentMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tournament_id", nullable = false)
    private Long tournamentId;

    @Column(nullable = false)
    private int round;

    @Column(name = "team_one_id")
    private Long teamOneId;

    @Column(name = "team_two_id")
    private Long teamTwoId;

    @Column(name = "winner_id")
    private Long winnerId;

    @Column(nullable = false, length = 20)
    private String status;

    public TournamentMatch(Long tournamentId, int round,
                           Long teamOneId, Long teamTwoId) {
        this.tournamentId = tournamentId;
        this.round = round;
        this.teamOneId = teamOneId;
        this.teamTwoId = teamTwoId;
        this.status = "PENDING";
    }
}