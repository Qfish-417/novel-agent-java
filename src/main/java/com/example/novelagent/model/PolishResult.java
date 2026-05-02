package com.example.novelagent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolishResult {
    private String chapterId;
    private String polishedContent;
    private List<PolishChange> changes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PolishChange {
        private String type;
        private String original;
        private String modified;
        private String explanation;
    }
}
