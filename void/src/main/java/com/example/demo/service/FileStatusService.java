package com.example.demo.service;

import com.example.demo.model.FileStatusDTO;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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

    /*
     * private final List<String> audioFormats = List.of("mp3", "wav", "ogg",
     * "flac");
     * 
     * private final Map<String, String> AUDIO_CODECS = Map.of(
     * "mp3", "libmp3lame",
     * "wav", "pcm_s16le",
     * "ogg", "libvorbis",
     * "flac", "flac",
     * "mp4", "libmp3lame",
     * "webm", "libvorbis",
     * "avi", "libmp3lame",
     * "mov", "aac",
     * "mkv", "aac",
     * "wmv", "wmav2"
     * 
     * );
     * 
     * private final Map<String, String> VIDEO_CODECS = Map.of(
     * "mp4", "libx264",
     * "webm", "libvpx",
     * "avi", "mpeg4",
     * "mov", "libx264",
     * "mkv", "libx264",
     * "wmv", "msmpeg4v2",
     * "webp", "libwebp",
     * "avif", "libaom-av1");
     * 
     * private final List<String> imageFormats = List.of("png", "jpg", "gif");
     * private final Map<String, String> FORMAT_ALIAS = Map.of(
     * "mkv", "matroska",
     * "wmv", "asf");
     */

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

    /*
     * @Async
     * public void convert(File input, File output, String goalFormat, String uuid)
     * {
     * 
     * ImageIO.scanForPlugins();
     * FileStatusDTO status = hashFile.get(uuid);
     * status.setStatus("PROCESSANDO!");
     * status.setFileGoalFormat(goalFormat);
     * String inputName = input.getName();
     * String inputFormat = inputName.substring(inputName.lastIndexOf(".") + 1);
     * BufferedImage image;
     * 
     * try {
     * if (imageFormats.contains(goalFormat) &&
     * !inputFormat.equalsIgnoreCase("avif") && !imageFormats.contains("avif")){
     * image = ImageIO.read(input);
     * System.out.println(image);
     * if (goalFormat.equals("jpg") || goalFormat.equals("jpeg")) {
     * BufferedImage rgbImage = new BufferedImage(image.getWidth(),
     * image.getHeight(),
     * BufferedImage.TYPE_INT_RGB);
     * rgbImage.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);
     * image = rgbImage;
     * 
     * }
     * 
     * ImageIO.write(image, goalFormat, output);
     * status.setDownloadUrl("/application/download/" + uuid);
     * status.setStatus("FINALIZADO!");
     * input.delete();
     * return;
     * 
     * }
     * 
     * EncodingAttributes attributes = setEncodingAttributes(input, goalFormat);
     * 
     * MultimediaObject multimediaObjectInput = new MultimediaObject(input);
     * 
     * CustomFFMPEGLocator ffmpegFull = new CustomFFMPEGLocator(ffmpegPath);
     * 
     * Encoder encoder = new Encoder(ffmpegFull);
     * System.out.println("Formatos de vídeo suportados pelo binário: " +
     * Arrays.toString(encoder.getVideoEncoders()));
     * 
     * encoder.encode(multimediaObjectInput, output, attributes);
     * 
     * status.setStatus("FINALIZADO!");
     * status.setDownloadUrl("/application/download/" + uuid);
     * 
     * System.out.println("Sucesso! Arquivo salvo em " + output.getAbsolutePath());
     * System.out.println(status.getStatus());
     * 
     * input.delete();
     * } catch (Exception e) {
     * System.err.println("Erro na conversão: " + e.getMessage());
     * }
     * 
     * }
     */
    @Async
    public void convert(File input, File output, String goalFormat, String uuid) {
        FileStatusDTO status = hashFile.get(uuid);
        status.setStatus("PROCESSANDO!");
        status.setFileGoalFormat(goalFormat);

        try {
            // terminal command line to Ffmpeg conversion
            List<String> command = new ArrayList<>();

            command.add(ffmpegPath);
            command.add("-y"); // positive answer for everything
            command.add("-i"); //
            command.add(input.getAbsolutePath());
            command.add("-pix_fmt"); // pixel format setted
            command.add("yuv420p"); // codec

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
                status.setDownloadUrl("/application/download/" + uuid);
                status.setStatus("FINALIZADO!");
                System.out.println("Conversão concluída com sucesso!");
            } else {
                status.setStatus("ERRO NA CONVERSÃO!");
                System.err.println("O FFmpeg retornou erro. Código: " + exitCode);
            }

            if (input.exists()) {
                input.delete();
            }

        } catch (Exception e) {
            status.setStatus("ERRO CRÍTICO!");
            System.err.println("Falha ao tentar rodar o FFmpeg: " + e.getMessage());
            e.printStackTrace();
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