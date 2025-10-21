package com.baidu.system.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baidu.system.service.OllamaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class OllamaServiceImpl implements OllamaService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ollama.base-url:http://127.0.0.1:11434}")
    private String baseUrl;

    @Value("${ollama.model:qwen2}")
    private String model;

    public OllamaServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String generate(String prompt) {
        try {
            String url = baseUrl + "/api/generate";
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("prompt", prompt);
            body.put("stream", false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

            String resp = restTemplate.postForObject(url, req, String.class);
            if (resp == null) return "";
            JsonNode node = objectMapper.readTree(resp);
            JsonNode content = node.get("response");
            return content == null ? resp : content.asText();
        } catch (Exception e) {
            return "[AI调用失败] " + e.getMessage();
        }
    }
}
