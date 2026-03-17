package com.gathix.moderation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarningRepository extends JpaRepository<Warning, Long> {
    List<Warning> findByGuildIdAndUserId(Long guildId, Long userId);
    long countByGuildIdAndUserId(Long guildId, Long userId);
}