package org.example.service.authFogetPassword;

import org.example.service.data.ForgetPasswordIData;
import org.example.service.data.ForgetPasswordOData;
import org.example.service.data.VerifyEmailForgetPassIData;
import org.example.service.data.VerifyEmailForgetPassOData;
import org.springframework.stereotype.Service;

@Service
public class DefaultAuthForgetPasswordService implements AuthForgetPasswordService {

    @Override
    public VerifyEmailForgetPassOData verifyEmail(VerifyEmailForgetPassIData input) {
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
