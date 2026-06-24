package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.net.MalformedURLException;
import com.example.demo.model.FileStatusDTO;
import com.example.demo.service.FileStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
public class converterController {
    private final FileStatusService fileStatusService;

    public converterController(FileStatusService fileStatusService) {
        this.fileStatusService = fileStatusService;
    }

    @Value("${app.output.path:/app/arquivos-no-docker/convertidos}")
    public String outputPath;

    @PostMapping("/convert")
    public ResponseEntity<String> convert(@RequestParam("file") MultipartFile file,
            @RequestParam("format") String fileFormat, @RequestParam("isExtract") boolean isExtract)
            throws IOException {
        String uuid = fileStatusService.runProcess();
        File inputFile = fileStatusService.record(file, uuid);
        File outputFile = new File(outputPath + uuid + "." + fileFormat);
        fileStatusService.convertAndExtract(inputFile, outputFile, fileFormat, uuid, isExtract);
        return ResponseEntity.ok(uuid);
    }

    @PostMapping("/extract-audio")
    public ResponseEntity<String> extract(@RequestParam("file") MultipartFile file,
            @RequestParam("format") String fileFomat, @RequestParam("isExtract") boolean isExtract) {
        String uuid = fileStatusService.runProcess();
        File inputFile = fileStatusService.record(file, uuid);
        File outputFile = new File(outputPath + uuid + "." + fileFomat);
        fileStatusService.convertAndExtract(inputFile, outputFile, fileFomat, uuid, isExtract);
        return ResponseEntity.ok(uuid);
    }

    @GetMapping("/status/{uuid}")
    @ResponseBody
    public FileStatusDTO statusConversion(@PathVariable String uuid) {
        return fileStatusService.statusCheck(uuid);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") String uuid) {
        try {
            FileStatusDTO dto = fileStatusService.statusCheck(uuid);
            String newFormat = dto.getFileGoalFormat();
            String originalName = dto.getFileOriginalName();

            // remove format of the filename
            if (originalName.contains(".")) {
                originalName = originalName.substring(0, originalName.lastIndexOf("."));
            }

            // filename without original format + final format
            String nomeFinal = originalName + "." + newFormat;

            File file = new File(outputPath + uuid + "." + dto.getFileGoalFormat());

            Resource resource = new UrlResource(file.toURI());

            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + nomeFinal
                                    + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
