package com.example.demo.service;

import com.example.demo.model.FileStatusDTO;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.info.VideoSize;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FileStatusService {
    private final Map<String, FileStatusDTO> hashFile = new ConcurrentHashMap<String, FileStatusDTO>();

    private final List<String> audioFormats = List.of("mp3", "wav", "ogg", "flac");

    private final Map<String, String> AUDIO_CODECS = Map.of(
            "mp3", "libmp3lame",
            "wav", "pcm_s16le",
            "ogg", "libvorbis",
            "flac", "flac",
            "mp4", "libmp3lame",
            "webm", "libvorbis",
            "avi", "libmp3lame",
            "mov", "aac",
            "mkv", "aac",
            "wmv", "wmav2"


    );

    private final Map<String, String> VIDEO_CODECS = Map.of(
            "mp4", "libx264",
            "webm", "libvpx",
            "avi", "mpeg4",
            "mov", "libx264",
            "mkv", "libx264",
            "wmv", "msmpeg4v2",
            "webp", "libwebp"
    );

    private final List<String> imageFormats = List.of("png", "jpg", "gif");
    private final Map<String, String> FORMAT_ALIAS = Map.of(
            "mkv", "matroska",
            "wmv", "asf"
    );

    @Value("${app.input.path}")
    public String inputDir;

    @Value("${app.output.path}")
    public String outputDir;

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

    public EncodingAttributes setEncodingAttributes(File file, String goalFormat) {
        VideoAttributes videoAttributes;
        AudioAttributes audioAttributes;
        EncodingAttributes attributes = new EncodingAttributes();

        String fileName = file.getName();
        String fileFormat = fileName.substring(fileName.lastIndexOf(".") + 1);

        if (FORMAT_ALIAS.containsKey(goalFormat)) goalFormat = FORMAT_ALIAS.get(goalFormat);

        if (VIDEO_CODECS.containsKey(fileFormat)) {

            videoAttributes = setVideoAttributes(file, goalFormat);
            audioAttributes = setAudioAttributes(file, goalFormat);

            attributes.setVideoAttributes(videoAttributes);
            attributes.setAudioAttributes(audioAttributes);

        } else {
            if (audioFormats.contains(fileFormat)) {
                audioAttributes = setAudioAttributes(file, goalFormat);
                attributes.setAudioAttributes(audioAttributes);
            }
            if (imageFormats.contains(fileFormat)){
                videoAttributes = setVideoAttributes(file,goalFormat);
                attributes.setVideoAttributes(videoAttributes);
            }
        }

        attributes.setOutputFormat(goalFormat);
        return attributes;
    }

    public VideoAttributes setVideoAttributes(File file, String goalFormat) {
        VideoAttributes videoAttributes = new VideoAttributes();
        String fileName = file.getName();
        String fileFormat = fileName.substring(fileName.lastIndexOf(".") + 1);
        String codecVideo = "";

        if (!VIDEO_CODECS.containsKey(fileFormat) && !imageFormats.contains(fileFormat))
            return videoAttributes;
        else {
            codecVideo = VIDEO_CODECS.get(goalFormat);
            videoAttributes.setCodec(codecVideo);
            videoAttributes.setBitRate(8000000);
            videoAttributes.setFrameRate(30);
            if (!goalFormat.equalsIgnoreCase("webp")){
            videoAttributes.setSize(new VideoSize(1920, 1080));
        }
        }

        return videoAttributes;
    }

    public AudioAttributes setAudioAttributes(File file, String goalFormat) {
        AudioAttributes audioAttributes = new AudioAttributes();

        String fileName = file.getName();
        String fileFormat = fileName.substring(fileName.lastIndexOf(".") + 1);

        String codec = "";

        if (AUDIO_CODECS.get(fileFormat) != null)
            codec = AUDIO_CODECS.get(goalFormat);

        audioAttributes.setCodec(codec);
        audioAttributes.setBitRate(128000);

        return audioAttributes;
    }

    @Async
    public void convert(File input, File output, String goalFormat, String uuid) {
        ImageIO.scanForPlugins();
        FileStatusDTO status = hashFile.get(uuid);
        status.setStatus("PROCESSANDO!");
        status.setFileGoalFormat(goalFormat);
        BufferedImage image;

        try {
            if (imageFormats.contains(goalFormat)) {
                image = ImageIO.read(input);
                if (goalFormat.equals("jpg") || goalFormat.equals("jpeg")){
                    BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(),BufferedImage.TYPE_INT_RGB);
                    rgbImage.createGraphics().drawImage(image,0,0, Color.WHITE, null);
                    image = rgbImage;

                }
                ImageIO.write(image,goalFormat,output);
                status.setDownloadUrl("/application/download/" + uuid);
                status.setStatus("FINALIZADO!");
                input.delete();
                return;

            }

            EncodingAttributes attributes = setEncodingAttributes(input, goalFormat);

            MultimediaObject multimediaObjectInput = new MultimediaObject(input);

            Encoder encoder = new Encoder();
            encoder.encode(multimediaObjectInput, output, attributes);

            status.setStatus("FINALIZADO!");
            status.setDownloadUrl("/application/download/" + uuid);

            System.out.println("Sucesso! Arquivo salvo em " + output.getAbsolutePath());
            System.out.println(status.getStatus());

            input.delete();
        } catch (Exception e) {
            System.err.println("Erro na conversão: " + e.getMessage());
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