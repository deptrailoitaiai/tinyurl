package org.example.service.impl;

import org.example.entity.Url;
import org.example.repository.master.ServiceReferenceMasterRepository;
import org.example.repository.master.UrlMasterRepository;
import org.example.repository.slave.UrlSlaveRepository;
import org.example.service.UrlRedirectService;
import org.example.service.data.RedirectWithPassword_I_Data;
import org.example.service.data.RedirectWithPassword_O_Data;
import org.example.service.data.RedirectWithoutPassword_I_Data;
import org.example.service.data.RedirectWithoutPassword_O_Data;
import org.example.util.Base62Util;
import org.example.util.HashAndCompareUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("UrlRedirectServiceImpl")
public class UrlRedirectServiceImpl implements UrlRedirectService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UrlSlaveRepository urlSlaveRepository;

    @Override
    public RedirectWithPassword_O_Data redirectWithPassword(RedirectWithPassword_I_Data inputData) {
        RedirectWithPassword_O_Data ret = new RedirectWithPassword_O_Data();

        // 1. get id from short code, find by id
        Long urlId = Base62Util.base62ToId(inputData.getShortCode());
        Optional<Url> optionalUrl = urlSlaveRepository.findById(urlId);

        if (optionalUrl.isEmpty()) {
            ret.setValidated(false);
            return ret;
        }

        Url url = optionalUrl.get();

        // 2. Check URL's status
        if (url.getStatus() == Url.UrlStatus.expired || url.getStatus() == Url.UrlStatus.disabled) {
            ret.setValidated(false);
            return ret;
        }

        // 3. Check password if URL is password protected
        // separate 2 if block cuz it could cause null pointer exception
        if (url.getPasswordHash() == null) {
            ret.setValidated(true);
            ret.setOriginUrl(url.getOriginalUrl());
            return ret;
        }

        if (!HashAndCompareUtil.compare(inputData.getPassword(), url.getPasswordHash())) {
            ret.setValidated(false);
            return ret;
        }

        // 4. return
        ret.setOriginUrl(url.getOriginalUrl());
        ret.setValidated(true);

        return ret;
    }

    @Override
    public RedirectWithoutPassword_O_Data redirectWithoutPassword(RedirectWithoutPassword_I_Data inputData) {
        RedirectWithoutPassword_O_Data ret = new RedirectWithoutPassword_O_Data();

        // 1. get id from short code, find by id
        Long urlId = Base62Util.base62ToId(inputData.getShortCode());

        Optional<Url> optionalUrl = urlSlaveRepository.findById(urlId);

        // 2. check url status
        if (optionalUrl.isEmpty()) {
            ret.setUrlAvailable(false);
            return ret;
        }

        Url url = optionalUrl.get();

        if (url.getStatus() == Url.UrlStatus.expired || url.getStatus() == Url.UrlStatus.disabled) {
            ret.setUrlAvailable(false);
            return ret;
        }

        // 3. check if url got password -> redirect to "redirectWithPassword"
        if (url.getPasswordHash() != null) {
            ret.setNeedPassword(true);
            ret.setShortCode(inputData.getShortCode());
            return ret;
        }

        // 4. return
        ret.setOriginalUrl(url.getOriginalUrl());
        return ret;
    }
}
