package com.example.verve.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class VerveService {
    private final ConcurrentMap<Integer, Long> uniqueIds = new ConcurrentHashMap<>();
    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Logger logger = LoggerFactory.getLogger(VerveService.class);

    public void processRequest(int id, String endpoint) {
        boolean isNew = redisTemplate.opsForSet().add("uniqueIds", id) == 1;

        if (isNew) {
            redisTemplate.expire("uniqueIds", Duration.ofMinutes(1));
            if (endpoint != null) {
                sendRequestToEndpoint(endpoint);
            }
        }
    }

    @Scheduled(fixedRate = 60000)
    public void logUniqueIdsCount() {
        long uniqueCount = redisTemplate.opsForSet().size("uniqueIds");

        // Publish the count to Kafka
        kafkaTemplate.send("unique-id-counts", String.valueOf(uniqueCount));

        // Log for monitoring
        logger.info("Unique count for the last minute sent to Kafka: {}", uniqueCount);
    }

    private void sendRequestToEndpoint(String endpoint) {
        long uniqueCount = uniqueIds.values().stream()
                .filter(timestamp -> timestamp / 60000 == System.currentTimeMillis() / 60000)
                .count();

        try {
            String jsonPayload = "{ \"uniqueCount\": " + uniqueCount + " }";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("HTTP status code from endpoint {}: {}", endpoint, response.statusCode());
        } catch (Exception e) {
            logger.error("Failed to send POST request to endpoint", e);
        }
    }

}
