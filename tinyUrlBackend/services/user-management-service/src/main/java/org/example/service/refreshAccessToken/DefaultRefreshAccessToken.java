package org.example.service.refreshAccessToken;

import org.example.config.jwt.JwtService;
import org.example.constants.ErrorCode;
import org.example.entity.User;
import org.example.repository.master.UserMasterRepository;
import org.example.service.data.RefreshAccessTokenIData;
import org.example.service.data.RefreshAccessTokenOData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service("DefaultRefreshAccessToken")
public class DefaultRefreshAccessToken implements RefreshAccessTokenService{

    @Autowired
    private UserMasterRepository userMasterRepository;

    @Autowired
    private JwtService jwtService;

    @Override
    public RefreshAccessTokenOData refresh(RefreshAccessTokenIData input) {
        RefreshAccessTokenOData ret = new RefreshAccessTokenOData();
        boolean isRefreshToken;
        Long userId;

        try {
            isRefreshToken = jwtService.validateToken(input.getRefreshToken());

            if (!isRefreshToken) {
                ret.setErrCode(ErrorCode.UNIDENTIFIED_TOKEN);
                return ret;
            }

            isRefreshToken =
                    Objects.equals(jwtService.getPayload(input.getRefreshToken()).getStringClaim("token_type"), "REFRESH");

            if (!isRefreshToken) {
                ret.setErrCode(ErrorCode.UNIDENTIFIED_TOKEN);
                return ret;
            }

            userId = Long.parseLong(jwtService.getPayload(input.getRefreshToken()).getSubject());

            Optional<User> user = userMasterRepository.findById(userId);

            if(user.isEmpty()) {
                ret.setErrCode(ErrorCode.UNIDENTIFIED_TOKEN);
                return ret;
            }

            userId = user.get().getId();

            ret.setErrCode(ErrorCode.SUCCESS);
            ret.setAccessToken(jwtService.generateAccessToken(userId));
            ret.setRefreshToken(jwtService.generateRefreshToken(userId));

            return ret;
        } catch (Exception e) {
            ret.setErrCode(ErrorCode.SYSTEM_ERROR);
            return ret;
        }
    }
}
