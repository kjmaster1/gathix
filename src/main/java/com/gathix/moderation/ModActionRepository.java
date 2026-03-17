package com.gathix.moderation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModActionRepository extends JpaRepository<ModAction, Long> {
    List<ModAction> findByGuildIdOrderByCreatedAtDesc(Long guildId);
}