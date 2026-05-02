package com.example.novelagent.model.novel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEntry {
    private String eventId;
    private String chapterId;
    private String timestamp;
    private String description;
    private String location;
}
