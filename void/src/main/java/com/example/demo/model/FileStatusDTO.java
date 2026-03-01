package com.example.demo.model;

import lombok.Data;

@Data
public class FileStatusDTO {
    private String UUID;
    private String fileOriginalName;
    private String fileGoalFormat;
    private String status;
    private String downloadUrl;

    public FileStatusDTO() {
    }
    public FileStatusDTO(String status){
        this.status = status;

    }
}
