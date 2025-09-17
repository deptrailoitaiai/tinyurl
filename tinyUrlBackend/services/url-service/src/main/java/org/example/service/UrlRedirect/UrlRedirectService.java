package org.example.service.UrlRedirect;

import org.example.service.data.RedirectWithPasswordIData;
import org.example.service.data.RedirectWithPasswordOData;
import org.example.service.data.RedirectWithoutPasswordIData;
import org.example.service.data.RedirectWithoutPasswordOData;
import org.example.service.data.RedirectWithTrackingIData;
import org.example.service.data.RedirectWithTrackingOData;

public interface UrlRedirectService {
    RedirectWithPasswordOData redirectWithPassword(RedirectWithPasswordIData inputData);

    RedirectWithoutPasswordOData redirectWithoutPassword(RedirectWithoutPasswordIData inputData);
    
    /**
     * Redirect with click tracking - captures user information and sends to analytics
     */
    RedirectWithTrackingOData redirectWithTracking(RedirectWithTrackingIData inputData);
}
