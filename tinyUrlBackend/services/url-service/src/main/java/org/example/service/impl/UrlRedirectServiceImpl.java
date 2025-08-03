package org.example.service.impl;

import org.example.constants.ErrorCode;
import org.example.entity.Url;
import org.example.repository.slave.UrlSlaveRepository;
import org.example.service.UrlRedirectService;
import org.example.service.data.*;
import org.example.util.Base62Util;
import org.example.util.HashAndCompareUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
    public RedirectWithPasswordOData redirectWithPassword(RedirectWithPasswordIData inputData) {
        RedirectWithPasswordOData ret = new RedirectWithPasswordOData();

        // Try to get from cache first
        UrlRedirectCacheData cachedData = getCachedRedirectInfo(inputData.getShortCode());
        if (cachedData != null) {
            return processRedirectWithPassword(cachedData, inputData.getPassword(), ret);
        }

        // Cache miss - fetch from database
        Long urlId = Base62Util.base62ToId(inputData.getShortCode());
        Optional<Url> optionalUrl = urlSlaveRepository.findById(urlId);

        if (optionalUrl.isEmpty()) {
            ret.setErrorCode(ErrorCode.URL_NOT_FOUND);
            return ret;
        }

        Url url = optionalUrl.get();

        // Cache the URL redirect info for future use
        UrlRedirectCacheData cacheData = UrlRedirectCacheData.fromUrl(url, inputData.getShortCode());
        cacheRedirectInfo(inputData.getShortCode(), cacheData);

        return processRedirectWithPassword(cacheData, inputData.getPassword(), ret);
    }

    @Override
    public RedirectWithoutPasswordOData redirectWithoutPassword(RedirectWithoutPasswordIData inputData) {
        RedirectWithoutPasswordOData ret = new RedirectWithoutPasswordOData();

        // Try to get from cache first
        UrlRedirectCacheData cachedData = getCachedRedirectInfo(inputData.getShortCode());
        if (cachedData != null) {
            return processRedirectWithoutPassword(cachedData, ret);
        }

        // Cache miss - fetch from database
        Long urlId = Base62Util.base62ToId(inputData.getShortCode());
        Optional<Url> optionalUrl = urlSlaveRepository.findById(urlId);

        if (optionalUrl.isEmpty()) {
            ret.setErrorCode(ErrorCode.URL_NOT_FOUND);
            return ret;
        }

        Url url = optionalUrl.get();

        // Cache the URL redirect info for future use
        UrlRedirectCacheData cacheData = UrlRedirectCacheData.fromUrl(url, inputData.getShortCode());
        cacheRedirectInfo(inputData.getShortCode(), cacheData);

        return processRedirectWithoutPassword(cacheData, ret);
    }

    /**
     * Process redirect with password using cached data
     */
    private RedirectWithPasswordOData processRedirectWithPassword(UrlRedirectCacheData cacheData, String password, RedirectWithPasswordOData ret) {
        // Check URL's status
        if (!cacheData.isAvailableForRedirect()) {
            ret.setErrorCode((cacheData.getStatus() == Url.UrlStatus.EXPIRED) ? ErrorCode.URL_EXPIRED : ErrorCode.URL_DISABLED);
            return ret;
        }

        // Check password if URL is password protected
        if (!cacheData.isPasswordProtected()) {
            ret.setErrorCode(ErrorCode.SUCCESS);
            ret.setOriginUrl(cacheData.getOriginalUrl());
            return ret;
        }

        if (!HashAndCompareUtil.compare(password, cacheData.getPasswordHash())) {
            ret.setErrorCode(ErrorCode.PASSWORD_IN_CORRECT);
            return ret;
        }

        // TODO: send request to analytics service

        ret.setOriginUrl(cacheData.getOriginalUrl());
        ret.setErrorCode(ErrorCode.SUCCESS);
        return ret;
    }

    /**
     * Process redirect without password using cached data
     */
    private RedirectWithoutPasswordOData processRedirectWithoutPassword(UrlRedirectCacheData cacheData, RedirectWithoutPasswordOData ret) {
        // Check URL status
        if (!cacheData.isAvailableForRedirect()) {
            ret.setErrorCode((cacheData.getStatus() == Url.UrlStatus.EXPIRED) ? ErrorCode.URL_EXPIRED : ErrorCode.URL_DISABLED);
            return ret;
        }

        // Check if URL has password
        if (cacheData.isPasswordProtected()) {
            ret.setErrorCode(ErrorCode.PASSWORD_REQUIRED);
            ret.setShortCode(cacheData.getShortCode());
            return ret;
        }

        ret.setOriginalUrl(cacheData.getOriginalUrl());
        ret.setErrorCode(ErrorCode.SUCCESS);
        return ret;
    }

    /**
     * Cache URL redirect info using manual cache operations
     */
    @CachePut(value = "urlRedirectCache", key = "#shortCode")
    private UrlRedirectCacheData cacheRedirectInfo(String shortCode, UrlRedirectCacheData cacheData) {
        return cacheData;
    }

    /**
     * Get cached URL redirect info
     */
    @Cacheable(value = "urlRedirectCache", key = "#shortCode")
    private UrlRedirectCacheData getCachedRedirectInfo(String shortCode) {
        return null; // Will be populated by cache if exists
    }
}
