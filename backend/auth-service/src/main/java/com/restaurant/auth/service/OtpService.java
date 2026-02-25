package com.restaurant.auth.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final JavaMailSender mailSender;
    private final Map<String, OtpData> store = new ConcurrentHashMap<>();

    public OtpService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    @Async
    public void generateAndSend(String email) {
        String otp = String.format("%06d", new Random().nextInt(1000000));
        store.put(email, new OtpData(otp, LocalDateTime.now().plusMinutes(5)));

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Your Login OTP");
        msg.setText("OTP: " + otp + "\nValid for 5 minutes.");
        mailSender.send(msg);
    }

    public boolean verify(String email, String otp) {
        OtpData data = store.get(email);
        if (data == null) return false;
        if (LocalDateTime.now().isAfter(data.expiresAt)) {
            store.remove(email);
            return false;
        }

        if (data.otp.equals(otp)) {
            store.remove(email);
            return true;
        }

        return false;
    }

    @Scheduled(fixedRate = 60000)
    public void cleanup() {
        LocalDateTime now = LocalDateTime.now();
        store.entrySet().removeIf(e -> now.isAfter(e.getValue().expiresAt));
    }

    private record OtpData(String otp, LocalDateTime expiresAt) {}
}
