package com.example.novelagent.model.novel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CharacterState {
    private String characterId;
    private String name;
    private String description;
    private Map<String, String> attributes;
    private String currentLocation;
    private String currentStatus;
}
