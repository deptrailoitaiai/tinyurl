package org.example.service;

import org.example.service.data.ShorteningUrl_I_Data;
import org.example.service.data.ShorteningUrl_O_Data;

public interface UrlService {
    ShorteningUrl_O_Data shorteningUrl (ShorteningUrl_I_Data inputData);

    void redirect();
}
