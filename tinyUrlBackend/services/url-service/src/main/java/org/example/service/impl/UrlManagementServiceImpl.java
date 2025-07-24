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

import java.util.Optional;

@Service("UrlManagementServiceImpl")
public class UrlManagementServiceImpl implements org.example.service.UrlManagementService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UrlSlaveRepository urlSlaveRepository;

    @Autowired
    private UrlMasterRepository urlMasterRepository;

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

        Long urlId = Base62Util.base62ToId(inputData.getShortCode());

        Optional<Url> getOptionalUrl = urlSlaveRepository.findById(urlId);

        if(getOptionalUrl.isEmpty()) {

        }

        Url getUrl = getOptionalUrl.get();

        return ret;
    }
}
