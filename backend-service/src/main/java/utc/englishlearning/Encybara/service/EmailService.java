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
        message.setFrom("vuq18031@gmail.com");
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
            helper.setFrom("vuq18031@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, isHtml);
            
            System.out.println("🚀 ATTEMPTING TO SEND EMAIL...");
            System.out.println("📧 FROM: vuq18031@gmail.com");
            System.out.println("📧 TO: " + to);
            System.out.println("📧 SUBJECT: " + subject);
            System.out.println("🔐 SMTP HOST: smtp.gmail.com:587");
            System.out.println("🔑 USERNAME: vuq18031@gmail.com");
            System.out.println("🔑 PASSWORD LENGTH: " + (javamailSender != null ? "Connected" : "No Connection"));
            
            javamailSender.send(message);
            System.out.println("✅ EMAIL SENT SUCCESSFULLY!");
            
        } catch (MailException | MessagingException e) {
            System.out.println("❌ ERROR SENDING EMAIL: " + e.getClass().getSimpleName());
            System.out.println("🔍 ERROR MESSAGE: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("🔍 ROOT CAUSE: " + e.getCause().getClass().getSimpleName());
                System.out.println("🔍 CAUSE MESSAGE: " + e.getCause().getMessage());
            }
            e.printStackTrace();
        }
    }
    //Gửi mail dùng file
    public void sendEmailFromTemplateSync(String to, String subject, String otp) {
        try {
            System.out.println("📄 Processing email template...");
            System.out.println("🎯 Target: " + to);
            System.out.println("📝 Subject: " + subject);
            System.out.println("🔢 OTP: " + otp);
            
            // Tạo đối tượng Context và thêm dữ liệu
            Context context = new Context();
            context.setVariable("otp", otp);
            System.out.println("📋 Template context created with OTP variable");

            // Kết xuất nội dung HTML từ template
            System.out.println("🔄 Processing template: otp-email.html");
            String content = templateEngine.process("otp-email.html", context);
            System.out.println("✅ Template processed successfully");
            System.out.println("📄 Generated content length: " + content.length() + " characters");

            // Gửi email
            System.out.println("📧 Calling sendEmailSync...");
            sendEmailSync(to, subject, content, false, true);
            
        } catch (Exception e) {
            System.out.println("💥 ERROR in sendEmailFromTemplateSync: " + e.getClass().getSimpleName());
            System.out.println("🔍 Message: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw để AuthController catch được
        }
    }

}
