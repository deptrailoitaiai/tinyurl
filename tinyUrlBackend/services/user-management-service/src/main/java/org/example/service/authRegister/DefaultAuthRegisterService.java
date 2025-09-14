package org.example.service.authRegister;

import org.example.constants.ErrorCode;
import org.example.entity.User;
import org.example.repository.master.UserMasterRepository;
import org.example.service.EmailVerification.EmailVerificationService;
import org.example.service.data.*;
import org.example.util.HashAndCompareUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class DefaultAuthRegisterService implements AuthRegisterService {

    @Autowired
    private UserMasterRepository userMasterRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private EmailVerificationService emailVerificationService;

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

            newUser = userMasterRepository.save(newUser);

            ret.setErrCode(ErrorCode.SUCCESS);
            ret.setEmail(newUser.getEmail());
            ret.setUserId(newUser.getId());

            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public VerifyEmailRegisterOData verifyEmail(VerifyEmailRegisterIData input) {
        VerifyEmailRegisterOData ret = new VerifyEmailRegisterOData();
        try {
            String userId = input.getUserId();
            String providedToken = input.getToken();

            String redisKey = "cacheToken::" + userId;
            String storedToken = redisTemplate.opsForValue().get(redisKey);

            if (storedToken == null || !storedToken.equals(providedToken)) {
                ret.setErrCode(ErrorCode.UNIDENTIFIED_TOKEN);
                return ret;
            }

            redisTemplate.delete(redisKey);

            ret.setErrCode(ErrorCode.SUCCESS);
            return ret;

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SendEmailToVerifyRegisterOData sendEmailToVerifyRegister(SendEmailToVerifyRegisterIData input) {
        try {
            // Use EmailVerificationService to send verification email
            return emailVerificationService.sendVerificationEmail(input);
        } catch (Exception e) {
            SendEmailToVerifyRegisterOData ret = new SendEmailToVerifyRegisterOData();
            ret.setErrCode(ErrorCode.EMAIL_SEND_FAILED);
            ret.setMessage("Failed to send verification email: " + e.getMessage());
            return ret;
        }
    }

    private String generateToken(String userId) {
        String token = UUID.randomUUID().toString();
        String redisKey = "cacheToken::" + userId;
        redisTemplate.opsForValue().set(redisKey, token, 5, TimeUnit.MINUTES);
        return token;
    }
}
