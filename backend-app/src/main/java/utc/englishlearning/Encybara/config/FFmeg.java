package utc.englishlearning.Encybara.config;

import java.io.IOException;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class FFmeg {

  @Value("${ffmpeg.path}")
  private String ffmpegPath;

  @Value("${ffprobe.path}")
  private String ffprobePath;

  @Bean
  public FFmpeg ffmpeg() {
    try {
      return new FFmpeg(ffmpegPath);
    } catch (IOException e) {
      throw new RuntimeException("Error initializing FFmpeg", e);
    }
  }

  @Bean
  public FFprobe ffprobe() {
    try {
      return new FFprobe(ffprobePath);
    } catch (IOException e) {
      throw new RuntimeException("Error initializing FFprobe", e);
    }
  }
}



