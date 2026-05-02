package com.example.novelagent.tools;

import java.util.Map;

public interface Tool {
    String getName();
    String getDescription();
    Map<String, String> getParameters();
    Object call(Map<String, Object> arguments);
}