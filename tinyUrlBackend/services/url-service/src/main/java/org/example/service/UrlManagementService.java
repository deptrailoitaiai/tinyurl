package org.example.service;

import org.example.service.data.*;

public interface UrlManagementService {
    GetUrlInfoByIdOData getUrlInfoById(GetUrlInfoByIdIData inputData);

    CreateUrlInfoOData createUrlInfo(CreateUrlInfoIData inputData);

    UpdateUrlInfoOData updateUrlInfo(UpdateUrlInfoIData inputData);

    DeleteUrlInfoOData deleteUrlInfo(DeleteUrlInfoIData inputData);

    // TODO: get all url limit 10
}
