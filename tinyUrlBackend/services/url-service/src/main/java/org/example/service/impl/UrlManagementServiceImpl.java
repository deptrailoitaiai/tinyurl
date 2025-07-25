package org.example.service.impl;

import org.example.entity.Url;
import org.example.repository.master.UrlMasterRepository;
import org.example.repository.slave.UrlSlaveRepository;
import org.example.service.data.GetUrlInfoById_I_Data;
import org.example.service.data.GetUrlInfoById_O_Data;
import org.example.service.data.UpdateUrlInfo_I_Data;
import org.example.service.data.UpdateUrlInfo_O_Data;
import org.example.util.Base62Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service("UrlManagementServiceImpl")
public class UrlManagementServiceImpl implements org.example.service.UrlManagementService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UrlSlaveRepository urlSlaveRepository;

    @Autowired
    private UrlMasterRepository urlMasterRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public GetUrlInfoById_O_Data getUrlInfoById(GetUrlInfoById_I_Data inputData) {
        GetUrlInfoById_O_Data ret = new GetUrlInfoById_O_Data();

        // 1. get id by short code, find by id
        Long urlId = Base62Util.base62ToId(inputData.getShortCode());

        Optional<Url> optionalUrl = urlSlaveRepository.findById(urlId);

        if (optionalUrl.isEmpty()) {
            ret.setUrlFound(false);
            return ret;
        }

        Url url = optionalUrl.get();

        // 2. return url info
        ret.setOriginalUrl(url.getOriginalUrl());
        ret.setStatus(url.getStatus());
        ret.setCreatedAt(url.getCreatedAt());
        ret.setLastUpdate(url.getUpdatedAt());
        ret.setExpiredAt(url.getExpiresAt());

        return ret;
    }

    @Override
    public UpdateUrlInfo_O_Data updateUrlInfo(UpdateUrlInfo_I_Data inputData) {
        UpdateUrlInfo_O_Data ret = new UpdateUrlInfo_O_Data();

        // 1. try lock
        String uuidLock = UUID.randomUUID().toString();
        boolean lock = tryLock(inputData.getShortCode(), uuidLock);
        // TODO: if cant lock ???

        // 2. get id by short code, find by id
        Long urlId = Base62Util.base62ToId(inputData.getShortCode());

        Optional<Url> getOptionalUrl = urlSlaveRepository.findById(urlId);

        if (getOptionalUrl.isEmpty()) {

        }

        Url getUrl = getOptionalUrl.get();

        return ret;
    }

    private boolean tryLock(String shortCode, String uuid) {
        String lockKey = "updateUrlInfoLock" + shortCode;

        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, Duration.ofSeconds(30)));
    }

    private boolean releaseLock(String shortCode, String uuid) {
        String lockKey = "updateUrlInfoLock" + shortCode;

        String valueOfLock = redisTemplate.opsForValue().get(lockKey);
        if(Objects.equals(uuid, valueOfLock)) {
            return Boolean.TRUE.equals(redisTemplate.delete(lockKey));
        }

        return false;
    }
}
