package com.example.novelagent.agent.novel;

import com.example.novelagent.agent.ReActAgent;
import com.example.novelagent.model.novel.NovelState;
import com.example.novelagent.model.ChapterContent;
import com.example.novelagent.model.ChapterPlan;
import com.example.novelagent.storage.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 小说主控智能体
 * 负责：主控调度、生成初稿、数据落库、返工修订
 */
@Slf4j
@Component
public class NovelMasterAgent extends ReActAgent {

    private final ChatClient chatClient;
    private final FileStorageService fileStorageService;

    // 当前工作状态
    private ChapterPlan currentPlan;
    private ChapterContent currentDraft;
    private NovelState novelState;

    public NovelMasterAgent(ChatClient chatClient, FileStorageService fileStorageService) {
        this.chatClient = chatClient;
        this.fileStorageService = fileStorageService;
        this.setName("NovelMaster");
        this.setSystemPrompt("你是一位资深小说作家，擅长创作长篇小说。请根据提供的章节计划和小说状态，创作高质量的小说章节内容。");
    }

    /**
     * 设置当前工作上下文
     */
    public void setContext(NovelState novelState, ChapterPlan plan) {
        this.novelState = novelState;
        this.currentPlan = plan;
        this.currentDraft = null;
    }

    /**
     * 生成章节初稿（同步方式）
     */
    public ChapterContent generateDraft() {
        log.info("NovelMasterAgent: 开始生成章节 {} 初稿", currentPlan.getChapterId());

        String template = """
                根据以下章节计划和小说状态，创作小说章节内容：
                
                章节计划：
                {chapterPlan}
                
                当前小说状态：
                {novelState}
                
                写作要求：
                1. 严格按照章节计划的目标和要求进行创作
                2. 保持与小说状态的一致性（人设、世界观、时间线等）
                3. 使用生动的描写和细腻的情感表达
                4. 注意节奏把控，保持读者的阅读兴趣
                5. 输出格式：纯文本小说内容，无需额外说明
                
                请输出完整的章节内容：
                """;

        String filledTemplate = template
                .replace("{chapterPlan}", currentPlan.toString())
                .replace("{novelState}", novelState.toString());

        String content = chatClient.prompt()
                .system(this.getSystemPrompt())
                .user(filledTemplate)
                .call()
                .content();

        currentDraft = ChapterContent.builder()
                .chapterId(currentPlan.getChapterId())
                .content(content)
                .version("1")
                .build();

        log.info("NovelMasterAgent: 章节 {} 初稿生成完成", currentPlan.getChapterId());
        return currentDraft;
    }

    /**
     * 生成章节初稿（流式方式）
     * @return Flux<String> 流式响应
     */
    public Flux<String> generateDraftStream() {
        log.info("NovelMasterAgent: 开始流式生成章节 {} 初稿", currentPlan.getChapterId());

        String template = """
                根据以下章节计划和小说状态，创作小说章节内容：
                
                章节计划：
                {chapterPlan}
                
                当前小说状态：
                {novelState}
                
                写作要求：
                1. 严格按照章节计划的目标和要求进行创作
                2. 保持与小说状态的一致性（人设、世界观、时间线等）
                3. 使用生动的描写和细腻的情感表达
                4. 注意节奏把控，保持读者的阅读兴趣
                5. 输出格式：纯文本小说内容，无需额外说明
                
                请输出完整的章节内容：
                """;

        String filledTemplate = template
                .replace("{chapterPlan}", currentPlan.toString())
                .replace("{novelState}", novelState.toString());

        return chatClient.prompt()
                .system(this.getSystemPrompt())
                .user(filledTemplate)
                .stream()
                .content()
                .doOnNext(content -> {
                    // 累积内容到 currentDraft
                    if (currentDraft == null) {
                        currentDraft = ChapterContent.builder()
                                .chapterId(currentPlan.getChapterId())
                                .content(content)
                                .version("1")
                                .build();
                    } else {
                        currentDraft.setContent(currentDraft.getContent() + content);
                    }
                })
                .doOnComplete(() -> {
                    log.info("NovelMasterAgent: 章节 {} 流式初稿生成完成", currentPlan.getChapterId());
                });
    }

    /**
     * 修订章节内容（同步方式）
     */
    public ChapterContent reviseDraft(String revisionSuggestions) {
        log.info("NovelMasterAgent: 开始修订章节 {}", currentPlan.getChapterId());

        String template = """
                根据以下修订建议对章节内容进行修改：
                
                原始内容：
                {draftContent}
                
                修订建议：
                {revisionSuggestions}
                
                修改要求：
                1. 根据修订建议进行针对性修改
                2. 保持原有剧情和人物设定不变
                3. 确保修改后内容连贯自然
                
                请输出修改后的完整章节内容：
                """;

        String filledTemplate = template
                .replace("{draftContent}", currentDraft.getContent())
                .replace("{revisionSuggestions}", revisionSuggestions);

        String revisedContent = chatClient.prompt()
                .system(this.getSystemPrompt())
                .user(filledTemplate)
                .call()
                .content();

        currentDraft.setContent(revisedContent);
        currentDraft.setVersion(String.valueOf(Integer.parseInt(currentDraft.getVersion()) + 1));

        log.info("NovelMasterAgent: 章节 {} 修订完成，版本 {}", currentPlan.getChapterId(), currentDraft.getVersion());
        return currentDraft;
    }

    /**
     * 修订章节内容（流式方式）
     * @return Flux<String> 流式响应
     */
    public Flux<String> reviseDraftStream(String revisionSuggestions) {
        log.info("NovelMasterAgent: 开始流式修订章节 {}", currentPlan.getChapterId());

        String template = """
                根据以下修订建议对章节内容进行修改：
                
                原始内容：
                {draftContent}
                
                修订建议：
                {revisionSuggestions}
                
                修改要求：
                1. 根据修订建议进行针对性修改
                2. 保持原有剧情和人物设定不变
                3. 确保修改后内容连贯自然
                
                请输出修改后的完整章节内容：
                """;

        String filledTemplate = template
                .replace("{draftContent}", currentDraft.getContent())
                .replace("{revisionSuggestions}", revisionSuggestions);

        final String originalVersion = currentDraft.getVersion();

        return chatClient.prompt()
                .system(this.getSystemPrompt())
                .user(filledTemplate)
                .stream()
                .content()
                .doOnNext(content -> {
                    // 累积修订内容
                    currentDraft.setContent(currentDraft.getContent() + content);
                })
                .doOnComplete(() -> {
                    currentDraft.setVersion(String.valueOf(Integer.parseInt(originalVersion) + 1));
                    log.info("NovelMasterAgent: 章节 {} 流式修订完成，版本 {}", currentPlan.getChapterId(), currentDraft.getVersion());
                });
    }

    /**
     * 保存章节内容到文件
     */
    public void saveDraft(String projectId) throws Exception {
        if (currentDraft != null) {
            int version = Integer.parseInt(currentDraft.getVersion());
            fileStorageService.saveChapterDraft(projectId, currentDraft.getChapterId(),
                    currentDraft.getContent(), version);
            log.info("NovelMasterAgent: 章节 {} 已保存", currentPlan.getChapterId());
        }
    }

    @Override
    public boolean think() {
        // 检查是否有未完成的任务
        if (currentPlan == null) {
            log.info("NovelMasterAgent: 没有待处理的章节计划");
            return false;
        }
        if (currentDraft == null) {
            log.info("NovelMasterAgent: 需要生成初稿");
            return true;
        }
        log.info("NovelMasterAgent: 任务已完成");
        return false;
    }

    @Override
    public String act() {
        if (currentDraft == null) {
            generateDraft();
            return "章节 " + currentPlan.getChapterId() + " 初稿生成完成";
        }
        return "无需进一步行动";
    }
}