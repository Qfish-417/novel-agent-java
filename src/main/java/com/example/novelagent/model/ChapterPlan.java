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
public class ChapterPlan {
    private String chapterId;
    private String goal;
    private List<String> mustEvents;
    private List<String> forbiddenEvents;
    private List<String> foreshadowingToPlace;
    private List<String> foreshadowingToResolve;
    private String tone;
    private String pov;
}
