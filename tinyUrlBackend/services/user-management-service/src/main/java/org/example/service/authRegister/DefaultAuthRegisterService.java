package org.example.service.authRegister;

import org.example.service.data.RegisterIData;
import org.example.service.data.RegisterOData;
import org.example.service.data.VerifyEmailRegisterIData;
import org.example.service.data.VerifyEmailRegisterOData;
import org.springframework.stereotype.Service;

@Service
public class DefaultAuthRegisterService implements AuthRegisterService {

    @Override
    public RegisterOData register(RegisterIData input) {

        return null;
    }

    @Override
    public VerifyEmailRegisterOData verifyEmail(VerifyEmailRegisterIData input) {

        return null;
    }
}
