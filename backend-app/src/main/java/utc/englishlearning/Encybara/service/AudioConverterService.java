package utc.englishlearning.Encybara.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class AudioConverterService {

    @Autowired
    private FFmpeg ffmpeg;
    @Autowired
    private FFprobe ffprobe;

    @Value("${file.upload-dir}") // Cấu hình thư mục lưu file trong `application.properties`
    private String uploadDir;

    public AudioConverterService()  {

    }

    public Mono<MultipartFile> convertM4aToWav(MultipartFile file) {
        return Mono.fromCallable(() -> {
            try {
                // 1. Tạo thư mục nếu chưa có
                File uploadFolder = new File(uploadDir);
                if (!uploadFolder.exists()) {
                    uploadFolder.mkdirs();
                }

                // 2. Kiểm tra tên file đầu vào
                String inputFileName = file.getOriginalFilename();
                if (inputFileName == null || !inputFileName.toLowerCase().endsWith(".m4a")) {
                    throw new RuntimeException("File không hợp lệ, chỉ hỗ trợ .m4a");
                }

                // 3. Tạo đường dẫn file input/output
                String inputPath = Paths.get(uploadDir, inputFileName).toString();
                String outputFileName = inputFileName.replace(".m4a", ".wav");
                String outputPath = Paths.get(uploadDir, outputFileName).toString();

                // 4. Lưu file .m4a vào hệ thống
                File inputFile = new File(inputPath);
                file.transferTo(inputFile);

                FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(inputPath)
                    .overrideOutputFiles(true)
                    .addOutput(outputPath)
                    .setFormat("wav")
                    .setAudioCodec("pcm_s16le")
                    .setAudioSampleRate(16000)
                    .setAudioChannels(1)
                    .done();


                FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
                executor.createJob(builder).run();

                // 6. Đọc file WAV và tạo MultipartFile
                File wavFile = new File(outputPath);
                if (!wavFile.exists()) {
                    throw new RuntimeException("Không tìm thấy file WAV sau khi convert");
                }

                byte[] wavBytes = Files.readAllBytes(wavFile.toPath());

                // 6. Tạo MultipartFile từ byte array
                MultipartFile result = new MultipartFile() {
                    @Override
                    public String getName() {
                        return "file";
                    }

                    @Override
                    public String getOriginalFilename() {
                        return outputFileName;
                    }

                    @Override
                    public String getContentType() {
                        return "audio/wav";
                    }

                    @Override
                    public boolean isEmpty() {
                        return wavBytes.length == 0;
                    }

                    @Override
                    public long getSize() {
                        return wavBytes.length;
                    }

                    @Override
                    public byte[] getBytes() throws IOException {
                        return wavBytes;
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return new ByteArrayInputStream(wavBytes);
                    }

                    @Override
                    public void transferTo(File dest) throws IOException, IllegalStateException {
                        Files.write(dest.toPath(), wavBytes);
                    }
                };

                // 7. Xoá file tạm
                inputFile.delete();
                wavFile.delete();

                return result;

            } catch (Exception e) {
                e.printStackTrace(); // In log ra để debug
                throw new RuntimeException("Lỗi xử lý file: " + e.getMessage(), e);
            }
        });
    }



}
