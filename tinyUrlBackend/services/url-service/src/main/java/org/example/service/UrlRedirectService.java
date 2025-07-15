package org.example.service;

import org.example.service.data.RedirectWithPassword_I_Data;
import org.example.service.data.RedirectWithPassword_O_Data;
import org.example.service.data.RedirectWithoutPassword_I_Data;
import org.example.service.data.RedirectWithoutPassword_O_Data;

public interface UrlRedirectService {
    RedirectWithPassword_O_Data redirectWithPassword(RedirectWithPassword_I_Data inputData);

    RedirectWithoutPassword_O_Data redirectWithoutPassword(RedirectWithoutPassword_I_Data inputData);
}
