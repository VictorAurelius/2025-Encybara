package utc.englishlearning.Encybara.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class FFmeg {

  private static final Logger logger = LoggerFactory.getLogger(FFmeg.class);

  @Value("${ffmpeg.path:}")
  private String ffmpegPath;

  @Value("${ffprobe.path:}")
  private String ffprobePath;

  private String detectFFmpegPath() {
    String[] possiblePaths = {
        ffmpegPath, // From config
        "/usr/bin/ffmpeg", // Linux default
        "/usr/local/bin/ffmpeg", // Linux alternative
        "ffmpeg" // System PATH
    };

    for (String path : possiblePaths) {
      if (path != null && !path.isEmpty()) {
        try {
          if ("ffmpeg".equals(path)) {
            // Try to use system PATH - check if command works
            try {
              ProcessBuilder pb = isWindows() ? 
                new ProcessBuilder("where", "ffmpeg") : 
                new ProcessBuilder("which", "ffmpeg");
              Process process = pb.start();
              if (process.waitFor() == 0) {
                logger.info("Found FFmpeg in system PATH");
                return "ffmpeg";
              }
            } catch (Exception e) {
              // Try direct execution
              try {
                ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-version");
                Process process = pb.start();
                if (process.waitFor() == 0) {
                  logger.info("Found FFmpeg via direct execution");
                  return "ffmpeg";
                }
              } catch (Exception ex) {
                logger.debug("FFmpeg not found in PATH: {}", ex.getMessage());
              }
            }
          } else if (Files.exists(Paths.get(path))) {
            logger.info("Found FFmpeg at: {}", path);
            return path;
          }
        } catch (Exception e) {
          logger.debug("Failed to check FFmpeg path: {} - {}", path, e.getMessage());
        }
      }
    }
    return null;
  }

  private String detectFFprobePath() {
    String[] possiblePaths = {
        ffprobePath, // From config
        "/usr/bin/ffprobe", // Linux default
        "/usr/local/bin/ffprobe", // Linux alternative
        "ffprobe" // System PATH
    };

    for (String path : possiblePaths) {
      if (path != null && !path.isEmpty()) {
        try {
          if ("ffprobe".equals(path)) {
            // Try to use system PATH - check if command works
            try {
              ProcessBuilder pb = isWindows() ? 
                new ProcessBuilder("where", "ffprobe") : 
                new ProcessBuilder("which", "ffprobe");
              Process process = pb.start();
              if (process.waitFor() == 0) {
                logger.info("Found FFprobe in system PATH");
                return "ffprobe";
              }
            } catch (Exception e) {
              // Try direct execution
              try {
                ProcessBuilder pb = new ProcessBuilder("ffprobe", "-version");
                Process process = pb.start();
                if (process.waitFor() == 0) {
                  logger.info("Found FFprobe via direct execution");
                  return "ffprobe";
                }
              } catch (Exception ex) {
                logger.debug("FFprobe not found in PATH: {}", ex.getMessage());
              }
            }
          } else if (Files.exists(Paths.get(path))) {
            logger.info("Found FFprobe at: {}", path);
            return path;
          }
        } catch (Exception e) {
          logger.debug("Failed to check FFprobe path: {} - {}", path, e.getMessage());
        }
      }
    }
    return null;
  }

  private boolean isWindows() {
    return System.getProperty("os.name").toLowerCase().contains("windows");
  }

  @Bean
  public FFmpeg ffmpeg() {
    try {
      String detectedPath = detectFFmpegPath();
      if (detectedPath == null) {
        logger.warn("FFmpeg not found in any known location. FFmpeg functionality will be disabled.");
        return null;
      }
      
      logger.info("Initializing FFmpeg with path: {}", detectedPath);
      return new FFmpeg(detectedPath);
    } catch (IOException e) {
      logger.error("Error initializing FFmpeg: {}", e.getMessage());
      return null;
    }
  }

  @Bean
  public FFprobe ffprobe() {
    try {
      String detectedPath = detectFFprobePath();
      if (detectedPath == null) {
        logger.warn("FFprobe not found in any known location. FFprobe functionality will be disabled.");
        return null;
      }
      
      logger.info("Initializing FFprobe with path: {}", detectedPath);
      return new FFprobe(detectedPath);
    } catch (IOException e) {
      logger.error("Error initializing FFprobe: {}", e.getMessage());
      return null;
    }
  }
}



