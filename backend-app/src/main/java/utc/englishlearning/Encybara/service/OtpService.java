package utc.englishlearning.Encybara.service;

import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.domain.request.auth.ReqOtpVerificationDTO;
import utc.englishlearning.Encybara.domain.response.auth.ResCreateUserDTO;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    private final Map<String, ReqOtpVerificationDTO> otpStorage = new ConcurrentHashMap<>();
    private static final long OTP_EXPIRATION_TIME = 2 * 60 * 1000; // 2 phút

    public String generateOtp(String email) {
        return String.valueOf((int) (Math.random() * 900000 + 100000));
    }

    public String saveRegisterData(String email, ResCreateUserDTO registerDTO, String otp, String type) {
        String otpID = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        long tempTimestamp = System.currentTimeMillis();

        ReqOtpVerificationDTO otpData = new ReqOtpVerificationDTO(otpID, otp, email, registerDTO, tempTimestamp, type);
        otpStorage.put(otpID, otpData);
        return otpID;
    }

    public String updateOtp(String otpID) {
        ReqOtpVerificationDTO otpData = otpStorage.get(otpID);
        if (otpData != null) {
            otpData.setTimestamp(System.currentTimeMillis());
            if (System.currentTimeMillis() - otpData.getTimestamp() <= OTP_EXPIRATION_TIME) {
                return otpData.getOtp();
            }
            String newOtp = generateOtp(otpID);
            return newOtp;
        }
        return null;
    }

    public ReqOtpVerificationDTO getOtpData(String otpID) {
        ReqOtpVerificationDTO otpData = otpStorage.get(otpID);
        if (otpData == null) {
            return null;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - otpData.getTimestamp() > OTP_EXPIRATION_TIME) {
            otpStorage.remove(otpID);
            return null;
        }
        return otpData;
    }

    public boolean validateOtp(String otpID, String otp) {
        ReqOtpVerificationDTO otpData = getOtpData(otpID);
        if (otpData == null || !otpData.getOtp().equals(otp)) {
            return false; // otpData rỗng hoặc otp không đúng
        }
        return true;
    }

    public ReqOtpVerificationDTO removeOtpData(String otpID) {
        return otpStorage.remove(otpID);
    }
}