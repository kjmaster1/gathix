package com.gathix.tournament;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentMatchRepository
        extends JpaRepository<TournamentMatch, Long> {

    List<TournamentMatch> findByTournamentIdAndRoundOrderById(
            Long tournamentId, int round);
    List<TournamentMatch> findByTournamentIdOrderByRoundAscIdAsc(
            Long tournamentId);
}