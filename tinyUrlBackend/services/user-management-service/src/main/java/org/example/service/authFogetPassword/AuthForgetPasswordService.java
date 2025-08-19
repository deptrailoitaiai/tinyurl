package org.example.service.authFogetPassword;

import org.example.service.data.ForgetPasswordIData;
import org.example.service.data.ForgetPasswordOData;
import org.example.service.data.VerifyEmailIData;
import org.example.service.data.VerifyEmailOData;

public interface AuthForgetPasswordService {
    VerifyEmailOData verifyEmail(VerifyEmailIData input);

    ForgetPasswordOData generatePasscode(ForgetPasswordIData input);

    ForgetPasswordOData updatePassword(ForgetPasswordIData input);
}
