package com.example.novelagent.tools;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class WebSearchTool extends SimpleTool {

    public WebSearchTool() {
        super("webSearch", "Search the web for information about a given query",
                Map.of("query", "The search query"));
    }

    @Override
    public Object call(Map<String, Object> arguments) {
        String query = (String) arguments.get("query");
        log.info("执行网页搜索: {}", query);
        return "搜索结果：这是一个模拟的搜索结果。在实际部署时，需要配置真实的搜索API。";
    }
}