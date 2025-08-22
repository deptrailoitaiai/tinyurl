package org.example.service.authRegister;

import org.example.constants.ErrorCode;
import org.example.entity.User;
import org.example.repository.master.UserMasterRepository;
import org.example.service.data.*;
import org.example.util.HashAndCompareUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class DefaultAuthRegisterService implements AuthRegisterService {

    @Autowired
    private UserMasterRepository userMasterRepository;

    @Override
    public RegisterOData register(RegisterIData input) {
        RegisterOData ret = new RegisterOData();

        try {
            // check if exist
            Optional<User> findUser = userMasterRepository.findByEmail(input.getEmail());

            if(findUser.isPresent()){
                ret.setErrCode(ErrorCode.USER_EXISTED);
                return ret;
            }

            User newUser = new User();
            newUser.setEmail(input.getEmail());
            newUser.setFullName(input.getFullName());
            newUser.setPasswordHash(HashAndCompareUtil.hash(input.getPassword()));

            Long userId = userMasterRepository.save(newUser).getId();

            ret.setErrCode(ErrorCode.SUCCESS);
            ret.setUserId(userId);

            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public VerifyEmailRegisterOData verifyEmail(VerifyEmailRegisterIData input) {

        return null;
    }

    @Override
    public SendEmailToVerifyRegisterOData sendEmailToVerifyRegister(SendEmailToVerifyRegisterIData input) {


        return null;
    }

    @Cacheable(value = "cacheToken", key = "#userId")
    private String generateToken(String userId) {
        String token = UUID.randomUUID().toString();

        return token;
    }
}
