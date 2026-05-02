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
public class ChapterAudit {
    private String chapterId;
    private boolean pass;
    private int score;
    private List<AuditIssue> issues;
    private ContinuityChecks continuityChecks;
    private List<String> fixSuggestions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditIssue {
        private String type;
        private String description;
        private String location;
        private String severity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContinuityChecks {
        private int characterConsistency;
        private int worldRulesConsistency;
        private int timelineConsistency;
        private int propStateConsistency;
        private int foreshadowingConsistency;
        private int narrativePerspectiveConsistency;
    }
}
