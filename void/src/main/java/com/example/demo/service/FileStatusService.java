package com.example.demo.service;

import com.example.demo.model.FileStatusDTO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.info.VideoSize;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FileStatusService {
    private final Map<String, FileStatusDTO> hashFile = new ConcurrentHashMap<String, FileStatusDTO>();

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
            Path inputPath = Paths.get("C:/Users/Athur/Documents/input/" + safeName);

            // Envia o arquivo pro caminho final
            Files.copy(file.getInputStream(), inputPath);
            return inputPath.toFile();

        } catch (Exception e) {
            status.setStatus("ERRO AO GRAVAR!");
            throw new RuntimeException("Erro ao salvar arquivo no disco: " + e.getMessage());
        }
    }

    @Async
    public void convert(File input, File output, String goalFormat, String uuid) {
        FileStatusDTO status = hashFile.get(uuid);
        status.setStatus("PROCESSANDO!");
        status.setFileGoalFormat(goalFormat);

        try {

            VideoAttributes video = new VideoAttributes();
            AudioAttributes audio = new AudioAttributes();

            video.setBitRate(8000000);
            video.setFrameRate(30);
            video.setSize(new VideoSize(1920,1080 ));

            audio.setBitRate(128000);


            switch (goalFormat) {
                case "mp4" -> {
                    video.setCodec("libx264");
                    audio.setCodec("libmp3lame");
                }
                case "webm" -> {
                    video.setCodec("libvpx");
                    audio.setCodec("libvorbis");
                }
                case "avi" -> {
                    video.setCodec("mpeg4");
                    audio.setCodec("libmp3lame");
                }
                case "mov" -> {
                    video.setCodec("libx264");
                    audio.setCodec("aac");
                }
            }
            EncodingAttributes attributes = new EncodingAttributes();
            attributes.setAudioAttributes(audio);
            attributes.setVideoAttributes(video);
            attributes.setOutputFormat(goalFormat);

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

}