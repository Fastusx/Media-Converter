package com.example.demo.service;

import com.example.demo.model.FileStatusDTO;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.transform.Source;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FileStatusService {
    private final Map<String, FileStatusDTO> hashFile = new ConcurrentHashMap<String, FileStatusDTO>();


    @Value("${app.input.path:/app/arquivos-no-docker/uploads}")
    public String inputDir;

    @Value("${app.output.path:/app/arquivos-no-docker/convertidos}")
    public String outputDir;

    @Value("${app.ffmpeg.path:ffmpeg}")
    public String ffmpegPath;

    public FileStatusService() {
    }

    @PostConstruct
    public void setupFolders() {
        File input = new File(inputDir);
        File output = new File(outputDir);
        if (!input.exists()) {
            boolean isCreated = input.mkdirs();
            System.out.println("Pasta de Input criada? " + isCreated);
        }
        if (!output.exists()) {
            boolean isCreated = output.mkdirs();
            System.out.println("Pasta de Output criada? " + isCreated);
        }
    }

    public String runProcess() {
        FileStatusDTO fileStatusDTO = new FileStatusDTO();
        fileStatusDTO.setStatus("INICIANDO!");
        String uuid = UUID.randomUUID().toString();
        fileStatusDTO.setUUID(uuid);
        hashFile.put(uuid, fileStatusDTO);
        return uuid;
    }

    public File record(MultipartFile file, String uuid) {
        FileStatusDTO status = hashFile.get(uuid);
        status.setStatus("GRAVANDO!");
        String originalName = file.getOriginalFilename();
        status.setFileOriginalName(originalName);

        try {
            String safeName = uuid + "_" + originalName;
            // define o caminho final
            Path inputPath = Paths.get(inputDir, safeName);

            // Envia o arquivo pro caminho final
            Files.copy(file.getInputStream(), inputPath);
            return inputPath.toFile();

        } catch (Exception e) {
            status.setStatus("ERRO AO GRAVAR!");
            throw new RuntimeException("Erro ao salvar arquivo no disco: " + e.getMessage());
        }
    }


    @Async
    public void convertAndExtract(File input, File output, String goalFormat, String uuid, boolean isExtract) {
        FileStatusDTO status = hashFile.get(uuid);
        status.setStatus("PROCESSANDO!");
        status.setFileGoalFormat(goalFormat);

        try {
            // terminal command line to Ffmpeg conversion
            List<String> command = new ArrayList<>();
            command.add(ffmpegPath);
            command.add("-i"); //
            command.add(input.getAbsolutePath());
            command.add("-y"); // 

            if (!isExtract) {
                command.add("-pix_fmt"); // pixel format setted
                command.add("yuv420p"); // codec
            }else {
                command.add("-vn");
                command.add("-acodec");
                switch (goalFormat) {
                    case "mp3" -> command.add("libmp3lame");
                    case "ogg" -> command.add("libvorbis");
                    case "flac" -> command.add("flac");
                    case "wav" -> command.add("pcm_s16le");
                    default ->
                            command.add("copy");
                }
            }
            command.add(output.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("FFmpeg Log: " + line);
                }
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                status.setDownloadUrl("/download/" + uuid);
                status.setStatus("FINALIZADO!");
                String message = isExtract ? "Extração concluída com sucesso!" : "conversão conclúida com sucesso!";
                System.out.println(message);
            } else {
                String message = isExtract ? "Erro na Extração" : "Erro na Conversão";
                status.setStatus(message);
                System.err.println("O FFmpeg retornou erro. Código: " + exitCode);
            }



        } catch (Exception e) {
            status.setStatus("ERRO CRÍTICO!");
            System.err.println("Falha ao tentar rodar o FFmpeg: " + e.getMessage());
            e.printStackTrace();
        }
        finally {
            if (input.exists()) {
                input.delete();
            }
        }
    }

    public FileStatusDTO statusCheck(String uuid) {
        return hashFile.getOrDefault(uuid, new FileStatusDTO("Arquivo não encontrado"));
    }

    @Scheduled(fixedRate = 1800000) // thirty minutes
    public void cleanUp() {
        String outputPath = outputDir;
        File outputDir = new File(outputPath);
        long currentTime = System.currentTimeMillis();
        File[] files = outputDir.listFiles();

        for (File file : files) {
            // if the file's modification date be greater than 2 minutes
            if (currentTime - file.lastModified() > 3600000) { // 1 hora
                file.delete();
                System.out.println("arquivo excluido! nome " + file.getName());
            }
        }

    }

}