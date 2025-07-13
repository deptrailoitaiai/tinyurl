package org.example.service.impl;

import org.example.constants.ErrorMessage;
import org.example.entity.ServiceReference;
import org.example.entity.Url;
import org.example.exeption.PasswordUrlIncorrectException;
import org.example.exeption.UrlNotFoundException;
import org.example.repository.master.ServiceReferenceMasterRepository;
import org.example.repository.master.UrlMasterRepository;
import org.example.repository.slave.UrlSlaveRepository;
import org.example.service.UrlService;
import org.example.service.data.ValidateAndRedirect_I_Data;
import org.example.service.data.ValidateAndRedirect_O_Data;
import org.example.service.data.ShorteningUrl_I_Data;
import org.example.service.data.ShorteningUrl_O_Data;
import org.example.util.Base62Util;
import org.example.util.HashAndCompareUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

        String hashedPassword = null;
        if (inputData.getPassword() != null && !inputData.getPassword().trim().isEmpty()) {
            hashedPassword = HashAndCompareUtil.hash(inputData.getPassword());
        }

        Url newUrl = Url.builder()
                .originalUrl(inputData.getOriginalUrl())
                .title(inputData.getTitle())
                .passwordHash(hashedPassword)
                .expiresAt(inputData.getExpiredDate())
                .build();

        Url savedUrl = urlMasterRepository.save(newUrl);

        ServiceReference newReference = ServiceReference.builder()
                .localId(savedUrl.getId())
                .localTable("urls")
                .targetId(inputData.getUserId())
                .targetTable("users")
                .build();

        // Save reference asynchronously with error handling
        Thread threadSaveNewReference = new Thread(() -> {
            try {
                serviceReferenceMasterRepository.save(newReference);
            } catch (Exception e) {
                // Log error but don't fail the main operation
                System.err.println("Failed to save service reference: " + e.getMessage());
            }
        });
        threadSaveNewReference.start();

        String shortCode = Base62Util.idToBase62(savedUrl.getId());

        ret.setOriginalUrl(savedUrl.getOriginalUrl());
        ret.setTitle(savedUrl.getTitle());
        ret.setShortCode(shortCode); // Fix: Set the shortCode
        ret.setCreatedAt(savedUrl.getCreatedAt());
        ret.setExpiredDate(savedUrl.getExpiresAt());

        return ret;
    }

    @Override
    public ValidateAndRedirect_O_Data validateAndRedirect(ValidateAndRedirect_I_Data inputData) {
        ValidateAndRedirect_O_Data ret = new ValidateAndRedirect_O_Data();

        Long urlId = Base62Util.base62ToId(inputData.getShortCode());
        Optional<Url> optionalUrl = urlSlaveRepository.findById(urlId);

        if (optionalUrl.isEmpty()) {
            ret.setValidated(false);
            return ret;
        }

        Url url = optionalUrl.get();

        // Check if URL is expired or disabled
        if (url.getStatus() == Url.UrlStatus.expired || url.getStatus() == Url.UrlStatus.disabled) {
            ret.setValidated(false);
            return ret;
        }

        // Check password if URL is password protected
        if (url.getPasswordHash() != null) {
            String providedPassword = inputData.getPassword();
            if (providedPassword == null || providedPassword.isEmpty()) {
                throw new PasswordUrlIncorrectException("Password required for this URL");
            }

            boolean passwordMatches = HashAndCompareUtil.compare(providedPassword, url.getPasswordHash());
            if (!passwordMatches) {
                throw new PasswordUrlIncorrectException("Incorrect password");
            }
        }

        ret.setOriginUrl(url.getOriginalUrl());
        ret.setValidated(true);


        return ret;
    }

    @Override
    public void getUrlByShortCode() {
        // TODO: Implement this method based on business requirements
        // This method signature is incomplete - it should probably:
        // 1. Take a shortCode parameter
        // 2. Return URL information or throw exception if not found
        throw new UnsupportedOperationException("Method not implemented yet");
    }
}
