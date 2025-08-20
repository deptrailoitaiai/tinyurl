package org.example.service.authRegister;

import org.example.service.data.RegisterIData;
import org.example.service.data.RegisterOData;
import org.example.service.data.VerifyEmailRegisterIData;
import org.example.service.data.VerifyEmailRegisterOData;

public interface AuthRegisterService {
    RegisterOData register(RegisterIData input);

    VerifyEmailRegisterOData verifyEmail(VerifyEmailRegisterIData input);
}
