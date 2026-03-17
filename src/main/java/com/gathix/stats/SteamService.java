package com.gathix.stats;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class SteamService {

    private static final Logger log = LoggerFactory.getLogger(SteamService.class);
    private static final String STEAM_API_BASE = "https://api.steampowered.com";
    private static final String STEAM_STORE_BASE = "https://store.steampowered.com";

    @Value("${gathix.steam.api-key}")
    private String apiKey;

    private final WebClient webClient;

    public SteamService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    // Resolve a vanity URL (username) to a Steam64 ID
    public String resolveVanityUrl(String vanityUrl) {
        try {
            JsonNode response = webClient.get()
                    .uri(STEAM_API_BASE + "/ISteamUser/ResolveVanityURL/v1/" +
                                    "?key={key}&vanityurl={vanity}",
                            apiKey, vanityUrl)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            JsonNode result = response.path("response");
            if (result.path("success").asInt() == 1) {
                return result.path("steamid").asText();
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to resolve vanity URL {}: {}", vanityUrl, e.getMessage());
            return null;
        }
    }

    // Get player summary (profile info)
    public JsonNode getPlayerSummary(String steamId) {
        try {
            JsonNode response = webClient.get()
                    .uri(STEAM_API_BASE + "/ISteamUser/GetPlayerSummaries/v2/" +
                                    "?key={key}&steamids={id}",
                            apiKey, steamId)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            JsonNode players = response.path("response").path("players");
            if (players.isArray() && players.size() > 0) {
                return players.get(0);
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to get player summary for {}: {}", steamId, e.getMessage());
            return null;
        }
    }

    // Get owned games count and total playtime
    public JsonNode getOwnedGames(String steamId) {
        try {
            JsonNode response = webClient.get()
                    .uri(STEAM_API_BASE + "/IPlayerService/GetOwnedGames/v1/" +
                                    "?key={key}&steamid={id}&include_appinfo=false",
                            apiKey, steamId)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            return response.path("response");
        } catch (Exception e) {
            log.error("Failed to get owned games for {}: {}", steamId, e.getMessage());
            return null;
        }
    }

    // Get recently played games (last 2 weeks)
    public JsonNode getRecentGames(String steamId) {
        try {
            JsonNode response = webClient.get()
                    .uri(STEAM_API_BASE + "/IPlayerService/GetRecentlyPlayedGames/v1/" +
                                    "?key={key}&steamid={id}&count=3",
                            apiKey, steamId)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            return response.path("response");
        } catch (Exception e) {
            log.error("Failed to get recent games for {}: {}", steamId, e.getMessage());
            return null;
        }
    }

    // Determine persona state string
    public String getPersonaState(int state) {
        return switch (state) {
            case 0 -> "Offline";
            case 1 -> "Online";
            case 2 -> "Busy";
            case 3 -> "Away";
            case 4 -> "Snooze";
            case 5 -> "Looking to Trade";
            case 6 -> "Looking to Play";
            default -> "Unknown";
        };
    }
}