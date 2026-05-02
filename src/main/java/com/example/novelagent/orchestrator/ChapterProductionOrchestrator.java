package com.example.novelagent.orchestrator;

import com.example.novelagent.agent.novel.NovelMasterAgent;
import com.example.novelagent.agent.novel.PolishAgent;
import com.example.novelagent.agent.novel.PlotControllerAgent;
import com.example.novelagent.memory.MemorySystem;
import com.example.novelagent.model.novel.NovelState;
import com.example.novelagent.model.ChapterAudit;
import com.example.novelagent.model.ChapterContent;
import com.example.novelagent.model.ChapterPlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 章节生产流水线编排器
 * 负责协调多个智能体完成章节创作的完整流程
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChapterProductionOrchestrator {

    private final PlotControllerAgent plotControllerAgent;
    private final NovelMasterAgent novelMasterAgent;
    private final PolishAgent polishAgent;
    private final MemorySystem memorySystem;

    /**
     * 执行完整的章节生产流程
     *
     * @param projectId 项目ID
     * @param chapterId 章节ID
     * @return 最终完成的章节内容
     */
    public ChapterContent produceChapter(String projectId, String chapterId) throws Exception {
        log.info("ChapterProductionOrchestrator: 开始章节 {} 的生产流程", chapterId);

        // 1. 从记忆系统获取当前小说状态
        NovelState novelState = memorySystem.getNovelState(projectId);
        log.info("ChapterProductionOrchestrator: 获取小说状态完成");

        // 2. 生成章节任务卡
        plotControllerAgent.setContext(novelState);
        ChapterPlan chapterPlan = plotControllerAgent.generateChapterPlan(chapterId);
        log.info("ChapterProductionOrchestrator: 章节任务卡生成完成");

        // 3. 生成初稿
        novelMasterAgent.setContext(novelState, chapterPlan);
        ChapterContent draft = novelMasterAgent.generateDraft();
        log.info("ChapterProductionOrchestrator: 章节初稿生成完成");

        // 4. 润色完善
        polishAgent.setInput(draft, null, null);
        String polishedContent = polishAgent.polish();
        draft.setContent(polishedContent);
        log.info("ChapterProductionOrchestrator: 章节润色完成");

        // 5. 连续性审计
        ChapterAudit audit = plotControllerAgent.auditChapter(draft.getContent());
        
        // 6. 如果审计不通过，进行修订
        int revisionCount = 0;
        while (!audit.isPass() && revisionCount < 3) {
            log.warn("ChapterProductionOrchestrator: 章节 {} 审计未通过，进行第 {} 次修订", chapterId, revisionCount + 1);
            
            // 根据审计建议进行修订
            String revisionSuggestions = audit.getFixSuggestions().toString();
            draft = novelMasterAgent.reviseDraft(revisionSuggestions);
            
            // 重新审计
            audit = plotControllerAgent.auditChapter(draft.getContent());
            revisionCount++;
        }

        if (!audit.isPass()) {
            log.error("ChapterProductionOrchestrator: 章节 {} 经过 {} 次修订仍未通过审计", chapterId, revisionCount);
            throw new RuntimeException("章节 " + chapterId + " 审计未通过");
        }

        // 7. 保存章节内容
        novelMasterAgent.saveDraft(projectId);
        log.info("ChapterProductionOrchestrator: 章节 {} 已保存", chapterId);

        // 8. 更新记忆系统
        memorySystem.updateNovelState(projectId, draft);
        log.info("ChapterProductionOrchestrator: 记忆系统已更新");

        log.info("ChapterProductionOrchestrator: 章节 {} 生产流程完成", chapterId);
        return draft;
    }

    /**
     * 执行简化的章节生产流程（跳过润色步骤）
     */
    public ChapterContent produceChapterSimple(String projectId, String chapterId) throws Exception {
        log.info("ChapterProductionOrchestrator: 开始章节 {} 的简化生产流程", chapterId);

        // 1. 从记忆系统获取当前小说状态
        NovelState novelState = memorySystem.getNovelState(projectId);

        // 2. 生成章节任务卡
        plotControllerAgent.setContext(novelState);
        ChapterPlan chapterPlan = plotControllerAgent.generateChapterPlan(chapterId);

        // 3. 生成初稿
        novelMasterAgent.setContext(novelState, chapterPlan);
        ChapterContent draft = novelMasterAgent.generateDraft();

        // 4. 连续性审计
        ChapterAudit audit = plotControllerAgent.auditChapter(draft.getContent());

        // 5. 如果审计不通过，进行修订
        int revisionCount = 0;
        while (!audit.isPass() && revisionCount < 3) {
            String revisionSuggestions = audit.getFixSuggestions().toString();
            draft = novelMasterAgent.reviseDraft(revisionSuggestions);
            audit = plotControllerAgent.auditChapter(draft.getContent());
            revisionCount++;
        }

        if (!audit.isPass()) {
            throw new RuntimeException("章节 " + chapterId + " 审计未通过");
        }

        // 6. 保存章节内容
        novelMasterAgent.saveDraft(projectId);

        // 7. 更新记忆系统
        memorySystem.updateNovelState(projectId, draft);

        return draft;
    }
}
