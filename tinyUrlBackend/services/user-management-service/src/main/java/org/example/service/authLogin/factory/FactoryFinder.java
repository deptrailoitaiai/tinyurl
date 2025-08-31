package org.example.service.authLogin.factory;

import java.util.Locale;
import java.util.Map;

public class FactoryFinder {
    final private Map<String, AuthLoginServiceFactory> factories;

    public FactoryFinder(Map<String, AuthLoginServiceFactory> factories) {
        this.factories = factories;
    }

    public AuthLoginServiceFactory getFactory(AuthLoginServiceFactory.AuthenticationType type) {
        String factoryBeanName = Character.toUpperCase(type.name().charAt(0)) + type.name().substring(1)  + "AuthLoginServiceFactory";

        AuthLoginServiceFactory factory = factories.get(factoryBeanName);

        if (factory == null) {
            throw new IllegalArgumentException("auth login factory not found: " + type);
        }

        return factory;
    }
}
