package com.example.novelagent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterContent {
    private String chapterId;
    private String content;
    private String version;
    private String type;
    private String summary;
    private FactDelta factDelta;
}
