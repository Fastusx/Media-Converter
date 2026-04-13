# Media-Converter
A full-stack media converter application built-in Java/Spring Boot dockerized and deployed via Render Cloud\
Live Demo: https://media-converter-gr5h.onrender.com/

> [!CAUTION]
> ## Known limitations
> - **Specific to Render deployment:** Due to the 512Mb RAM limit on the Render free tier, this application is, currently, best suited for small files, such as images and audio content. Large files will trigger an Out Of Memory (OOM) error.

## Current features
- **Multi-format Conversion** - Support for video, audio and image formats (e.g., .mp4, .webm, .mp3, .wav, .jpg, .avif).
- **interactive UI/UX** - Drag n Drop support for intuitive file uploading
- **Asynchronous Processing:** Powered by FFmpeg, allowing the server to handle tasks without blocking the main thread.
- **UUID Tracking:** Every conversion is assigned a unique identifier for status monitoring and secure download.
- **Automated Docker Deployment:** Fully containerized environment ensuring consistent behavior between local and cloud (Render).
- **Live Status Check:** Endpoint dedicated to tracking the conversion progress.
<hr>



## How to run
  ### 1. prerequisites
  Before starting, ensure you have the following installed:
  - Docker (Recommended)
  - Java 21 and Maven (If running without Docker)

  ### 2. Clone Repository 
  ```
  git clone https://github.com/Fastusx/Media-Converter.git
  cd Media-converter 
  ```
  ### 3. Build and run the Docker Container
  ```
  docker build -t media-converter .
  docker run -p 8080:8080 media-converter
  ```

  >[!NOTE]
  >if you want to run the Spring Boot application directly in your machine
  > 1. Ensure FFmpeg is installed and added to your system's PATH.
  >   2. ```
  >      mvn spring-boot:run
  >      ```  
### 4. Access Application in http://localhost:8080



