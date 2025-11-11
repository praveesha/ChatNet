package com.chatdashboard.api;

import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

@RestController
@RequestMapping("/api/files")
public class FileController {
    @GetMapping
    public List<String> listFiles() {
        File folder = new File("filestorage");
        if (!folder.exists()) return List.of();
        return Arrays.stream(folder.listFiles())
                .filter(File::isFile)
                .map(File::getName)
                .collect(Collectors.toList());
    }
}
