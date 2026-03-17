package com.gathix.guild;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GuildService {

    private static final Logger log = LoggerFactory.getLogger(GuildService.class);
    private final GuildRepository guildRepository;

    public GuildService(GuildRepository guildRepository) {
        this.guildRepository = guildRepository;
    }

    public void registerGuild(Long id, String name) {
        if (!guildRepository.existsById(id)) {
            Guild guild = new Guild(id, name);
            guildRepository.save(guild);
            log.info("Registered new guild: {} ({})", name, id);
        }
    }

    public void removeGuild(Long id) {
        guildRepository.deleteById(id);
        log.info("Removed guild: {}", id);
    }

    public Guild getGuild(Long id) {
        return guildRepository.findById(id).orElse(null);
    }

    public void updateModLogChannel(Long guildId, Long channelId) {
        guildRepository.findById(guildId).ifPresent(guild -> {
            guild.setModLogChannel(channelId);
            guildRepository.save(guild);
        });
    }
}