package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.net.MalformedURLException;
import com.example.demo.model.FileStatusDTO;
import com.example.demo.model.User;
import com.example.demo.service.FileStatusService;
import com.example.demo.service.RateLimitingService;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
public class converterController {
    private final FileStatusService fileStatusService;

    private final RateLimitingService rateLimitingService;

    public converterController(FileStatusService fileStatusService, RateLimitingService rateLimitingService) {
        this.fileStatusService = fileStatusService;
        this.rateLimitingService = rateLimitingService;
    }

    @Value("${app.output.path:/app/arquivos-no-docker/convertidos}")
    public String outputPath;

    @PostMapping("/convert")
    public ResponseEntity<String> convert(@RequestParam("file") MultipartFile file,
            @RequestParam("format") String fileFormat, @RequestParam("isExtract") boolean isExtract,
            Authentication authentication, HttpServletRequest request)
            throws IOException {
        String identifier;
        String role;

        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof User userPrincipal) {
                identifier = userPrincipal.getEmail();
            } else {
                identifier = authentication.getName();
            }
            role = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst().orElse("USER");

        } else {
            identifier = request.getRemoteAddr();
            role = "ROLE_ANONYMOUS";
        }

        Bucket bucket = rateLimitingService.resolveBucket(identifier, role);

        if (bucket.tryConsume(1)) {
            String uuid = fileStatusService.runProcess();
            File inputFile = fileStatusService.record(file, uuid);
            File outputFile = new File(outputPath + uuid + "." + fileFormat);
            fileStatusService.convertAndExtract(inputFile, outputFile, fileFormat, uuid, isExtract);
            return ResponseEntity.ok(uuid);
        } else {
            if (role.equals("ROLE_ANONYMOUS")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body("Limite diário para visitantes atingido. Crie uma conta grátis para ganhar mais 3 conversões!");
            } else {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body("Limite diário para o plano gratuito atingido. Seja Premium para acesso ilimitado!");
            }
        }
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
