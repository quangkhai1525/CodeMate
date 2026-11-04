package org.example.service;

import org.example.utils.EmailUtils;

import java.time.LocalDateTime;
import java.util.Random;

public class EmailService {
    private static String OTPNow;
    private static LocalDateTime timeNow;

    public String otpSend(){
        Random random = new Random();
        int randomOTP = random.nextInt(1000000);
        return String.format("%06d", randomOTP);
    }
    public void emailSend(String emailTo){
        String otp = otpSend();
        String subject = "MÃ XÁC THỰC";
        String content = "Mã xác thực của bạn là " + otp
                + "\nVui lòng không chia sẻ mã này với bất kì ai!"
                + "\nLưu ý: Mã xác thực chỉ có hiệu lực trong vòng 3 phút.";
        EmailUtils.sendEmail(emailTo, subject, content);
        saveOTP(otp);
    }
    public void saveOTP(String otp){
        OTPNow = otp;
        timeNow = LocalDateTime.now().plusMinutes(3);
    }
    public boolean checkOTP(String inputOTP){
        if(OTPNow == null || timeNow == null){
            return false;
        }
        if(LocalDateTime.now().isAfter(timeNow)){
            OTPNow = null;
            return false;
        }
        if(OTPNow.equals(inputOTP)){
            OTPNow = null;
            return true;
        }
        return false;
    }
}
