package org.example.service.authFogetPassword;

import org.example.service.data.ForgetPasswordIData;
import org.example.service.data.ForgetPasswordOData;
import org.example.service.data.VerifyEmailForgetPassIData;
import org.example.service.data.VerifyEmailForgetPassOData;

public interface AuthForgetPasswordService {
    VerifyEmailForgetPassOData verifyEmail(VerifyEmailForgetPassIData input);

    ForgetPasswordOData generatePasscode(ForgetPasswordIData input);

    ForgetPasswordOData updatePassword(ForgetPasswordIData input);
}
