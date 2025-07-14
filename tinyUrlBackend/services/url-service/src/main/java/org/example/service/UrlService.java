package org.example.service;

import org.example.service.data.*;

public interface UrlService {
    ShorteningUrl_O_Data shorteningUrl (ShorteningUrl_I_Data inputData);

    RedirectWithPassword_O_Data redirectWithPassword(RedirectWithPassword_I_Data inputData);

    RedirectWithoutPassword_O_Data redirectWithoutPassword(RedirectWithoutPassword_I_Data inputData);

    GetUrlInfoById_O_Data getUrlInfoById(GetUrlInfoById_I_Data inputData);
}
