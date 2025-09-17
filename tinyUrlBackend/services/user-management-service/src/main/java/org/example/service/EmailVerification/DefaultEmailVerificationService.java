package org.example.service.EmailVerification;

import org.example.constants.ErrorCode;
import org.example.service.data.SendEmailToVerifyRegisterIData;
import org.example.service.data.SendEmailToVerifyRegisterOData;
import org.example.service.data.VerifyEmailForgetPassIData;
import org.example.service.data.VerifyEmailForgetPassOData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class DefaultEmailVerificationService implements EmailVerificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${cache.token.verification.ttl}")
    private long verificationTokenTtl;

    @Value("${cache.token.reset-password.ttl}")
    private long resetPasswordTokenTtl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String VERIFICATION_TOKEN_PREFIX = "verification:";
    private static final String RESET_PASSWORD_TOKEN_PREFIX = "reset-password:";
    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public SendEmailToVerifyRegisterOData sendVerificationEmail(SendEmailToVerifyRegisterIData input) {
        SendEmailToVerifyRegisterOData result = new SendEmailToVerifyRegisterOData();
        
        try {
            String email = input.getEmail();
            String userId = input.getUserId();
            
            // Generate verification token
            String token = generateSecureToken();
            
            // Store token in Redis with TTL
            String redisKey = VERIFICATION_TOKEN_PREFIX + userId;
            redisTemplate.opsForValue().set(redisKey, token, verificationTokenTtl, TimeUnit.SECONDS);
            
            // Send email
            sendVerificationEmailTemplate(email, token, userId);
            
            result.setErrCode(ErrorCode.SUCCESS);
            result.setToken(token);
            
        } catch (Exception e) {
            result.setErrCode(ErrorCode.INTERNAL_SERVER_ERROR);
            result.setMessage("Failed to send verification email: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public VerifyEmailForgetPassOData sendResetPasswordEmail(VerifyEmailForgetPassIData input) {
        VerifyEmailForgetPassOData result = new VerifyEmailForgetPassOData();
        
        try {
            String email = input.getEmail();
            Long userId = input.getUserId();
            
            // Generate reset password token
            String token = generateSecureToken();
            
            // Store token in Redis with TTL
            String redisKey = RESET_PASSWORD_TOKEN_PREFIX + userId;
            redisTemplate.opsForValue().set(redisKey, token, resetPasswordTokenTtl, TimeUnit.SECONDS);
            
            // Send email
            sendResetPasswordEmailTemplate(email, token, userId.toString());
            
            result.setErrCode(ErrorCode.SUCCESS);
            result.setToken(token);
            
        } catch (Exception e) {
            result.setErrCode(ErrorCode.INTERNAL_SERVER_ERROR);
            result.setMessage("Failed to send reset password email: " + e.getMessage());
        }
        
        return result;
    }

    private void sendVerificationEmailTemplate(String toEmail, String token, String userId) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("TinyURL - Email Verification");

        String htmlContent = buildVerificationEmailHtml(token, userId);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private void sendResetPasswordEmailTemplate(String toEmail, String token, String userId) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("TinyURL - Reset Password");

        String htmlContent = buildResetPasswordEmailHtml(token, userId);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private String buildVerificationEmailHtml(String token, String userId) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Email Verification</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .token { background-color: #e7f3ff; padding: 15px; border-left: 4px solid #2196F3; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; }
                    .button { display: inline-block; background-color: #4CAF50; color: white; padding: 12px 24px; text-decoration: none; border-radius: 4px; margin: 10px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to TinyURL!</h1>
                    </div>
                    <div class="content">
                        <h2>Email Verification Required</h2>
                        <p>Thank you for registering with TinyURL. To complete your registration, please verify your email address.</p>
                        
                        <div class="token">
                            <strong>Your verification token:</strong><br>
                            <code style="font-size: 16px; font-weight: bold;">%s</code>
                        </div>
                        
                        <p>Please use this token to verify your email address. This token will expire in 30 minutes.</p>
                        
                        <p>If you didn't create an account with TinyURL, please ignore this email.</p>
                    </div>
                    <div class="footer">
                        <p>Best regards,<br>TinyURL Team</p>
                    </div>
                </div>
            </body>
            </html>
            """, token);
    }

    private String buildResetPasswordEmailHtml(String token, String userId) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Reset Password</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #FF6B6B; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .token { background-color: #ffe7e7; padding: 15px; border-left: 4px solid #FF6B6B; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; }
                    .warning { background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 10px; border-radius: 4px; margin: 10px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Reset Your Password</h1>
                    </div>
                    <div class="content">
                        <h2>Password Reset Request</h2>
                        <p>We received a request to reset your password for your TinyURL account.</p>
                        
                        <div class="token">
                            <strong>Your reset token:</strong><br>
                            <code style="font-size: 16px; font-weight: bold;">%s</code>
                        </div>
                        
                        <p>Please use this token to reset your password. This token will expire in 15 minutes for security reasons.</p>
                        
                        <div class="warning">
                            <strong>Security Notice:</strong> If you didn't request a password reset, please ignore this email and your password will remain unchanged.
                        </div>
                    </div>
                    <div class="footer">
                        <p>Best regards,<br>TinyURL Team</p>
                    </div>
                </div>
            </body>
            </html>
            """, token);
    }

    private String generateSecureToken() {
        // Generate a 6-digit secure random token
        int token = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(token);
    }

    /**
     * Verify token for email verification
     */
    public boolean verifyEmailToken(String userId, String providedToken) {
        String redisKey = VERIFICATION_TOKEN_PREFIX + userId;
        String storedToken = redisTemplate.opsForValue().get(redisKey);
        
        if (storedToken != null && storedToken.equals(providedToken)) {
            redisTemplate.delete(redisKey); // Remove token after successful verification
            return true;
        }
        return false;
    }

    /**
     * Verify token for password reset
     */
    public boolean verifyResetPasswordToken(String userId, String providedToken) {
        String redisKey = RESET_PASSWORD_TOKEN_PREFIX + userId;
        String storedToken = redisTemplate.opsForValue().get(redisKey);
        
        if (storedToken != null && storedToken.equals(providedToken)) {
            redisTemplate.delete(redisKey); // Remove token after successful verification
            return true;
        }
        return false;
    }
}