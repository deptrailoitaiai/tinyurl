package org.example.service.authFogetPassword;

import org.example.service.data.ForgetPasswordIData;
import org.example.service.data.ForgetPasswordOData;
import org.example.service.data.VerifyEmailIData;
import org.example.service.data.VerifyEmailOData;
import org.springframework.stereotype.Service;

@Service
public class DefaultAuthForgetPasswordService implements AuthForgetPasswordService {

    @Override
    public VerifyEmailOData verifyEmail(VerifyEmailIData input) {
        // TODO: Implement email verification logic
        return null;
    }

    @Override
    public ForgetPasswordOData generatePasscode(ForgetPasswordIData input) {
        // TODO: Implement passcode generation logic
        return null;
    }

    @Override
    public ForgetPasswordOData updatePassword(ForgetPasswordIData input) {
        // TODO: Implement password update logic
        return null;
    }
}
