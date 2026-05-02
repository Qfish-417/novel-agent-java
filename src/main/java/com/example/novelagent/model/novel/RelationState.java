package com.example.novelagent.model.novel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelationState {
    private String characterId1;
    private String characterId2;
    private String relationType;
    private String description;
}
