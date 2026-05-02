package com.example.novelagent.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final ObjectMapper objectMapper;

    @Value("${novel.output.base-path:./output/novel}")
    private String basePath;

    public void initializeProject(String projectId) throws IOException {
        Path projectDir = Paths.get(basePath, projectId);
        Files.createDirectories(projectDir);

        Path bibleDir = projectDir.resolve("bible");
        Path chaptersDir = projectDir.resolve("chapters");
        Path summariesDir = projectDir.resolve("summaries");
        Path auditsDir = projectDir.resolve("audits");
        Path plansDir = projectDir.resolve("plans");
        Path ragDocsDir = projectDir.resolve("rag_docs");

        Files.createDirectories(bibleDir);
        Files.createDirectories(chaptersDir);
        Files.createDirectories(summariesDir);
        Files.createDirectories(auditsDir);
        Files.createDirectories(plansDir);
        Files.createDirectories(ragDocsDir);

        log.info("FileStorageService: 初始化项目目录 - {}", projectId);
    }

    public void saveChapterDraft(String projectId, String chapterId, String content, int version) throws IOException {
        Path filePath = Paths.get(basePath, projectId, "chapters",
                String.format("chapter_%s_draft_v%d.md", chapterId, version));
        Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        log.info("FileStorageService: 保存草稿 - {}", filePath);
    }

    public void saveChapterPolish(String projectId, String chapterId, String content, int version) throws IOException {
        Path filePath = Paths.get(basePath, projectId, "chapters",
                String.format("chapter_%s_polish_v%d.md", chapterId, version));
        Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        log.info("FileStorageService: 保存润色版 - {}", filePath);
    }

    public void saveChapterFinal(String projectId, String chapterId, String content) throws IOException {
        Path filePath = Paths.get(basePath, projectId, "chapters",
                String.format("chapter_%s_final.md", chapterId));
        Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        log.info("FileStorageService: 保存终稿 - {}", filePath);
    }

    public void saveChapterSummary(String projectId, String chapterId, String summary) throws IOException {
        Path filePath = Paths.get(basePath, projectId, "summaries",
                String.format("chapter_%s_summary.md", chapterId));
        Files.writeString(filePath, summary, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void saveAuditResult(String projectId, String chapterId, Object auditResult) throws IOException {
        Path filePath = Paths.get(basePath, projectId, "audits",
                String.format("chapter_%s_audit.json", chapterId));
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(auditResult);
        Files.writeString(filePath, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        log.info("FileStorageService: 保存审计结果 - {}", filePath);
    }

    public void savePlan(String projectId, String chapterId, Object plan) throws IOException {
        Path filePath = Paths.get(basePath, projectId, "plans",
                String.format("chapter_%s_plan.json", chapterId));
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(plan);
        Files.writeString(filePath, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        log.info("FileStorageService: 保存计划 - {}", filePath);
    }

    public void savePolishChanges(String projectId, String chapterId, Object changes) throws IOException {
        Path filePath = Paths.get(basePath, projectId, "chapters",
                String.format("chapter_%s_polish_changes.json", chapterId));
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(changes);
        Files.writeString(filePath, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void saveBibleFile(String projectId, String fileName, Object content) throws IOException {
        Path filePath = Paths.get(basePath, projectId, "bible", fileName);
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(content);
        Files.writeString(filePath, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public String readFile(String projectId, String subDir, String fileName) throws IOException {
        Path filePath = Paths.get(basePath, projectId, subDir, fileName);
        return Files.readString(filePath);
    }

    public Map<String, String> listChapterFiles(String projectId, String chapterId) throws IOException {
        Map<String, String> files = new HashMap<>();
        Path chaptersDir = Paths.get(basePath, projectId, "chapters");

        if (Files.exists(chaptersDir)) {
            Files.walkFileTree(chaptersDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String fileName = file.getFileName().toString();
                    if (fileName.startsWith("chapter_" + chapterId)) {
                        files.put(fileName, file.toString());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        return files;
    }

    public int getNextVersion(String projectId, String chapterId, String type) throws IOException {
        int[] maxVersion = {0};
        Path chaptersDir = Paths.get(basePath, projectId, "chapters");

        if (Files.exists(chaptersDir)) {
            Files.walkFileTree(chaptersDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String fileName = file.getFileName().toString();
                    String prefix = "chapter_" + chapterId + "_" + type + "_v";
                    if (fileName.startsWith(prefix) && fileName.endsWith(".md")) {
                        try {
                            String versionStr = fileName.substring(prefix.length(), fileName.length() - 3);
                            int version = Integer.parseInt(versionStr);
                            maxVersion[0] = Math.max(maxVersion[0], version);
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        return maxVersion[0] + 1;
    }
}
