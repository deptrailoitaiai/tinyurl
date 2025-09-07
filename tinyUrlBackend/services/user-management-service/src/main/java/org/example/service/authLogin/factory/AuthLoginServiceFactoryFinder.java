package org.example.service.authLogin.factory;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuthLoginServiceFactoryFinder {
    final private Map<String, AuthLoginServiceFactory> factories;

    public AuthLoginServiceFactoryFinder(Map<String, AuthLoginServiceFactory> factories) {
        this.factories = factories;
    }

    public AuthLoginServiceFactory getFactory(AuthenticationType type) {
        String factoryBeanName = Character.toUpperCase(type.name().charAt(0)) + type.name().substring(1)  + "AuthLoginServiceFactory";

        AuthLoginServiceFactory factory = factories.get(factoryBeanName);

        if (factory == null) {
            throw new IllegalArgumentException("auth login factory not found: " + type);
        }

        return factory;
    }

    public enum AuthenticationType {
        PASSWORD, GOOGLE, FACEBOOK
    }
}
