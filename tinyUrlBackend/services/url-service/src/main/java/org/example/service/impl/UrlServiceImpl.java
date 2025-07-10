package org.example.service.impl;

import org.example.entity.Url;
import org.example.repository.master.UrlMasterRepository;
import org.example.repository.slave.UrlSlaveRepository;
import org.example.service.UrlService;
import org.example.service.data.ShorteningUrl_I_Data;
import org.example.service.data.ShorteningUrl_O_Data;
import org.example.util.Base62Util;
import org.example.util.HashAndCompareUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("UrlServiceImpl")
public class UrlServiceImpl implements UrlService {

    @Autowired
    private UrlMasterRepository urlMasterRepository;

    @Autowired
    private UrlSlaveRepository urlSlaveRepository;

    @Override
    public ShorteningUrl_O_Data shorteningUrl(ShorteningUrl_I_Data inputData) {
        ShorteningUrl_O_Data ret = new ShorteningUrl_O_Data();

        inputData.setPassword(HashAndCompareUtil.hash(inputData.getPassword()));

        Url newUrl = Url.builder()
                .userId(inputData.getUserId())
                .originalUrl(inputData.getOriginalUrl())
                .title(inputData.getTitle())
                .passwordHash(inputData.getPassword())
                .expiresAt(inputData.getExpiredDate())
                .build();

        Url savedUrl = urlMasterRepository.save(newUrl);

        String shortCode = Base62Util.idToBase62(savedUrl.getId());




        return ret;
    }

    @Override
    public void redirect() {

    }
}
