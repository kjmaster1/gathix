package com.gathix.tournament;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentTeamRepository
        extends JpaRepository<TournamentTeam, Long> {

    List<TournamentTeam> findByTournamentId(Long tournamentId);
    Optional<TournamentTeam> findByTournamentIdAndCaptainId(
            Long tournamentId, Long captainId);
    long countByTournamentId(Long tournamentId);
}