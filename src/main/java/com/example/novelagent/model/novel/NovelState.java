package com.example.novelagent.model.novel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NovelState {
    private String novelId;
    private String title;
    private List<CharacterState> characters;
    private List<TimelineEntry> timeline;
    private List<RelationState> relations;
    private List<PropState> props;
    private List<ForeshadowingState> foreshadowings;
    private WorldRules worldRules;
}
