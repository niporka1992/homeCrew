package ru.homecrew.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.homecrew.service.file.FileService;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_OWNER')")
public class FileController {

    private final FileService fileService;

    /**
     *  Отдаёт файл (проксируется из Telegram или другого источника)
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> getFile(@PathVariable(name = "fileId") String fileId) {
        return fileService.getFileById(fileId);
    }
}
