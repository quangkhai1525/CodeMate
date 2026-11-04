package org.example.utils;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Date;
import java.util.Properties;

public class EmailUtils {
    public static void sendEmail(String emailTo,String subject, String OTP) {
        String emailFrom = System.getenv("CODEMATE_MAIL_USERNAME");
        String password = System.getenv("CODEMATE_MAIL_PASSWORD");
        if (emailFrom == null || emailFrom.isBlank() || password == null || password.isBlank()) {
            throw new IllegalStateException("Gửi OTP cần CODEMATE_MAIL_USERNAME và CODEMATE_MAIL_PASSWORD trong biến môi trường.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailFrom, password);
            }
        };

        Session session = Session.getInstance(props, authenticator);

        try{
            MimeMessage message = new MimeMessage(session);

            message.setFrom(emailFrom);
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTo, false));
            message.setSubject(subject, "UTF-8");
            message.setSentDate(new Date());
            message.setText(OTP, "UTF-8");

            Transport.send(message);

        } catch (MessagingException e) {
            throw new IllegalStateException("Không gửi được email OTP. Kiểm tra cấu hình email và kết nối mạng.", e);
        }
    }
}
