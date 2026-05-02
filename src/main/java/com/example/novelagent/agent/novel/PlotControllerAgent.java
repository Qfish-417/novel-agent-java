package com.example.novelagent.agent.novel;

import com.example.novelagent.agent.ReActAgent;
import com.example.novelagent.model.ChapterAudit;
import com.example.novelagent.model.ChapterPlan;
import com.example.novelagent.model.novel.NovelState;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * 剧情把控智能体
 * 负责：生成章节任务卡、连续性审计（人设/世界观/时间线/道具/伏笔）
 */
@Slf4j
@Component
public class PlotControllerAgent extends ReActAgent {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    // 当前工作状态
    private NovelState novelState;
    private String currentChapterId;
    private ChapterPlan generatedPlan;
    private ChapterAudit lastAudit;

    public PlotControllerAgent(ChatClient chatClient, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.objectMapper = objectMapper;
        this.setName("PlotController");
        this.setSystemPrompt("你是一位资深的小说编辑和剧情策划师，擅长把控故事的连续性和逻辑性。");
    }

    /**
     * 设置当前工作上下文
     */
    public void setContext(NovelState novelState) {
        this.novelState = novelState;
        this.generatedPlan = null;
        this.lastAudit = null;
    }

    /**
     * 生成章节任务卡
     */
    public ChapterPlan generateChapterPlan(String chapterId) {
        log.info("PlotControllerAgent: 开始生成章节 {} 的任务卡", chapterId);
        this.currentChapterId = chapterId;

        String template = """
                根据当前小说状态，为第 {chapterId} 章生成详细的写作计划。
                
                当前小说状态：
                {novelState}
                
                请生成以下格式的章节计划：
                - 目标（goal）：本章需要达成的核心目标
                - 必须事件（mustEvents）：本章必须发生的事件列表
                - 禁止事件（forbiddenEvents）：本章绝对不能发生的事件列表
                - 需要埋设的伏笔（foreshadowingToPlace）：需要在本章埋设的伏笔
                - 需要回收的伏笔（foreshadowingToResolve）：需要在本章回收的伏笔
                - 语气（tone）：本章的整体语气风格
                - 视角（pov）：本章的叙事视角
                
                请确保计划符合小说的世界观和人设设定，保持故事的连续性。
                
                输出格式要求：JSON格式，包含字段：chapterId, goal, mustEvents, forbiddenEvents, foreshadowingToPlace, foreshadowingToResolve, tone, pov
                """;

        String filledTemplate = template
                .replace("{chapterId}", chapterId)
                .replace("{novelState}", novelState.toString());

        String content = chatClient.prompt()
                .system(this.getSystemPrompt())
                .user(filledTemplate)
                .call()
                .content();

        try {
            generatedPlan = objectMapper.readValue(content, ChapterPlan.class);
        } catch (JsonProcessingException e) {
            log.warn("解析章节计划失败，尝试提取JSON: {}", e.getMessage());
            generatedPlan = parseJsonContent(content, ChapterPlan.class);
        }
        
        if (generatedPlan == null) {
            generatedPlan = new ChapterPlan();
        }
        generatedPlan.setChapterId(chapterId);

        log.info("PlotControllerAgent: 章节 {} 的任务卡生成完成", chapterId);
        return generatedPlan;
    }

    /**
     * 审计章节内容
     */
    public ChapterAudit auditChapter(String chapterContent) {
        log.info("PlotControllerAgent: 开始审计章节 {}", currentChapterId);

        String template = """
                请根据以下小说状态和章节内容，进行全面的连续性审计：
                
                当前小说状态：
                {novelState}
                
                待审计章节内容：
                {chapterContent}
                
                审计项包括：
                1. 人设一致性 - 检查人物行为、性格、背景是否与设定一致
                2. 世界观规则一致性 - 检查是否符合世界设定规则
                3. 时间线一致性 - 检查时间顺序是否合理
                4. 道具状态一致性 - 检查道具位置和状态是否正确
                5. 伏笔埋设/回收状态一致性 - 检查伏笔是否合理埋设和回收
                6. 叙事视角和文体规则一致性 - 检查视角和风格是否统一
                
                请输出审计结果，格式要求：
                - pass: true/false
                - score: 0-100 的评分
                - issues: 问题清单，每项包含类型、描述、位置、严重程度
                - continuityChecks: 各项连续性检查的评分（每项0-100）
                - fixSuggestions: 修复建议列表
                
                审计标准：
                - 审计通过需要 pass=true 且 score >= 85
                - 不得出现硬冲突（人物年龄、时间、地点、道具状态）
                
                输出格式要求：JSON格式，包含字段：chapterId, pass, score, issues, continuityChecks, fixSuggestions
                """;

        String filledTemplate = template
                .replace("{novelState}", novelState.toString())
                .replace("{chapterContent}", chapterContent);

        String content = chatClient.prompt()
                .system(this.getSystemPrompt())
                .user(filledTemplate)
                .call()
                .content();

        try {
            lastAudit = objectMapper.readValue(content, ChapterAudit.class);
        } catch (JsonProcessingException e) {
            log.warn("解析章节审计失败，尝试提取JSON: {}", e.getMessage());
            lastAudit = parseJsonContent(content, ChapterAudit.class);
        }
        
        if (lastAudit == null) {
            lastAudit = new ChapterAudit();
        }
        lastAudit.setChapterId(currentChapterId);

        log.info("PlotControllerAgent: 章节 {} 审计完成，结果: pass={}, score={}", 
                currentChapterId, lastAudit.isPass(), lastAudit.getScore());
        return lastAudit;
    }

    /**
     * 尝试从内容中提取并解析JSON
     */
    private <T> T parseJsonContent(String content, Class<T> clazz) {
        int start = content.indexOf("{");
        int end = content.lastIndexOf("}") + 1;
        if (start >= 0 && end > start) {
            try {
                String jsonContent = content.substring(start, end);
                return objectMapper.readValue(jsonContent, clazz);
            } catch (JsonProcessingException e) {
                log.error("JSON解析失败: {}", e.getMessage());
            }
        }
        return null;
    }

    @Override
    public boolean think() {
        if (novelState == null) {
            log.info("PlotControllerAgent: 没有设置小说状态");
            return false;
        }
        if (generatedPlan == null) {
            log.info("PlotControllerAgent: 需要生成章节任务卡");
            return true;
        }
        log.info("PlotControllerAgent: 任务已完成");
        return false;
    }

    @Override
    public String act() {
        if (generatedPlan == null && currentChapterId != null) {
            generateChapterPlan(currentChapterId);
            return "章节 " + currentChapterId + " 任务卡生成完成";
        }
        return "无需进一步行动";
    }
}