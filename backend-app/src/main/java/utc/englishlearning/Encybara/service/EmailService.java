package utc.englishlearning.Encybara.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


@Service
public class EmailService {
    private final JavaMailSender javamailSender;
    private final MailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailService(JavaMailSender emailSender, MailSender mailSender, SpringTemplateEngine templateEngine) {
        this.javamailSender = emailSender;
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }
    //Gửi mail text
    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("englishlearning");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        this.mailSender.send(message);
        System.out.println("Sending email to " + to + " with body: " + text);
    }

    //Gửi mail có thế custom bằng html ở content
    public void sendEmailSync(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        MimeMessage message = javamailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, isMultipart, "UTF-8");
            helper.setFrom("englishlearning");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, isHtml);
            javamailSender.send(message);
        } catch (MailException | MessagingException e) {
            System.out.println("ERROR SENDING EMAIL: " + e.getMessage());
        }
    }
    //Gửi mail dùng file
    public void sendEmailFromTemplateSync(String to, String subject, String otp) {
        // Tạo đối tượng Context và thêm dữ liệu
        Context context = new Context();
        context.setVariable("otp", otp);

        // Kết xuất nội dung HTML từ template
        String content = templateEngine.process("otp-email.html", context);

        // Gửi email
        sendEmailSync(to, subject, content, false, true);
    }

}
