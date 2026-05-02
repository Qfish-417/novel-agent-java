package com.example.novelagent.memory;

import com.example.novelagent.model.novel.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MemorySystem {

    private final Map<String, NovelState> novelStates = new HashMap<>();

    public void initializeNovel(String novelId, String title) {
        NovelState novelState = NovelState.builder()
                .novelId(novelId)
                .title(title)
                .characters(new ArrayList<>())
                .timeline(new ArrayList<>())
                .relations(new ArrayList<>())
                .props(new ArrayList<>())
                .foreshadowings(new ArrayList<>())
                .worldRules(WorldRules.builder()
                        .description("默认世界观")
                        .magicRules(new ArrayList<>())
                        .socialRules(new ArrayList<>())
                        .physicalRules(new ArrayList<>())
                        .techLevelRules(new ArrayList<>())
                        .build())
                .build();
        novelStates.put(novelId, novelState);
        log.info("MemorySystem: 初始化小说 - {}", novelId);
    }

    public NovelState getNovelState(String novelId) {
        return novelStates.getOrDefault(novelId, null);
    }

    public void updateNovelState(String novelId, NovelState updatedState) {
        novelStates.put(novelId, updatedState);
        log.info("MemorySystem: 更新小说状态 - {}", novelId);
    }

    public void addCharacter(String novelId, CharacterState character) {
        NovelState state = getNovelState(novelId);
        if (state != null) {
            state.getCharacters().add(character);
            log.info("MemorySystem: 添加人物 - {} - {}", novelId, character.getCharacterId());
        }
    }

    public void addTimelineEvent(String novelId, TimelineEntry event) {
        NovelState state = getNovelState(novelId);
        if (state != null) {
            state.getTimeline().add(event);
            log.info("MemorySystem: 添加时间线事件 - {} - {}", novelId, event.getEventId());
        }
    }

    public void addRelation(String novelId, RelationState relation) {
        NovelState state = getNovelState(novelId);
        if (state != null) {
            state.getRelations().add(relation);
            log.info("MemorySystem: 添加关系 - {} - {}-{}", novelId, 
                    relation.getCharacterId1(), relation.getCharacterId2());
        }
    }

    public void addProp(String novelId, PropState prop) {
        NovelState state = getNovelState(novelId);
        if (state != null) {
            state.getProps().add(prop);
            log.info("MemorySystem: 添加道具 - {} - {}", novelId, prop.getPropId());
        }
    }

    public void addForeshadowing(String novelId, ForeshadowingState foreshadowing) {
        NovelState state = getNovelState(novelId);
        if (state != null) {
            state.getForeshadowings().add(foreshadowing);
            log.info("MemorySystem: 添加伏笔 - {} - {}", novelId, foreshadowing.getForeshadowingId());
        }
    }

    public void updateWorldRules(String novelId, WorldRules rules) {
        NovelState state = getNovelState(novelId);
        if (state != null) {
            state.setWorldRules(rules);
            log.info("MemorySystem: 更新世界观规则 - {}", novelId);
        }
    }

    public void updateNovelState(String novelId, com.example.novelagent.model.ChapterContent chapterContent) {
        NovelState state = getNovelState(novelId);
        if (state != null) {
            log.info("MemorySystem: 根据章节内容更新小说状态 - {}", novelId);
        }
    }
}