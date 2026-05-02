package com.example.novelagent.model.novel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForeshadowingState {
    private String foreshadowingId;
    private String description;
    private String plantedChapterId;
    private String resolvedChapterId;
    private String status;
}
