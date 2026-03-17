package com.gathix.moderation;

import com.gathix.guild.GuildService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ModerationService {

    private static final Logger log = LoggerFactory.getLogger(ModerationService.class);

    private final WarningRepository warningRepository;
    private final ModActionRepository modActionRepository;
    private final GuildService guildService;

    public ModerationService(WarningRepository warningRepository,
                             ModActionRepository modActionRepository,
                             GuildService guildService) {
        this.warningRepository = warningRepository;
        this.modActionRepository = modActionRepository;
        this.guildService = guildService;
    }

    public Warning warnUser(Long guildId, Long userId, Long moderatorId, String reason) {
        Warning warning = new Warning(guildId, userId, moderatorId, reason);
        warningRepository.save(warning);
        logAction(guildId, "WARN", userId, moderatorId, reason);
        return warning;
    }

    public List<Warning> getWarnings(Long guildId, Long userId) {
        return warningRepository.findByGuildIdAndUserId(guildId, userId);
    }

    public long getWarningCount(Long guildId, Long userId) {
        return warningRepository.countByGuildIdAndUserId(guildId, userId);
    }

    public void kickUser(Guild guild, Long userId, Long moderatorId, String reason) {
        guild.kick(UserSnowflake.fromId(userId))
                .reason(reason)
                .queue(
                        success -> logAction(guild.getIdLong(), "KICK",
                                userId, moderatorId, reason),
                        error -> log.error("Failed to kick user {}: {}",
                                userId, error.getMessage())
                );
    }

    public void banUser(Guild guild, Long userId, Long moderatorId,
                        String reason, int deleteMessageDays) {
        guild.ban(UserSnowflake.fromId(userId), deleteMessageDays,
                        java.util.concurrent.TimeUnit.DAYS)
                .reason(reason)
                .queue(
                        success -> logAction(guild.getIdLong(), "BAN",
                                userId, moderatorId, reason),
                        error -> log.error("Failed to ban user {}: {}",
                                userId, error.getMessage())
                );
    }

    public void unbanUser(Guild guild, Long userId, Long moderatorId, String reason) {
        guild.unban(UserSnowflake.fromId(userId))
                .reason(reason)
                .queue(
                        success -> logAction(guild.getIdLong(), "UNBAN",
                                userId, moderatorId, reason),
                        error -> log.error("Failed to unban user {}: {}",
                                userId, error.getMessage())
                );
    }

    private void logAction(Long guildId, String actionType, Long userId,
                           Long moderatorId, String reason) {
        ModAction action = new ModAction(guildId, actionType,
                userId, moderatorId, reason);
        modActionRepository.save(action);
        postToModLog(guildId, actionType, userId, moderatorId, reason);
    }

    private void postToModLog(Long guildId, String actionType, Long userId,
                              Long moderatorId, String reason) {
        com.gathix.guild.Guild guild = guildService.getGuild(guildId);
        if (guild == null || guild.getModLogChannel() == null) return;

        // We'll wire this up to JDA in the command handler
        // For now the action is persisted to the database
    }

    public EmbedBuilder buildModEmbed(String actionType, long userId,
                                      long moderatorId, String reason, Color color) {
        return new EmbedBuilder()
                .setTitle("Moderation Action — " + actionType)
                .setColor(color)
                .addField("User", "<@" + userId + ">", true)
                .addField("Moderator", "<@" + moderatorId + ">", true)
                .addField("Reason", reason, false)
                .setTimestamp(LocalDateTime.now()
                        .atZone(java.time.ZoneOffset.UTC).toInstant());
    }
}