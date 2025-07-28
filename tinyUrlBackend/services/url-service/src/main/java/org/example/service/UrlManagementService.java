package org.example.service;

import org.example.service.data.*;

public interface UrlManagementService {
    // Read operations
    GetUrlInfoById_O_Data getUrlInfoById(GetUrlInfoById_I_Data inputData);
    
    // Create operation
    CreateUrlInfo_O_Data createUrlInfo(CreateUrlInfo_I_Data inputData);
    
    // Update operations
    UpdateUrlInfo_O_Data updateUrlInfo(UpdateUrlInfo_I_Data inputData);
    
    // Delete operation
    DeleteUrlInfo_O_Data deleteUrlInfo(DeleteUrlInfo_I_Data inputData);
}
