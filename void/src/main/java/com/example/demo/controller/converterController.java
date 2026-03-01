package com.example.demo.controller;

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
import java.nio.file.Path;

@RestController
@RequestMapping("/application")

public class converterController {
    private final FileStatusService fileStatusService;

    public converterController(FileStatusService fileStatusService) {
        this.fileStatusService = fileStatusService;
    }

    String outputPath = "C:/Users/Athur/Documents/output/";

    @PostMapping("/convert")
    public ResponseEntity<String> convert(@RequestParam("file") MultipartFile file,
            @RequestParam("format") String fileFormat) throws IOException {
        String uuid = fileStatusService.runProcess();
        File inputFile = fileStatusService.record(file, uuid);
        File outputFile = new File(outputPath + uuid + "." + fileFormat);
        fileStatusService.convert(inputFile, outputFile, fileFormat, uuid);
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
            String formatoNovo = dto.getFileGoalFormat();
            String nomeOriginal = dto.getFileOriginalName();

            // remove format of the filename
            if (nomeOriginal.contains(".")) {
                nomeOriginal = nomeOriginal.substring(0, nomeOriginal.lastIndexOf("."));
            }

            // filename without original format + final format
            String nomeFinal = nomeOriginal + "." + formatoNovo;

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
