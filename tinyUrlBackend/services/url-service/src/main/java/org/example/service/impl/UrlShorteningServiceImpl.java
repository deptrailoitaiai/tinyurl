package org.example.service.impl;

import org.example.entity.ServiceReference;
import org.example.entity.Url;
import org.example.repository.master.ServiceReferenceMasterRepository;
import org.example.repository.master.UrlMasterRepository;
import org.example.repository.slave.UrlSlaveRepository;
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
    private StringRedisTemplate stringRedisTemplate;

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

        // 2. save url
        Url newUrl = Url.builder()
                .originalUrl(inputData.getOriginalUrl())
                .title(inputData.getTitle())
                .passwordHash(hashedPassword)
                .expiresAt(inputData.getExpiredDate())
                .build();

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

        // 4. generate short code from url id
        String shortCode = Base62Util.idToBase62(savedUrl.getId());

        // 5. return
        ret.setOriginalUrl(savedUrl.getOriginalUrl());
        ret.setTitle(savedUrl.getTitle());
        ret.setShortCode(shortCode);
        ret.setCreatedAt(savedUrl.getCreatedAt());
        ret.setExpiredDate(savedUrl.getExpiresAt());

        return ret;
    }
}
