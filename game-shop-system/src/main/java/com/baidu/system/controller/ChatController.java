package com.baidu.system.controller;

import com.baidu.common.ApiResponse;
import com.baidu.system.service.OllamaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class ChatController {

    @Autowired
    private OllamaService ollamaService;

    @GetMapping("/callChat")
    public ApiResponse<Map<String, String>> callChat(@RequestParam("question") String question) {
        String content = ollamaService.generate(question);
        return ApiResponse.ok(Map.of("content", content));
    }

    @GetMapping(value = "/streamChat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestParam("question") String question, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Connection", "keep-alive");
        SseEmitter emitter = new SseEmitter(0L);
        new Thread(() -> {
            try {
                String content = ollamaService.generate(question);
                emitter.send(SseEmitter.event().data(content));
                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }
}
