package org.example.service.UrlManagement;

import org.example.dto.CursorPageRequest;
import org.example.dto.CursorPageResponse;
import org.example.service.data.*;
import org.springframework.data.domain.Page;

public interface UrlManagementService {
    GetUrlInfoByIdOData getUrlInfoById(GetUrlInfoByIdIData inputData);

    CreateUrlInfoOData createUrlInfo(CreateUrlInfoIData inputData);

    UpdateUrlInfoOData updateUrlInfo(UpdateUrlInfoIData inputData);

    DeleteUrlInfoOData deleteUrlInfo(DeleteUrlInfoIData inputData);

    // Legacy method với traditional pagination
    Page<UrlProjection> getAllUrlInfo(int page, int size);
    
    // New method với cursor pagination
    CursorPageResponse<UrlProjection> getAllUrlInfoWithCursor(CursorPageRequest cursorRequest);
}
