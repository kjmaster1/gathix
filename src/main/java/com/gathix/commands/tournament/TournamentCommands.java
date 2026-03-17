package com.gathix.commands.tournament;

import com.gathix.tournament.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TournamentCommands extends ListenerAdapter {

    private final TournamentService tournamentService;

    public TournamentCommands(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    public static List<CommandData> getCommands() {
        return List.of(
                // Admin commands — requires Manage Server
                Commands.slash("tournament-admin", "Tournament administration")
                        .setDefaultPermissions(DefaultMemberPermissions
                                .enabledFor(Permission.MANAGE_SERVER))
                        .addSubcommands(
                                new SubcommandData("create",
                                        "Create a new tournament")
                                        .addOption(OptionType.STRING, "name",
                                                "Tournament name", true)
                                        .addOption(OptionType.STRING, "game",
                                                "Game being played", true)
                                        .addOption(OptionType.INTEGER, "max_teams",
                                                "Maximum number of teams (2-32)",
                                                true),
                                new SubcommandData("start",
                                        "Start the tournament")
                                        .addOption(OptionType.INTEGER, "id",
                                                "Tournament ID", true),
                                new SubcommandData("result",
                                        "Report a match result")
                                        .addOption(OptionType.INTEGER, "match_id",
                                                "Match ID", true)
                                        .addOption(OptionType.INTEGER, "winner_id",
                                                "Winning team ID", true)
                        ),

                // Player commands — available to everyone
                Commands.slash("tournament", "Tournament system")
                        .addSubcommands(
                                new SubcommandData("register",
                                        "Register your team")
                                        .addOption(OptionType.INTEGER, "id",
                                                "Tournament ID", true)
                                        .addOption(OptionType.STRING, "team_name",
                                                "Your team name", true),
                                new SubcommandData("bracket",
                                        "View the tournament bracket")
                                        .addOption(OptionType.INTEGER, "id",
                                                "Tournament ID", true),
                                new SubcommandData("list",
                                        "List tournaments in this server")
                        )
        );
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("tournament") &&
                !event.getName().equals("tournament-admin")) return;

        String subcommand = event.getSubcommandName();
        if (subcommand == null) return;

        switch (subcommand) {
            case "create" -> handleCreate(event);
            case "register" -> handleRegister(event);
            case "start" -> handleStart(event);
            case "bracket" -> handleBracket(event);
            case "result" -> handleResult(event);
            case "list" -> handleList(event);
        }
    }

    private void handleCreate(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) return;

        String name = event.getOption("name").getAsString();
        String game = event.getOption("game").getAsString();
        long maxTeams = event.getOption("max_teams").getAsLong();

        try {
            Tournament tournament = tournamentService.createTournament(
                    event.getGuild().getIdLong(),
                    name, game, (int) maxTeams,
                    event.getUser().getIdLong()
            );

            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("🏆 Tournament Created — " + name)
                    .setColor(new Color(255, 215, 0))
                    .addField("Game", game, true)
                    .addField("Max Teams", String.valueOf(maxTeams), true)
                    .addField("Tournament ID", "#" + tournament.getId(), true)
                    .addField("Status", "Open for Registration", false)
                    .setFooter("Use /tournament register " + tournament.getId()
                            + " <team name> to register")
                    .build()
            ).queue();

        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    private void handleRegister(SlashCommandInteractionEvent event) {
        long tournamentId = event.getOption("id").getAsLong();
        String teamName = event.getOption("team_name").getAsString();

        try {
            TournamentTeam team = tournamentService.registerTeam(
                    tournamentId, teamName,
                    event.getUser().getIdLong()
            );

            event.reply(String.format(
                    "✅ **%s** registered for tournament #%d! (Team ID: %d)",
                    teamName, tournamentId, team.getId()
            )).queue();

        } catch (IllegalArgumentException | IllegalStateException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    private void handleStart(SlashCommandInteractionEvent event) {
        long tournamentId = event.getOption("id").getAsLong();

        try {
            List<TournamentMatch> matches = tournamentService.startTournament(
                    tournamentId, event.getUser().getIdLong()
            );

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🏆 Tournament Started!")
                    .setColor(new Color(255, 215, 0))
                    .setDescription("Round 1 matches:");

            for (TournamentMatch match : matches) {
                String matchInfo;
                if (match.getStatus().equals("BYE")) {
                    var team = tournamentService.getTournamentTeams(tournamentId)
                            .stream()
                            .filter(t -> t.getId().equals(match.getTeamOneId()))
                            .findFirst().orElse(null);
                    matchInfo = (team != null ? team.getName() : "?") + " — BYE";
                } else {
                    var teams = tournamentService.getTournamentTeams(tournamentId);
                    var teamOne = teams.stream()
                            .filter(t -> t.getId().equals(match.getTeamOneId()))
                            .findFirst().orElse(null);
                    var teamTwo = teams.stream()
                            .filter(t -> t.getId().equals(match.getTeamTwoId()))
                            .findFirst().orElse(null);
                    matchInfo = String.format("%s vs %s",
                            teamOne != null ? teamOne.getName() : "?",
                            teamTwo != null ? teamTwo.getName() : "?");
                }
                embed.addField("Match #" + match.getId(), matchInfo, false);
            }

            embed.setFooter("Use /tournament result <match_id> <winner_id> " +
                    "to report results");
            event.replyEmbeds(embed.build()).queue();

        } catch (IllegalArgumentException | IllegalStateException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    private void handleBracket(SlashCommandInteractionEvent event) {
        long tournamentId = event.getOption("id").getAsLong();

        try {
            Tournament tournament = tournamentService
                    .getGuildTournaments(event.getGuild().getIdLong())
                    .stream()
                    .filter(t -> t.getId().equals(tournamentId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Tournament not found"));

            List<TournamentMatch> matches =
                    tournamentService.getTournamentMatches(tournamentId);
            List<TournamentTeam> teams =
                    tournamentService.getTournamentTeams(tournamentId);

            Map<Long, String> teamNames = teams.stream()
                    .collect(Collectors.toMap(
                            TournamentTeam::getId, TournamentTeam::getName));

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🏆 " + tournament.getName() + " — Bracket")
                    .setColor(new Color(255, 215, 0))
                    .addField("Game", tournament.getGame(), true)
                    .addField("Status", tournament.getStatus(), true);

            int currentRound = 0;
            for (TournamentMatch match : matches) {
                if (match.getRound() != currentRound) {
                    currentRound = match.getRound();
                }

                String teamOne = teamNames.getOrDefault(
                        match.getTeamOneId(), "TBD");
                String teamTwo = match.getTeamTwoId() != null
                        ? teamNames.getOrDefault(match.getTeamTwoId(), "TBD")
                        : "BYE";

                String result = "";
                if (match.getWinnerId() != null) {
                    result = " ✅ **" +
                            teamNames.getOrDefault(match.getWinnerId(), "?")
                            + " wins**";
                }

                embed.addField(
                        "R" + match.getRound() + " Match #" + match.getId(),
                        teamOne + " vs " + teamTwo + result,
                        false
                );
            }

            event.replyEmbeds(embed.build()).queue();

        } catch (IllegalArgumentException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    private void handleResult(SlashCommandInteractionEvent event) {
        long matchId = event.getOption("match_id").getAsLong();
        long winnerId = event.getOption("winner_id").getAsLong();

        try {
            tournamentService.reportResult(matchId, winnerId,
                    event.getUser().getIdLong());

            event.reply(String.format(
                    "✅ Result reported — Team ID %d wins Match #%d!",
                    winnerId, matchId
            )).queue();

        } catch (IllegalArgumentException | IllegalStateException e) {
            event.reply(e.getMessage()).setEphemeral(true).queue();
        }
    }

    private void handleList(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) return;

        List<Tournament> tournaments = tournamentService
                .getGuildTournaments(event.getGuild().getIdLong());

        if (tournaments.isEmpty()) {
            event.reply("No tournaments found. Use `/tournament create` " +
                    "to create one!").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🏆 Tournaments")
                .setColor(new Color(255, 215, 0));

        for (Tournament t : tournaments) {
            long teamCount = tournamentService
                    .getTournamentTeams(t.getId()).size();
            embed.addField(
                    "#" + t.getId() + " — " + t.getName(),
                    String.format("**%s** | %s | %d/%d teams",
                            t.getGame(), t.getStatus(),
                            teamCount, t.getMaxTeams()),
                    false
            );
        }

        event.replyEmbeds(embed.build()).queue();
    }
}