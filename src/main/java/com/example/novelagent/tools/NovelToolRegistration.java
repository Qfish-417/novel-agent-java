package com.example.novelagent.tools;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class NovelToolRegistration {

    @Bean
    public List<Tool> novelTools() {
        return List.of(
                new WebSearchTool(),
                new FileOperationTool(),
                new TerminateTool()
        );
    }

    @Bean
    public WebSearchTool webSearchTool() {
        return new WebSearchTool();
    }

    @Bean
    public FileOperationTool fileOperationTool() {
        return new FileOperationTool();
    }

    @Bean
    public TerminateTool terminateTool() {
        return new TerminateTool();
    }
}