package org.example.service.UrlManagement;

import org.example.service.data.*;
import org.springframework.data.domain.Page;

public interface UrlManagementService {
    GetUrlInfoByIdOData getUrlInfoById(GetUrlInfoByIdIData inputData);

    CreateUrlInfoOData createUrlInfo(CreateUrlInfoIData inputData);

    UpdateUrlInfoOData updateUrlInfo(UpdateUrlInfoIData inputData);

    DeleteUrlInfoOData deleteUrlInfo(DeleteUrlInfoIData inputData);

    Page<UrlProjection> getAllUrlInfo(int page, int size);
}
