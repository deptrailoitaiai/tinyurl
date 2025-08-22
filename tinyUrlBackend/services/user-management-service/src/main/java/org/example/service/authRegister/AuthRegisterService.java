package org.example.service.authRegister;

import org.example.service.data.*;

public interface AuthRegisterService {
    RegisterOData register(RegisterIData input);

    VerifyEmailRegisterOData verifyEmail(VerifyEmailRegisterIData input);

    SendEmailToVerifyRegisterOData sendEmailToVerifyRegister(SendEmailToVerifyRegisterIData input);
}
