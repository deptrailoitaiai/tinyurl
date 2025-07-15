package org.example.service;

import org.example.service.data.GetUrlInfoById_I_Data;
import org.example.service.data.GetUrlInfoById_O_Data;
import org.example.service.data.UpdateUrlInfo_I_Data;
import org.example.service.data.UpdateUrlInfo_O_Data;

public interface UrlManagementService {
    GetUrlInfoById_O_Data getUrlInfoById(GetUrlInfoById_I_Data inputData);

    UpdateUrlInfo_O_Data updateUrlInfo(UpdateUrlInfo_I_Data inputData);
}
