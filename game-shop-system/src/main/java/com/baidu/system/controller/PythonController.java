package com.baidu.system.controller;

import com.baidu.common.ApiResponse;
import com.baidu.utils.PythonUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/py")
public class PythonController {

    private final PythonUtils pythonUtils;

    public PythonController(PythonUtils pythonUtils) {
        this.pythonUtils = pythonUtils;
    }

    @GetMapping("/generateAll")
    public ApiResponse<Map<String, String>> generateAll(@RequestParam(value = "dimension", required = false) String dimension,
                                                        @RequestParam(value = "start", required = false) String start,
                                                        @RequestParam(value = "end", required = false) String end,
                                                        @RequestParam(value = "topN", required = false) Integer topN,
                                                        @RequestParam(value = "pieMetric", required = false) String pieMetric,
                                                        @RequestParam(value = "pname", required = false) String pname) {
        File saveDir = resolveDatedSubDir("ai");

        String pieName = "pie_" + System.currentTimeMillis() + ".png";
        String gridName = "grid_" + System.currentTimeMillis() + ".png";

        String dimArg = (dimension == null || dimension.isBlank()) ? "product" : dimension;
        String startArg = (start == null || start.isBlank()) ? "_" : start;
        String endArg = (end == null || end.isBlank()) ? "_" : end;
        String topArg = (topN == null ? "10" : String.valueOf(topN));
        String pieMetricArg = (pieMetric == null || pieMetric.isBlank()) ? "sales" : pieMetric;

        PythonUtils.ExecResult r1 = pythonUtils.executePython(
                "pie.py", saveDir.getAbsolutePath(), pieName, dimArg, startArg, endArg, topArg, pieMetricArg
        );
        if (r1.exitCode() != 0) {
            return ApiResponse.of(false, -1, "生成饼图失败: " + r1.output(), null);
        }

        PythonUtils.ExecResult r2 = pythonUtils.executePython("grid.py", saveDir.getAbsolutePath(), gridName);
        if (r2.exitCode() != 0) {
            return ApiResponse.of(false, -1, "生成混合图失败: " + r2.output(), null);
        }

        String piePath = toRelative(saveDir, pieName);
        String gridPath = toRelative(saveDir, gridName);
        return ApiResponse.ok(Map.of("piePath", piePath, "gridPath", gridPath));
    }

    @Value("${file.upload.base-dir:uploads}")
    private String uploadBaseDir;

    @GetMapping("/getPie")
    public ApiResponse<Map<String, String>> getPie(@RequestParam(value = "filename", required = false) String filename,
                                                   @RequestParam(value = "dimension", required = false) String dimension,
                                                   @RequestParam(value = "start", required = false) String start,
                                                   @RequestParam(value = "end", required = false) String end,
                                                   @RequestParam(value = "topN", required = false) Integer topN,
                                                   @RequestParam(value = "metric", required = false) String metric) {
        File saveDir = resolveDatedSubDir("ai");
        if (filename == null || filename.isBlank()) {
            filename = "pie_" + System.currentTimeMillis() + ".png";
        } else if (!filename.toLowerCase().endsWith(".png")) {
            filename = filename + ".png";
        }
        String dimArg = (dimension == null || dimension.isBlank()) ? "product" : dimension;
        String startArg = (start == null || start.isBlank()) ? "_" : start;
        String endArg = (end == null || end.isBlank()) ? "_" : end;
        String topArg = (topN == null ? "10" : String.valueOf(topN));
        String metricArg = (metric == null || metric.isBlank()) ? "sales" : metric;

        PythonUtils.ExecResult result = pythonUtils.executePython(
                "pie.py",
                saveDir.getAbsolutePath(),
                filename,
                dimArg,
                startArg,
                endArg,
                topArg,
                metricArg
        );
        if (result.exitCode() != 0) {
            return ApiResponse.of(false, -1, "执行 Python 失败: " + result.output(), null);
        }
        String relativePath = toRelative(saveDir, filename);
        return ApiResponse.ok(Map.of("path", relativePath));
    }

    @GetMapping(value = "/getGrid")
    public ApiResponse<Map<String, String>> getGrid(@RequestParam(value = "pname", required = false) String pname,
                                                    @RequestParam(value = "filename", required = false) String filename) {
        File saveDir = resolveDatedSubDir("ai");
        if (filename == null || filename.isBlank()) {
            filename = "grid_" + System.currentTimeMillis() + ".png";
        } else if (!filename.toLowerCase().endsWith(".png")) {
            filename = filename + ".png";
        }
        PythonUtils.ExecResult result;
        if (pname == null || pname.isBlank()) {
            result = pythonUtils.executePython("grid.py", saveDir.getAbsolutePath(), filename);
        } else {
            result = pythonUtils.executePython("grid.py", saveDir.getAbsolutePath(), filename, pname);
        }
        if (result.exitCode() != 0) {
            return ApiResponse.of(false, -1, "执行 Python 失败: " + result.output(), null);
        }
        String relativePath = toRelative(saveDir, filename);
        return ApiResponse.ok(Map.of("path", relativePath));
    }

    @GetMapping(value = "/showImages", produces = MediaType.IMAGE_PNG_VALUE)
    public void show(@RequestParam("path") String path, HttpServletResponse response) throws IOException {
        if (path == null || path.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (ServletOutputStream out = response.getOutputStream()) {
                out.write("Bad path".getBytes(StandardCharsets.UTF_8));
            }
            return;
        }
        File file = new File(path);
        if (!file.isAbsolute()) {
            file = new File(System.getProperty("user.dir"), path);
        }
        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            try (ServletOutputStream out = response.getOutputStream()) {
                out.write("Not found".getBytes(StandardCharsets.UTF_8));
            }
            return;
        }
        try (InputStream in = new FileInputStream(file); OutputStream out = response.getOutputStream()) {
            StreamUtils.copy(in, out);
            out.flush();
        }
    }

    private File resolveDatedSubDir(String sub) {
        String base = uploadBaseDir;
        File baseDir = new File(base);
        if (!baseDir.isAbsolute()) {
            baseDir = new File(System.getProperty("user.dir"), base);
        }
        String ym = new SimpleDateFormat("yyyy/MM").format(new Date());
        File dir = new File(baseDir, ym + "/" + sub);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private String toRelative(File dir, String filename) {
        File file = new File(dir, filename);
        String projectRoot = System.getProperty("user.dir");
        String abs = file.getAbsolutePath();
        if (abs.startsWith(projectRoot)) {
            return abs.substring(projectRoot.length() + 1).replace('\\', '/');
        }
        return abs;
    }
}
