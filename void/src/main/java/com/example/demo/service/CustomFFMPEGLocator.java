package com.example.demo.service;

import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;

public class CustomFFMPEGLocator extends DefaultFFMPEGLocator {
    private final String path;

    public CustomFFMPEGLocator(String path) {
        this.path = path;
    }

@Override
    public String getExecutablePath(){
        return this.path;
}
}