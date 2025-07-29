package org.example.service.impl;

import org.example.entity.ServiceReference;
import org.example.entity.Url;
import org.example.repository.master.ServiceReferenceMasterRepository;
import org.example.repository.master.UrlMasterRepository;
import org.example.service.UrlShorteningService;
import org.example.service.data.*;
import org.example.util.Base62Util;
import org.example.util.HashAndCompareUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service("UrlShorteningServiceImpl")
public class UrlShorteningServiceImpl implements UrlShorteningService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UrlMasterRepository urlMasterRepository;

    @Autowired
    private ServiceReferenceMasterRepository serviceReferenceMasterRepository;

    @Override
    public ShorteningUrl_O_Data shorteningUrl(ShorteningUrl_I_Data inputData) {
        ShorteningUrl_O_Data ret = new ShorteningUrl_O_Data();

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
        if (inputData.getExpiredDate() != null) {
            newUrl.setExpiresAt(inputData.getExpiredDate());
        }
        
        Url savedUrl = urlMasterRepository.save(newUrl);

        // 3. save reference key to service_reference using another thread
        ServiceReference newReference = ServiceReference.builder()
                .localId(savedUrl.getId())
                .localTable("urls")
                .targetId(inputData.getUserId())
                .targetTable("users")
                .build();

        Thread threadSaveNewReference = new Thread(() -> {
            try {
                serviceReferenceMasterRepository.save(newReference);
            } catch (Exception e) {
                // Log error but don't fail the main operation
                System.err.println("Failed to save service reference: " + e.getMessage());
            }
        });
        threadSaveNewReference.start();

        // 4. return response
        String shortCode = Base62Util.idToBase62(savedUrl.getId());
        ret.setOriginalUrl(savedUrl.getOriginalUrl());
        ret.setTitle(savedUrl.getTitle());
        ret.setShortCode(shortCode);
        ret.setCreatedAt(savedUrl.getCreatedAt());
        ret.setExpiredDate(savedUrl.getExpiresAt());

        return ret;
    }
}
