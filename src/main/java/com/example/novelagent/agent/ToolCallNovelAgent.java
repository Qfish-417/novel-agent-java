package com.example.novelagent.agent;

import cn.hutool.core.util.StrUtil;
import com.example.novelagent.agent.model.AgentState;
import com.example.novelagent.tools.Tool;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 工具调用智能体 - One-by-One 模式
 * 每次只处理一个工具调用，等待结果后再进行下一次思考
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallNovelAgent extends ReActAgent {

    private final List<Tool> availableTools;
    private ChatResponse toolCallChatResponse;
    private final ChatOptions chatOptions;
    private final long timeoutMs;

    private static final Pattern TOOL_CALL_PATTERN = Pattern.compile(
            "\\{\\{tool\\s+name=\"([^\"]+)\"\\s+(.*?)\\}\\}",
            Pattern.DOTALL
    );

    public ToolCallNovelAgent(List<Tool> availableTools, ChatOptions chatOptions, long timeoutMs) {
        this.availableTools = availableTools;
        this.chatOptions = chatOptions;
        this.timeoutMs = timeoutMs;
        super.setMaxSteps(10);
    }

    /**
     * One-by-One 思考模式：每次只决定是否调用工具
     * @return true 如果需要调用工具，false 如果直接回答
     */
    @Override
    public boolean think() {
        if (StrUtil.isNotBlank(getNextStepPrompt())) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
            log.info("[{}] 用户输入: {}", getName(), getNextStepPrompt());
            setNextStepPrompt(null);
        }

        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, this.chatOptions);

        try {
            // 构建工具描述（One-by-One 模式：清晰列出每个工具）
            String toolDescriptions = availableTools.stream()
                    .map(t -> String.format("工具 [%s]: %s\n  参数: %s",
                            t.getName(), t.getDescription(), t.getParameters()))
                    .collect(Collectors.joining("\n\n"));

            String systemPrompt = getSystemPrompt() + "\n\n=== 可用工具 ===" + "\n" + toolDescriptions + 
                    "\n\n=== 工具调用格式 ===" + 
                    "\n如需调用工具，请使用：{{tool name=\"工具名\" 参数名=\"参数值\"}}" +
                    "\n每次只能调用一个工具，请等待工具执行完成后再进行下一步。";

            log.info("[{}] 开始思考 (步骤 {}/{})", getName(), getCurrentStep(), getMaxSteps());
            
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(systemPrompt)
                    .call()
                    .chatResponse();

            this.toolCallChatResponse = chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            String result = assistantMessage.getText();

            log.info("[{}] 思考结果: {}", getName(), result);

            // One-by-One 模式：检查是否需要调用工具
            boolean hasToolCall = result.contains("{{tool");
            if (hasToolCall) {
                log.info("[{}] 决定调用工具", getName());
            }

            getMessageList().add(assistantMessage);
            return hasToolCall;
        } catch (Exception e) {
            log.error("[{}] 思考过程遇到问题: {}", getName(), e.getMessage());
            getMessageList().add(new AssistantMessage("处理时遇到错误: " + e.getMessage()));
            return false;
        }
    }

    /**
     * One-by-One 执行模式：每次只执行一个工具调用
     * @return 工具执行结果
     */
    @Override
    public String act() {
        AssistantMessage assistantMessage = toolCallChatResponse.getResult().getOutput();
        String result = assistantMessage.getText();

        if (!result.contains("{{tool")) {
            log.info("[{}] 无需调用工具，直接返回结果", getName());
            return result;
        }

        Matcher matcher = TOOL_CALL_PATTERN.matcher(result);
        
        // One-by-One 模式：只处理第一个工具调用
        if (matcher.find()) {
            String toolName = matcher.group(1);
            String paramsStr = matcher.group(2);

            log.info("[{}] 执行工具调用: {} ({})", getName(), toolName, paramsStr);

            Tool tool = availableTools.stream()
                    .filter(t -> t.getName().equals(toolName))
                    .findFirst()
                    .orElse(null);

            if (tool == null) {
                String errorMsg = "[工具未找到] " + toolName;
                log.warn(errorMsg);
                return errorMsg;
            }

            Map<String, Object> args = parseParameters(paramsStr);
            
            try {
                // 执行工具调用
                Object toolResult = tool.call(args);
                String resultStr = "[工具返回] " + toolName + ": " + toolResult;
                log.info("[{}] {}", getName(), resultStr);

                // 将工具执行结果添加到消息历史，供下一次思考使用
                getMessageList().add(new UserMessage(resultStr));

                // 检查是否是终止工具
                if ("doTerminate".equals(toolName)) {
                    setState(AgentState.FINISHED);
                    log.info("[{}] 任务已终止", getName());
                }

                return resultStr;
            } catch (Exception e) {
                String errorMsg = "[工具执行失败] " + toolName + ": " + e.getMessage();
                log.error(errorMsg);
                getMessageList().add(new UserMessage(errorMsg));
                return errorMsg;
            }
        }

        return "未找到有效的工具调用";
    }

    /**
     * 解析工具参数
     */
    private Map<String, Object> parseParameters(String paramsStr) {
        Map<String, Object> args = new HashMap<>();
        Pattern paramPattern = Pattern.compile("(\\w+)=\"([^\"]+)\"");
        Matcher paramMatcher = paramPattern.matcher(paramsStr);

        while (paramMatcher.find()) {
            args.put(paramMatcher.group(1), paramMatcher.group(2));
        }

        return args;
    }

    /**
     * 执行完整的思考-行动循环（One-by-One 模式）
     * @return 最终结果
     */
    public String runOneByOne(String initialPrompt) {
        setNextStepPrompt(initialPrompt);
        setState(AgentState.RUNNING);
        setCurrentStep(0);

        while (getState() == AgentState.RUNNING && getCurrentStep() < getMaxSteps()) {
            incrementStep();
            log.info("[{}] === 第 {} 轮循环 ===", getName(), getCurrentStep());

            // Step 1: 思考
            log.info("[{}] --- 思考阶段 ---", getName());
            boolean needTool = think();

            // Step 2: 行动
            if (needTool) {
                log.info("[{}] --- 执行阶段 ---", getName());
                String toolResult = act();
                log.info("[{}] 工具执行结果: {}", getName(), toolResult);
            } else {
                // 不需要调用工具，获取最终回答
                AssistantMessage lastMessage = (AssistantMessage) getMessageList()
                        .get(getMessageList().size() - 1);
                setState(AgentState.FINISHED);
                return lastMessage.getText();
            }
        }

        if (getCurrentStep() >= getMaxSteps()) {
            log.warn("[{}] 达到最大步骤限制 ({})", getName(), getMaxSteps());
            setState(AgentState.FINISHED);
            return "达到最大步骤限制，任务终止";
        }

        return "任务完成";
    }
}