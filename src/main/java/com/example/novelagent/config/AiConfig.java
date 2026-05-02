package com.example.novelagent.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Configuration
public class AiConfig {

    @Value("${spring.ai.dashscope.chat.options.timeout:120000}")
    private long timeoutMs;

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultOptions(DashScopeChatOptions.builder()
                        .withInternalToolExecutionEnabled(false)
                        .build())
                .build();
    }

    public ChatResponse executeWithTimeout(Prompt prompt, ChatClient chatClient) {
        CompletableFuture<ChatResponse> future = CompletableFuture.supplyAsync(() -> {
            return chatClient.prompt(prompt)
                    .call()
                    .chatResponse();
        });

        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new RuntimeException("AI思考超时，已自动断开连接");
        } catch (Exception e) {
            throw new RuntimeException("AI执行异常：" + e.getMessage());
        }
    }
}