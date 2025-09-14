package org.example.service.EmailVerification;

import org.example.service.data.SendEmailToVerifyRegisterIData;
import org.example.service.data.SendEmailToVerifyRegisterOData;
import org.example.service.data.VerifyEmailForgetPassIData;
import org.example.service.data.VerifyEmailForgetPassOData;

public interface EmailVerificationService {
    
    /**
     * Send verification email for user registration
     */
    SendEmailToVerifyRegisterOData sendVerificationEmail(SendEmailToVerifyRegisterIData input);
    
    /**
     * Send reset password email
     */
    VerifyEmailForgetPassOData sendResetPasswordEmail(VerifyEmailForgetPassIData input);
}
