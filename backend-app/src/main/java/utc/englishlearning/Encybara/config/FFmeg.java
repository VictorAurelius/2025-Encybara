package utc.englishlearning.Encybara.config;

import java.io.IOException;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FFmeg {

  @Bean
  public FFmpeg ffmpeg() {
    try {
      FFmpeg fFmpeg =  new FFmpeg("F:\\ffmpeg-2025-03-31-git-35c091f4b7-essentials_build\\ffmpeg-2025-03-31-git-35c091f4b7-essentials_build\\bin\\ffmpeg.exe");
      return fFmpeg;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Bean
  public FFprobe ffprobe() {
    try {
      FFprobe ffprobe =  new FFprobe("F:\\ffmpeg-2025-03-31-git-35c091f4b7-essentials_build\\ffmpeg-2025-03-31-git-35c091f4b7-essentials_build\\bin\\ffprobe.exe");
      return ffprobe;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }




}
