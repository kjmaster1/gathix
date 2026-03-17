package com.gathix.guild;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "guilds")
@Getter
@Setter
@NoArgsConstructor
public class Guild {

    @Id
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "mod_log_channel")
    private Long modLogChannel;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    public Guild(Long id, String name) {
        this.id = id;
        this.name = name;
        this.joinedAt = LocalDateTime.now();
    }
}