package com.example.novelagent.tools;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
public class FileOperationTool extends SimpleTool {

    public FileOperationTool() {
        super("fileOperation", "Read or write files from the local filesystem",
                Map.of(
                        "operation", "The operation type: read, write, or list",
                        "filePath", "The file path for read/write operations",
                        "content", "The content to write (for write operation)",
                        "dirPath", "The directory path for list operation"
                ));
    }

    @Override
    public Object call(Map<String, Object> arguments) {
        String operation = (String) arguments.get("operation");
        
        try {
            switch (operation.toLowerCase()) {
                case "read":
                    String filePath = (String) arguments.get("filePath");
                    log.info("读取文件: {}", filePath);
                    Path path = Paths.get(filePath);
                    if (!Files.exists(path)) {
                        return "文件不存在: " + filePath;
                    }
                    return Files.readString(path);
                    
                case "write":
                    String writePath = (String) arguments.get("filePath");
                    String content = (String) arguments.get("content");
                    log.info("写入文件: {}", writePath);
                    Path writeFilePath = Paths.get(writePath);
                    Files.createDirectories(writeFilePath.getParent());
                    Files.writeString(writeFilePath, content);
                    return "文件写入成功: " + writePath;
                    
                case "list":
                    String dirPath = (String) arguments.get("dirPath");
                    log.info("列出目录文件: {}", dirPath);
                    Path dir = Paths.get(dirPath);
                    if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                        return "目录不存在: " + dirPath;
                    }
                    StringBuilder sb = new StringBuilder();
                    Files.list(dir).forEach(p -> sb.append(p.getFileName()).append("\n"));
                    return sb.toString();
                    
                default:
                    return "不支持的操作类型: " + operation;
            }
        } catch (IOException e) {
            log.error("文件操作失败: {}", e.getMessage());
            return "文件操作失败: " + e.getMessage();
        }
    }
}