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
public class WorldRules {
    private String description;
    private List<String> magicRules;
    private List<String> socialRules;
    private List<String> physicalRules;
    private List<String> techLevelRules;
}
