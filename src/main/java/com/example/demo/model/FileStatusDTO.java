package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileStatusDTO {
    private String uuid;
    private String fileOriginalName;
    private String fileGoalFormat;
    private String status;
    private String downloadUrl;

    public FileStatusDTO(String status) {
        this.status = status;
    }

}