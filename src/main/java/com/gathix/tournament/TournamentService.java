package com.gathix.tournament;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TournamentService {

    private static final Logger log =
            LoggerFactory.getLogger(TournamentService.class);

    private final TournamentRepository tournamentRepository;
    private final TournamentTeamRepository teamRepository;
    private final TournamentMatchRepository matchRepository;

    public TournamentService(TournamentRepository tournamentRepository,
                             TournamentTeamRepository teamRepository,
                             TournamentMatchRepository matchRepository) {
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
    }

    public Tournament createTournament(Long guildId, String name,
                                       String game, int maxTeams,
                                       Long createdBy) {
        if (maxTeams < 2 || maxTeams > 32) {
            throw new IllegalArgumentException(
                    "Max teams must be between 2 and 32");
        }
        Tournament tournament = new Tournament(guildId, name,
                game, maxTeams, createdBy);
        return tournamentRepository.save(tournament);
    }

    public TournamentTeam registerTeam(Long tournamentId, String teamName,
                                       Long captainId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tournament not found"));

        if (!tournament.getStatus().equals("REGISTRATION")) {
            throw new IllegalStateException(
                    "This tournament is not accepting registrations");
        }

        long teamCount = teamRepository.countByTournamentId(tournamentId);
        if (teamCount >= tournament.getMaxTeams()) {
            throw new IllegalStateException("This tournament is full");
        }

        if (teamRepository.findByTournamentIdAndCaptainId(
                tournamentId, captainId).isPresent()) {
            throw new IllegalStateException(
                    "You are already registered in this tournament");
        }

        TournamentTeam team = new TournamentTeam(
                tournamentId, teamName, captainId);
        return teamRepository.save(team);
    }

    @Transactional
    public List<TournamentMatch> startTournament(Long tournamentId,
                                                 Long userId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tournament not found"));

        if (!tournament.getCreatedBy().equals(userId)) {
            throw new IllegalStateException(
                    "Only the tournament creator can start it");
        }

        if (!tournament.getStatus().equals("REGISTRATION")) {
            throw new IllegalStateException(
                    "Tournament has already started");
        }

        List<TournamentTeam> teams =
                teamRepository.findByTournamentId(tournamentId);

        if (teams.size() < 2) {
            throw new IllegalStateException(
                    "Need at least 2 teams to start");
        }

        // Shuffle teams for random seeding
        Collections.shuffle(teams);

        // Generate round 1 bracket
        List<TournamentMatch> matches = generateRound(
                tournamentId, 1, teams);

        tournament.setStatus("IN_PROGRESS");
        tournamentRepository.save(tournament);

        return matches;
    }

    private List<TournamentMatch> generateRound(Long tournamentId,
                                                int round,
                                                List<TournamentTeam> teams) {
        List<TournamentMatch> matches = new ArrayList<>();

        for (int i = 0; i < teams.size() - 1; i += 2) {
            TournamentMatch match = new TournamentMatch(
                    tournamentId,
                    round,
                    teams.get(i).getId(),
                    teams.get(i + 1).getId()
            );
            matches.add(matchRepository.save(match));
        }

        // Handle odd number of teams — last team gets a bye
        if (teams.size() % 2 != 0) {
            TournamentTeam byeTeam = teams.get(teams.size() - 1);
            TournamentMatch byeMatch = new TournamentMatch(
                    tournamentId, round,
                    byeTeam.getId(), null
            );
            byeMatch.setStatus("BYE");
            byeMatch.setWinnerId(byeTeam.getId());
            matches.add(matchRepository.save(byeMatch));
        }

        return matches;
    }

    @Transactional
    public void reportResult(Long matchId, Long winnerId, Long userId) {
        TournamentMatch match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Match not found"));

        if (!match.getStatus().equals("PENDING")) {
            throw new IllegalStateException(
                    "This match has already been completed");
        }

        if (!winnerId.equals(match.getTeamOneId()) &&
                !winnerId.equals(match.getTeamTwoId())) {
            throw new IllegalArgumentException(
                    "Winner must be one of the teams in this match");
        }

        match.setWinnerId(winnerId);
        match.setStatus("COMPLETED");
        matchRepository.save(match);

        // Check if round is complete — if so, generate next round
        advanceTournamentIfReady(match.getTournamentId(), match.getRound());
    }

    private void advanceTournamentIfReady(Long tournamentId, int round) {
        List<TournamentMatch> roundMatches = matchRepository
                .findByTournamentIdAndRoundOrderById(tournamentId, round);

        boolean roundComplete = roundMatches.stream()
                .allMatch(m -> m.getWinnerId() != null);

        if (!roundComplete) return;

        List<TournamentTeam> winners = roundMatches.stream()
                .map(m -> teamRepository.findById(m.getWinnerId()).orElseThrow())
                .toList();

        if (winners.size() == 1) {
            // Tournament over
            Tournament tournament = tournamentRepository
                    .findById(tournamentId).orElseThrow();
            tournament.setStatus("COMPLETED");
            tournamentRepository.save(tournament);
            log.info("Tournament {} completed. Winner: {}",
                    tournamentId, winners.get(0).getName());
        } else {
            // Generate next round
            generateRound(tournamentId, round + 1, winners);
            log.info("Generated round {} for tournament {}",
                    round + 1, tournamentId);
        }
    }

    public List<Tournament> getGuildTournaments(Long guildId) {
        return tournamentRepository.findByGuildIdOrderByCreatedAtDesc(guildId);
    }

    public List<TournamentMatch> getTournamentMatches(Long tournamentId) {
        return matchRepository
                .findByTournamentIdOrderByRoundAscIdAsc(tournamentId);
    }

    public List<TournamentTeam> getTournamentTeams(Long tournamentId) {
        return teamRepository.findByTournamentId(tournamentId);
    }
}