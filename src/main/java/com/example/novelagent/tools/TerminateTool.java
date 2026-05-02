package com.example.novelagent.tools;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class TerminateTool extends SimpleTool {

    public TerminateTool() {
        super("doTerminate", "Terminate the current agent task when it is completed or needs to stop",
                Map.of("reason", "The reason for termination"));
    }

    @Override
    public Object call(Map<String, Object> arguments) {
        String reason = (String) arguments.get("reason");
        if (reason == null) {
            reason = "任务完成";
        }
        log.info("任务终止: {}", reason);
        return "任务已终止，原因: " + reason;
    }
}