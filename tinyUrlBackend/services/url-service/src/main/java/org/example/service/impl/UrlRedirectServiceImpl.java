package org.example.service.impl;

import org.example.constants.ErrorCode;
import org.example.entity.Url;
import org.example.repository.slave.UrlSlaveRepository;
import org.example.service.UrlRedirectService;
import org.example.service.data.*;
import org.example.util.Base62Util;
import org.example.util.HashAndCompareUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("UrlRedirectServiceImpl")
public class UrlRedirectServiceImpl implements UrlRedirectService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UrlSlaveRepository urlSlaveRepository;

    @Override
    public RedirectWithPassword_O_Data redirectWithPassword(RedirectWithPassword_I_Data inputData) {
        RedirectWithPassword_O_Data ret = new RedirectWithPassword_O_Data();

        // 1. Get URL by short code directly from slave repository
        Long urlId = Base62Util.base62ToId(inputData.getShortCode());
        Optional<Url> optionalUrl = urlSlaveRepository.findById(urlId);

        if (optionalUrl.isEmpty()) {
            ret.setErrorCode(ErrorCode.URL_NOT_FOUND);
            return ret;
        }

        Url url = optionalUrl.get();

        // 2. Check URL's status
        if (url.getStatus().equals(Url.UrlStatus.EXPIRED) || url.getStatus().equals(Url.UrlStatus.DISABLED)) {
            ret.setErrorCode((url.getStatus() == Url.UrlStatus.EXPIRED) ? ErrorCode.URL_EXPIRED : ErrorCode.URL_DISABLED);
            return ret;
        }

        // 3. Check password if URL is password protected
        if (url.getPasswordHash() == null) {
            ret.setErrorCode(ErrorCode.SUCCESS);
            ret.setOriginUrl(url.getOriginalUrl());
            return ret;
        }

        if (!HashAndCompareUtil.compare(inputData.getPassword(), url.getPasswordHash())) {
            ret.setErrorCode(ErrorCode.PASSWORD_IN_CORRECT);
            return ret;
        }

        // TODO: send request to analytics service

        // 4. return
        ret.setOriginUrl(url.getOriginalUrl());
        ret.setErrorCode(ErrorCode.SUCCESS);

        return ret;
    }

    @Override
    public RedirectWithoutPassword_O_Data redirectWithoutPassword(RedirectWithoutPassword_I_Data inputData) {
        RedirectWithoutPassword_O_Data ret = new RedirectWithoutPassword_O_Data();

        // 1. Get URL by short code directly from slave repository
        Long urlId = Base62Util.base62ToId(inputData.getShortCode());
        Optional<Url> optionalUrl = urlSlaveRepository.findById(urlId);

        if (optionalUrl.isEmpty()) {
            ret.setErrorCode(ErrorCode.URL_NOT_FOUND);
            return ret;
        }

        Url url = optionalUrl.get();

        // 2. Check URL status
        if (url.getStatus().equals(Url.UrlStatus.EXPIRED) || url.getStatus().equals(Url.UrlStatus.DISABLED)) {
            ret.setErrorCode((url.getStatus() == Url.UrlStatus.EXPIRED) ? ErrorCode.URL_EXPIRED : ErrorCode.URL_DISABLED);
            return ret;
        }

        // 3. Check if URL has password
        if (url.getPasswordHash() != null) {
            ret.setErrorCode(ErrorCode.PASSWORD_REQUIRED);
            ret.setShortCode(inputData.getShortCode());
            return ret;
        }

        // 4. return
        ret.setOriginalUrl(url.getOriginalUrl());
        ret.setErrorCode(ErrorCode.SUCCESS);
        return ret;
    }
}
