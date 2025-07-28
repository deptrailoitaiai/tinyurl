package org.example.service.impl;

import org.example.constants.ErrorCode;
import org.example.entity.Url;
import org.example.repository.master.UrlMasterRepository;
import org.example.repository.slave.UrlSlaveRepository;
import org.example.service.data.*;
import org.example.util.Base62Util;
import org.example.util.HashAndCompareUtil;
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
            ret.setErrorCode(ErrorCode.URL_NOT_FOUND);
            return ret;
        }

        Url url = optionalUrl.get();

        // 2. return url info
        ret.setOriginalUrl(url.getOriginalUrl());
        ret.setTitle(url.getTitle());
        ret.setPasswordHash(url.getPasswordHash());
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
            ret.setErrorCode(ErrorCode.URL_NOT_FOUND);
            return ret;
        }

        Url getUrl = getOptionalUrl.get();

        return ret;
    }

    @Override
    public CreateUrlInfo_O_Data createUrlInfo(CreateUrlInfo_I_Data inputData) {
        CreateUrlInfo_O_Data ret = new CreateUrlInfo_O_Data();

        Url newUrl = new Url();
        newUrl.setOriginalUrl(inputData.getOriginalUrl());
        newUrl.setTitle(inputData.getTitle());
        newUrl.setPasswordHash(HashAndCompareUtil.hash(inputData.getPassword()));
        newUrl.setExpiresAt(inputData.getExpiresAt());

        Url savedUrl = urlMasterRepository.save(newUrl);

        // Generate shortCode from the saved URL's ID
        String shortCode = Base62Util.idToBase62(savedUrl.getId());
        ret.setShortCode(shortCode);
        ret.setOriginalUrl(savedUrl.getOriginalUrl());
        if (savedUrl.getExpiresAt() != null) {
            ret.setExpiresAt(savedUrl.getExpiresAt().toEpochSecond(java.time.ZoneOffset.UTC));
        }
        ret.setStatus("CREATED");

        return ret;
    }

    @Override
    public DeleteUrlInfo_O_Data deleteUrlInfo(DeleteUrlInfo_I_Data inputData) {
        DeleteUrlInfo_O_Data ret = new DeleteUrlInfo_O_Data();

        Long urlId = Base62Util.base62ToId(inputData.getShortCode());
        try {
            urlMasterRepository.deleteById(urlId);
            ret.setSuccess(true);
            ret.setMessage("URL deleted successfully");
        } catch (Exception e) {
            ret.setSuccess(false);
            ret.setMessage("Failed to delete URL: " + e.getMessage());
        }

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
            return redisTemplate.delete(lockKey);
        }

        return false;
    }

}
