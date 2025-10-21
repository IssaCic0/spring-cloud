package com.baidu.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class PythonUtils {
    private static final Logger log = LoggerFactory.getLogger(PythonUtils.class);

    @Value("${python.exec:python}")
    private String pythonExec;

    @Value("${python.scripts.path}")
    private String scriptsPath;

    public ExecResult executePython(String scriptName, String... args) {
        // 将脚本目录解析为绝对路径
        File scriptsDir = new File(scriptsPath);
        if (!scriptsDir.isAbsolute()) {
            scriptsDir = new File(System.getProperty("user.dir"), scriptsPath);
        }
        if (!scriptsDir.exists() || !scriptsDir.isDirectory()) {
            // 打包/构建运行环境的回退目录
            File fallbackDir = new File(System.getProperty("user.dir"), "target/classes/python");
            if (fallbackDir.exists() && fallbackDir.isDirectory()) {
                scriptsDir = fallbackDir;
            } else {
                return new ExecResult(-1, "脚本目录不存在: " + scriptsDir.getAbsolutePath());
            }
        }

        List<String> cmd = new ArrayList<>();
        cmd.add(pythonExec);
        File scriptFile = new File(scriptsDir, scriptName);
        if (!scriptFile.exists()) {
            return new ExecResult(-1, "脚本文件不存在: " + scriptFile.getAbsolutePath());
        }
        String scriptFullPath = scriptFile.getAbsolutePath();
        cmd.add(scriptFullPath);
        if (args != null) {
            for (String a : args) cmd.add(a);
        }

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        pb.directory(scriptsDir);
        log.info("[Python工具] 可执行={}, 工作目录={}, 命令={}", pythonExec, scriptsDir.getAbsolutePath(), cmd);
        try {
            Process p = pb.start();
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line).append('\n');
            }
            int code = p.waitFor();
            return new ExecResult(code, sb.toString());
        } catch (IOException | InterruptedException e) {
            return new ExecResult(-1, e.getMessage());
        }
    }

    public record ExecResult(int exitCode, String output) {}
}

