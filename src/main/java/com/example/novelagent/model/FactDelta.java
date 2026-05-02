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
public class FactDelta {
    private String chapterId;
    private List<CharacterUpdate> characterUpdates;
    private List<RelationUpdate> relationUpdates;
    private List<TimelineEvent> timelineEvents;
    private List<PropUpdate> propUpdates;
    private List<ForeshadowingUpdate> foreshadowingUpdates;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CharacterUpdate {
        private String characterId;
        private String attribute;
        private String oldValue;
        private String newValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelationUpdate {
        private String characterId1;
        private String characterId2;
        private String relationType;
        private String oldValue;
        private String newValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineEvent {
        private String eventId;
        private String timestamp;
        private String description;
        private String location;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropUpdate {
        private String propId;
        private String attribute;
        private String oldValue;
        private String newValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForeshadowingUpdate {
        private String foreshadowingId;
        private String status;
        private String resolutionChapterId;
    }
}
