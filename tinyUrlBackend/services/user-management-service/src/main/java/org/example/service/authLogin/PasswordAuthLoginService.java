package org.example.service.authLogin;

import org.example.config.jwt.JwtService;
import org.example.constants.ErrorCode;
import org.example.entity.User;
import org.example.repository.master.UserMasterRepository;
import org.example.service.data.AuthenticateLoginIData;
import org.example.service.data.AuthenticateLoginOData;
import org.example.util.HashAndCompareUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("PasswordAuthLoginService")
public class PasswordAuthLoginService implements AuthLoginService {

    @Autowired
    private UserMasterRepository userMasterRepository;

    @Autowired
    private JwtService jwtService;

    @Override
    public AuthenticateLoginOData authenticate(AuthenticateLoginIData input) {
        AuthenticateLoginOData ret = new AuthenticateLoginOData();

        try {
            Optional<User> getUser = userMasterRepository.findByEmail(input.getEmail());

            if(getUser.isEmpty()){
                ret.setErrCode(ErrorCode.USER_NOT_FOUND);
                return ret;
            }

            User user = getUser.get();

            if(!HashAndCompareUtil.compare(input.getPassword(), user.getPasswordHash())) {
                ret.setErrCode(ErrorCode.PASSWORD_IN_CORRECT);
                return ret;
            }

            String jwtAccessToken = jwtService.generateAccessToken(user.getId());
            String jwtRefreshToken = jwtService.generateRefreshToken(user.getId());

            ret.setErrCode(ErrorCode.SUCCESS);
            ret.setAccessToken(jwtAccessToken);
            ret.setRefreshToken(jwtRefreshToken);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ret;
    }
}
