package com.gathix.lfg;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LfgRepository extends JpaRepository<LfgPost, Long> {

    List<LfgPost> findByGuildIdAndStatusOrderByCreatedAtDesc(
            Long guildId, String status);

    List<LfgPost> findByGuildIdAndUserIdAndStatus(
            Long guildId, Long userId, String status);

    @Modifying
    @Transactional
    @Query("UPDATE LfgPost l SET l.status = 'EXPIRED' " +
            "WHERE l.expiresAt < :now AND l.status = 'OPEN'")
    int expirePosts(LocalDateTime now);
}