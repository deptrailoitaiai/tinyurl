package org.example.service.UrlManagement;

import jakarta.transaction.Transactional;
import org.example.constants.ErrorCode;
import org.example.entity.ServiceReference;
import org.example.entity.Url;
import org.example.repository.master.ServiceReferenceMasterRepository;
import org.example.repository.master.UrlMasterRepository;
import org.example.repository.slave.UrlSlaveRepository;
import org.example.service.UrlManagement.UrlManagementService;
import org.example.service.data.*;
import org.example.util.Base62Util;
import org.example.util.HashAndCompareUtil;
import org.hibernate.exception.ConstraintViolationException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service("DefaultUrlManagementService")
public class DefaultUrlManagementService implements UrlManagementService {

    @Autowired
    private UrlSlaveRepository urlSlaveRepository;

    @Autowired
    private UrlMasterRepository urlMasterRepository;

    @Autowired
    private ServiceReferenceMasterRepository serviceReferenceMasterRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public Page<UrlProjection> getAllUrlInfo(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UrlProjection> urlPage = urlSlaveRepository.findAllBy(pageable);

        return urlPage;
    }

    @Override
    public GetUrlInfoByIdOData getUrlInfoById(GetUrlInfoByIdIData inputData) {
        GetUrlInfoByIdOData ret = new GetUrlInfoByIdOData();
        Long urlId = Base62Util.base62ToId(inputData.getShortCode());

        // Try to get from cache first
        UrlInfoCacheData cachedData = getCachedUrlInfo(inputData.getShortCode());
        if (cachedData != null) {
            // Verify user has access to this URL
            if (isUserAuthorizedForUrl(urlId, inputData.getUserId())) {
                ret = cachedData.toGetUrlInfoByIdOData();
                ret.setErrorCode(ErrorCode.SUCCESS);
                return ret;
            }
        }

        // Cache miss or access denied - fetch from database
        // Check if user has access to this url
        if (!isUserAuthorizedForUrl(urlId, inputData.getUserId())) {
            ret.setErrorCode(ErrorCode.URL_NOT_FOUND);
            return ret;
        }

        Optional<Url> optionalUrl = urlSlaveRepository.findById(urlId);

        if (optionalUrl.isEmpty()) {
            ret.setErrorCode(ErrorCode.URL_NOT_FOUND);
            return ret;
        }

        Url url = optionalUrl.get();

        // Cache the URL info for future use
        UrlInfoCacheData cacheData = UrlInfoCacheData.fromUrl(url, inputData.getShortCode());
        cacheUrlInfo(inputData.getShortCode(), cacheData);

        // 3. return url info
        ret.setOriginalUrl(url.getOriginalUrl());
        ret.setTitle(url.getTitle());
        ret.setPasswordHash(url.getPasswordHash());
        ret.setStatus(url.getStatus());
        ret.setCreatedAt(url.getCreatedAt());
        ret.setLastUpdate(url.getUpdatedAt());
        ret.setExpiredAt(url.getExpiresAt());
        ret.setErrorCode(ErrorCode.SUCCESS);

        return ret;
    }

    @Override
    @Transactional
    public UpdateUrlInfoOData updateUrlInfo(UpdateUrlInfoIData inputData) {
        UpdateUrlInfoOData ret = new UpdateUrlInfoOData();
        boolean locked = tryLock(inputData.getShortCode());

        if (!locked) {
            ret.setErrorCode(ErrorCode.SYSTEM_ERROR);
            return ret;
        }

        try {
            Long urlId = Base62Util.base62ToId(inputData.getShortCode());

            // Check if user has access to this URL
            if (!isUserAuthorizedForUrl(urlId, inputData.getUserId())) {
                ret.setErrorCode(ErrorCode.URL_NOT_FOUND);
                return ret;
            }

            Optional<Url> urlOpt = urlMasterRepository.findById(urlId);
            if (urlOpt.isEmpty()) {
                ret.setErrorCode(ErrorCode.URL_NOT_FOUND);
                return ret;
            }

            Url url = urlOpt.get();

            if (inputData.getTitle() != null) url.setTitle(inputData.getTitle());
            if (inputData.getPassword() != null) url.setPasswordHash(HashAndCompareUtil.hash(inputData.getPassword()));
            if (inputData.getStatus() != null) url.setStatus(inputData.getStatus());
            if (inputData.getExpiredAt() != null) url.setExpiresAt(inputData.getExpiredAt());

            urlMasterRepository.save(url);

            // Update cache with new data
            UrlInfoCacheData cacheData = UrlInfoCacheData.fromUrl(url, inputData.getShortCode());
            cacheUrlInfo(inputData.getShortCode(), cacheData);
            
            // Also update redirect cache since URL data changed
            evictRedirectCache(inputData.getShortCode());

            // Set response
            ret.setErrorCode(ErrorCode.SUCCESS);
            ret.setShortCode(inputData.getShortCode());
            ret.setOriginalUrl(url.getOriginalUrl());
            ret.setTitle(url.getTitle());
            ret.setStatus(url.getStatus());
            ret.setUpdateAt(url.getUpdatedAt());
            ret.setExpireAt(url.getExpiresAt());
        } catch (RuntimeException ex) {
            ret.setErrorCode(ErrorCode.SYSTEM_ERROR);
        } finally {
            boolean released = releaseLock(inputData.getShortCode());
            if (!released) {
                System.err.println("Không thể release lock cho shortCode: " + inputData.getShortCode());
            }
        }

        return ret;
    }


    @Override
//    @CachePut(
//            value = "urlInfoCache",
//            key = "",
//            condition = "#result.errorCode == T(org.example.constants.ErrorCode).SUCCESS"
//    )
    @Transactional
    public CreateUrlInfoOData createUrlInfo(CreateUrlInfoIData inputData) {
        CreateUrlInfoOData ret = new CreateUrlInfoOData();

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
                .localTable(ServiceReference.LocalTable.Urls)
                .targetId(inputData.getUserId())
                .targetTable(ServiceReference.TargetTable.Users)
                .build();

        try {
            serviceReferenceMasterRepository.save(newReference);
        } catch (Exception e) {
            ret.setErrorCode(ErrorCode.SYSTEM_ERROR);
            return ret;
        }

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
    @Transactional
    public DeleteUrlInfoOData deleteUrlInfo(DeleteUrlInfoIData inputData) {
        DeleteUrlInfoOData ret = new DeleteUrlInfoOData();

        Long urlId = Base62Util.base62ToId(inputData.getShortCode());

        // Check if url belongs to user
        if (!isUserAuthorizedForUrl(urlId, inputData.getUserId())) {
            ret.setErrorCode(ErrorCode.URL_NOT_FOUND);
            return ret;
        }

        try {
            urlMasterRepository.deleteById(urlId);
            
            // Remove from cache after successful deletion
            evictUrlInfoCache(inputData.getShortCode());
            
            // Also remove from redirect cache
            evictRedirectCache(inputData.getShortCode());
            
            ret.setErrorCode(ErrorCode.SUCCESS);
        } catch (Exception e) {
            ret.setErrorCode(ErrorCode.SYSTEM_ERROR);
        }

        return ret;
    }

    public boolean tryLock(String shortCode) {
        String lockKey = "updateUrlInfoLock::" + shortCode;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            return lock.tryLock(1, 10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public boolean releaseLock(String shortCode) {
        String lockKey = "updateUrlInfoLock::" + shortCode;
        RLock lock = redissonClient.getLock(lockKey);

        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            return true;
        }

        return false;
    }

    /**
     * Cache URL info using manual cache operations
     */
    @CachePut(value = "urlInfoCache", key = "#shortCode")
    private UrlInfoCacheData cacheUrlInfo(String shortCode, UrlInfoCacheData cacheData) {
        return cacheData;
    }

    /**
     * Get cached URL info
     */
    @Cacheable(value = "urlInfoCache", key = "#shortCode")
    private UrlInfoCacheData getCachedUrlInfo(String shortCode) {
        return null; // Will be populated by cache if exists
    }

    /**
     * Evict URL info from cache
     */
    @CacheEvict(value = "urlInfoCache", key = "#shortCode")
    private void evictUrlInfoCache(String shortCode) {
        // Cache will be evicted automatically
    }

    /**
     * Evict URL redirect cache when URL data changes
     */
    @CacheEvict(value = "urlRedirectCache", key = "#shortCode")
    private void evictRedirectCache(String shortCode) {
        // Cache will be evicted automatically
    }

    /**
     * Check if a specific user has access to a specific URL
     * More efficient than getting all authorized users when we only need to check one user
     */
    private boolean isUserAuthorizedForUrl(Long urlId, Long userId) {
        Optional<ServiceReference> reference = serviceReferenceMasterRepository
                .findAllByLocalIdAndLocalTableAndTargetIdAndTargetTable(
                        urlId,
                        ServiceReference.LocalTable.Urls,
                        userId,
                        ServiceReference.TargetTable.Users
                );
        
        return reference.isPresent();
    }

}

// TODO: check code again