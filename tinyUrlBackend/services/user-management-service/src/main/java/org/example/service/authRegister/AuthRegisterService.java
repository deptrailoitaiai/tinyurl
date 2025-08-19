package org.example.service.authRegister;

import org.example.service.data.RegisterIData;
import org.example.service.data.RegisterOData;

public interface AuthRegisterService {
    RegisterOData register(RegisterIData input);
}
