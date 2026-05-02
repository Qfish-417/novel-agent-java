package com.example.novelagent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

/**
 * 流式输出服务
 * 提供 AI 模型的流式响应能力
 */
@Slf4j
@Service
public class StreamingService {

    private final ChatClient chatClient;

    public StreamingService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 流式生成响应
     * @param userPrompt 用户提示词
     * @param systemPrompt 系统提示词
     * @return Flux<String> 流式响应
     */
    public Flux<String> streamResponse(String userPrompt, String systemPrompt) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .stream()
                .content();
    }

    /**
     * 流式生成响应（带回调）
     * @param userPrompt 用户提示词
     * @param systemPrompt 系统提示词
     * @param callback 回调函数处理每个片段
     */
    public void streamResponseWithCallback(String userPrompt, String systemPrompt, Consumer<String> callback) {
        chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .stream()
                .content()
                .subscribe(
                    callback::accept,
                    error -> log.error("流式输出错误: {}", error.getMessage()),
                    () -> log.info("流式输出完成")
                );
    }

    /**
     * 流式生成响应（带选项）
     * @param userPrompt 用户提示词
     * @param systemPrompt 系统提示词
     * @param options 聊天选项
     * @return Flux<String> 流式响应
     */
    public Flux<String> streamResponse(String userPrompt, String systemPrompt, ChatOptions options) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .options(options)
                .stream()
                .content();
    }

    /**
     * 流式生成完整内容并拼接
     * @param userPrompt 用户提示词
     * @param systemPrompt 系统提示词
     * @return Flux<String> 完整的流式响应
     */
    public Flux<String> streamCompleteContent(String userPrompt, String systemPrompt) {
        return streamResponse(userPrompt, systemPrompt)
                .doOnComplete(() -> log.info("流式输出完成"))
                .doOnError(e -> log.error("流式输出错误: {}", e.getMessage()));
    }
}