package org.example.service.impl;

import org.example.entity.ServiceReference;
import org.example.entity.Url;
import org.example.repository.master.ServiceReferenceMasterRepository;
import org.example.repository.master.UrlMasterRepository;
import org.example.repository.slave.UrlSlaveRepository;
import org.example.service.UrlService;
import org.example.service.data.*;
import org.example.util.Base62Util;
import org.example.util.HashAndCompareUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("UrlServiceImpl")
public class UrlServiceImpl implements UrlService {

    @Autowired
    private UrlMasterRepository urlMasterRepository;

    @Autowired
    private UrlSlaveRepository urlSlaveRepository;

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

        // 2. set url info
        ret.setOriginalUrl(url.getOriginalUrl());
        ret.setStatus(url.getStatus());
        ret.setCreatedAt(url.getCreatedAt());
        ret.setLastUpdate(url.getUpdatedAt());
        ret.setExpiredAt(url.getExpiresAt());

        return ret;
    }
}
