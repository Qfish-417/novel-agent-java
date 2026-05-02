package com.example.novelagent.tools;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class SimpleTool implements Tool {

    protected final String name;
    protected final String description;
    protected final Map<String, String> parameters;

    public SimpleTool(String name, String description, Map<String, String> parameters) {
        this.name = name;
        this.description = description;
        this.parameters = parameters != null ? parameters : new HashMap<>();
    }

    @Override
    public abstract Object call(Map<String, Object> arguments);
}