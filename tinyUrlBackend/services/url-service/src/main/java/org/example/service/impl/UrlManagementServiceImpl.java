package org.example.service.impl;

import jakarta.transaction.Transactional;
import org.example.constants.ErrorCode;
import org.example.entity.ServiceReference;
import org.example.entity.Url;
import org.example.repository.master.ServiceReferenceMasterRepository;
import org.example.repository.master.UrlMasterRepository;
import org.example.repository.slave.UrlSlaveRepository;
import org.example.service.data.*;
import org.example.util.Base62Util;
import org.example.util.HashAndCompareUtil;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service("UrlManagementServiceImpl")
public class UrlManagementServiceImpl implements org.example.service.UrlManagementService {

    @Autowired
    private UrlSlaveRepository urlSlaveRepository;

    @Autowired
    private UrlMasterRepository urlMasterRepository;

    @Autowired
    private ServiceReferenceMasterRepository serviceReferenceMasterRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    @Cacheable(
            value = "getUrlInfoCache",
            key = "#inputdata.shortCode",
            unless = "#result.errorCode = T(org.example.constants.ErrorCode).URL_NOT_FOUND"
    )
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
    @Transactional
    @Caching(
        evict = {
                @CacheEvict(
                        value = "urlInfoCache",
                        key = "#inputData.shortCode",
                        condition = "#result.errorCode = T(org.example.constants.ErrorCode).SUCCESS"
                )
        },
        put = {
                @CachePut(
                        value = "urlInfoCache",
                        key = "#inputData.shortCode",
                        condition = "#result.errorCode = T(org.example.constants.ErrorCode).SUCCESS"
                )
        }
    )
    public UpdateUrlInfo_O_Data updateUrlInfo(UpdateUrlInfo_I_Data inputData) {
        UpdateUrlInfo_O_Data ret = new UpdateUrlInfo_O_Data();

        // 1. try lock
        String uuidLock = UUID.randomUUID().toString();
        boolean canLock = tryLock(inputData.getShortCode(), uuidLock);

        if (!canLock) {
            ret.setErrorCode(ErrorCode.SYSTEM_ERROR);
            return ret;
        }

        // 2. get id by short code, find by id
        Long urlId = Base62Util.base62ToId(inputData.getShortCode());

        Optional<Url> getOptionalUrl = urlMasterRepository.findById(urlId);

        if (getOptionalUrl.isEmpty()) {
            ret.setErrorCode(ErrorCode.URL_NOT_FOUND);
            return ret;
        }

        Url getUrl = getOptionalUrl.get();

        // 3. set changed field and update with @DynamicUpdate (include hash password before update)
        getUrl.setTitle(inputData.getTitle() == null ? getUrl.getTitle() : inputData.getTitle());
        getUrl.setPasswordHash(inputData.getPassword() == null ? getUrl.getPasswordHash() : HashAndCompareUtil.hash(inputData.getPassword()));
        getUrl.setStatus(inputData.getStatus() == null ? getUrl.getStatus() : inputData.getStatus());
        getUrl.setExpiresAt(inputData.getExpiredAt() == null ? getUrl.getExpiresAt() : inputData.getExpiredAt());

        Url updateUrl = urlMasterRepository.save(getUrl);

        // 4. release lock
        boolean releaseLock = releaseLock(inputData.getShortCode(), uuidLock);

        ret.setErrorCode(ErrorCode.SUCCESS);
        ret.setShortCode(inputData.getShortCode());
        ret.setOriginalUrl(getUrl.getOriginalUrl());
        ret.setTitle(getUrl.getTitle());
        ret.setStatus(getUrl.getStatus());
        ret.setUpdateAt(getUrl.getUpdatedAt());
        ret.setExpireAt(getUrl.getExpiresAt());

        return ret;
    }

    @Override
    @CachePut(
            value = "urlInfoCache",
            key = "#inputData.shortCode",
            condition = "#result.errorCode == T(org.example.constants.ErrorCode).SUCCESS"
    )
    public CreateUrlInfo_O_Data createUrlInfo(CreateUrlInfo_I_Data inputData) {
        CreateUrlInfo_O_Data ret = new CreateUrlInfo_O_Data();

        // 1. hash password
        String hashedPassword = null;
        if (inputData.getPassword() != null && !inputData.getPassword().isEmpty()) {
            hashedPassword = HashAndCompareUtil.hash(inputData.getPassword());
        }

        // 2. create URL directly using master repository
        Url newUrl = new Url();
        newUrl.setOriginalUrl(inputData.getOriginalUrl());
        newUrl.setTitle(inputData.getTitle());
        newUrl.setPasswordHash(hashedPassword);
        if (inputData.getExpiresAt() != null) {
            newUrl.setExpiresAt(inputData.getExpiresAt());
        }

        Url savedUrl = null;
        try {
            savedUrl = urlMasterRepository.save(newUrl);
        } catch (DataIntegrityViolationException | ConstraintViolationException ex) {
            ret.setErrorCode(ErrorCode.URL_EXISTED);
            return ret;
        }

        // 3. save reference key to service_reference using another thread
        ServiceReference newReference = ServiceReference.builder()
                .localId(savedUrl.getId())
                .localTable("urls")
                .targetId(inputData.getUserId())
                .targetTable("users")
                .build();
        saveServiceReferenceAsync(newReference);

        // 4. return response
        String shortCode = Base62Util.idToBase62(savedUrl.getId());
        ret.setOriginalUrl(savedUrl.getOriginalUrl());
        ret.setTitle(savedUrl.getTitle());
        ret.setShortCode(shortCode);
        ret.setCreateAt(savedUrl.getCreatedAt());
        ret.setExpiredAt(savedUrl.getExpiresAt());

        return ret;
    }

    @Override
    @CacheEvict(
            value = "urlInfoCache",
            key = "#inputData.shortCode",
            condition = "#result.errorCode = T(org.example.constants.ErrorCode).SUCCESS"
    )
    public DeleteUrlInfo_O_Data deleteUrlInfo(DeleteUrlInfo_I_Data inputData) {
        DeleteUrlInfo_O_Data ret = new DeleteUrlInfo_O_Data();

        Long urlId = Base62Util.base62ToId(inputData.getShortCode());
        try {
            urlMasterRepository.deleteById(urlId);
            ret.setErrorCode(ErrorCode.SUCCESS);
        } catch (Exception e) {
            ret.setErrorCode(ErrorCode.SYSTEM_ERROR);
        }

        return ret;
    }

    private boolean tryLock(String shortCode, String uuid) {
        String lockKey = "updateUrlInfoLock" + "::" + shortCode;
        boolean lockAcquired = false;
        int maxRetries = 3;
        int retryCount = 0;

        while (!lockAcquired && retryCount < maxRetries) {
            lockAcquired = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, Duration.ofSeconds(30)));

            if (lockAcquired) {
                return lockAcquired;
            }

            retryCount ++;
        }

        return lockAcquired;
    }

    private boolean releaseLock(String shortCode, String uuid) {
        String lockKey = "updateUrlInfoLock" + "::" + shortCode;

        String valueOfLock = redisTemplate.opsForValue().get(lockKey);
        if(Objects.equals(uuid, valueOfLock)) {
            return redisTemplate.delete(lockKey);
        }

        return false;
    }

    private void saveServiceReferenceAsync(ServiceReference reference) {
        new Thread(() -> {
            try {
                serviceReferenceMasterRepository.save(reference);
            } catch (Exception e) {
                // Log error
                System.err.println("Failed to save service reference: " + e.getMessage());
            }
        }).start();
    }
}
