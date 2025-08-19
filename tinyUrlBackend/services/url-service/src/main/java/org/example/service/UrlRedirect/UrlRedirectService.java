package org.example.service.UrlRedirect;

import org.example.service.data.RedirectWithPasswordIData;
import org.example.service.data.RedirectWithPasswordOData;
import org.example.service.data.RedirectWithoutPasswordIData;
import org.example.service.data.RedirectWithoutPasswordOData;

public interface UrlRedirectService {
    RedirectWithPasswordOData redirectWithPassword(RedirectWithPasswordIData inputData);

    RedirectWithoutPasswordOData redirectWithoutPassword(RedirectWithoutPasswordIData inputData);
}
