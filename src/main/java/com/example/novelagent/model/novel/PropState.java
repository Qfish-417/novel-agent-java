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
public class PropState {
    private String propId;
    private String name;
    private String description;
    private String ownerId;
    private String location;
    private Map<String, String> attributes;
}
