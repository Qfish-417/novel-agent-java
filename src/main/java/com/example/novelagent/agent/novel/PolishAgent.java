package com.example.novelagent.agent.novel;

import com.example.novelagent.agent.ReActAgent;
import com.example.novelagent.model.ChapterContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * 润色完善智能体
 * 负责：文风优化、节奏调整、对话改进、输出修改说明
 */
@Slf4j
@Component
public class PolishAgent extends ReActAgent {

    private final ChatClient chatClient;

    // 当前工作状态
    private ChapterContent draftContent;
    private String polishRules;
    private String factLocks;
    private String polishedContent;
    private String changeLog;

    public PolishAgent(ChatClient chatClient) {
        this.chatClient = chatClient;
        this.setName("PolishAgent");
        this.setSystemPrompt("你是一位专业的文字润色师，擅长提升小说的文学性和可读性。");
    }

    /**
     * 设置待润色的内容
     */
    public void setInput(ChapterContent draft, String polishRules, String factLocks) {
        this.draftContent = draft;
        this.polishRules = polishRules;
        this.factLocks = factLocks;
        this.polishedContent = null;
        this.changeLog = null;
    }

    /**
     * 执行润色
     */
    public String polish() {
        log.info("PolishAgent: 开始润色章节 {}", draftContent.getChapterId());

        String template = """
                请对以下小说章节进行润色优化：
                
                原始草稿：
                {draftContent}
                
                润色规则：
                {polishRules}
                
                禁改规则：
                {factLocks}
                
                润色要求：
                1. 优化文风，提升文学性
                2. 调整叙事节奏，增强阅读体验
                3. 改进对话的自然度和表现力
                4. 保持原有剧情和人物设定不变
                5. 遵循禁改规则，不得修改锁定的事实
                
                请输出：
                1. 润色后的完整章节内容
                2. 修改说明（列出主要修改点）
                
                输出格式：
                ---润色内容---
                [润色后的章节内容]
                
                ---修改说明---
                [逐条列出修改点和原因]
                """;

        String filledTemplate = template
                .replace("{draftContent}", draftContent.getContent())
                .replace("{polishRules}", polishRules != null ? polishRules : getDefaultPolishRules())
                .replace("{factLocks}", factLocks != null ? factLocks : "无特殊禁改规则");

        String result = chatClient.prompt()
                .system(this.getSystemPrompt())
                .user(filledTemplate)
                .call()
                .content();

        // 解析结果
        int separatorIndex = result.indexOf("---修改说明---");
        if (separatorIndex > 0) {
            polishedContent = result.substring("---润色内容---\n".length(), separatorIndex).trim();
            changeLog = result.substring(separatorIndex + "---修改说明---\n".length()).trim();
        } else {
            polishedContent = result;
            changeLog = "未提供详细修改说明";
        }

        log.info("PolishAgent: 章节 {} 润色完成", draftContent.getChapterId());
        return polishedContent;
    }

    /**
     * 获取修改说明
     */
    public String getChangeLog() {
        return changeLog;
    }

    /**
     * 获取默认润色规则
     */
    private String getDefaultPolishRules() {
        return """
                1. 使用更生动的词汇和比喻
                2. 增强场景描写的画面感
                3. 优化句子结构，提升流畅度
                4. 增强情感表达的感染力
                5. 保持适当的段落长度
                """;
    }

    @Override
    public boolean think() {
        if (draftContent == null) {
            log.info("PolishAgent: 没有待润色的内容");
            return false;
        }
        if (polishedContent == null) {
            log.info("PolishAgent: 需要执行润色");
            return true;
        }
        log.info("PolishAgent: 任务已完成");
        return false;
    }

    @Override
    public String act() {
        if (polishedContent == null && draftContent != null) {
            polish();
            return "章节 " + draftContent.getChapterId() + " 润色完成";
        }
        return "无需进一步行动";
    }
}
