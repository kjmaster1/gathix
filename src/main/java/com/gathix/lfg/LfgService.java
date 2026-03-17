package com.gathix.lfg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LfgService {

    private static final Logger log = LoggerFactory.getLogger(LfgService.class);
    private final LfgRepository lfgRepository;

    public LfgService(LfgRepository lfgRepository) {
        this.lfgRepository = lfgRepository;
    }

    public LfgPost createPost(Long guildId, Long userId, String game,
                              String description, int playersNeeded) {
        LfgPost post = new LfgPost(guildId, userId, game,
                description, playersNeeded);
        return lfgRepository.save(post);
    }

    public List<LfgPost> getOpenPosts(Long guildId) {
        return lfgRepository.findByGuildIdAndStatusOrderByCreatedAtDesc(
                guildId, "OPEN");
    }

    public Optional<LfgPost> getPost(Long postId) {
        return lfgRepository.findById(postId);
    }

    public LfgPost joinPost(Long postId, Long userId) {
        LfgPost post = lfgRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "LFG post not found"));

        if (!post.getStatus().equals("OPEN")) {
            throw new IllegalStateException("This LFG post is no longer open");
        }

        if (post.getUserId().equals(userId)) {
            throw new IllegalStateException(
                    "You cannot join your own LFG post");
        }

        if (post.isFull()) {
            throw new IllegalStateException("This LFG post is already full");
        }

        post.setPlayersJoined(post.getPlayersJoined() + 1);

        if (post.isFull()) {
            post.setStatus("FULL");
        }

        return lfgRepository.save(post);
    }

    public void closePost(Long postId, Long userId) {
        LfgPost post = lfgRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "LFG post not found"));

        if (!post.getUserId().equals(userId)) {
            throw new IllegalStateException(
                    "You can only close your own LFG posts");
        }

        post.setStatus("CLOSED");
        lfgRepository.save(post);
    }

    // Run every 5 minutes to expire old posts
    @Scheduled(fixedDelay = 300000)
    public void expireOldPosts() {
        int expired = lfgRepository.expirePosts(LocalDateTime.now());
        if (expired > 0) {
            log.info("Expired {} LFG post(s)", expired);
        }
    }
}