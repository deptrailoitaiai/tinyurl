package org.example.service.impl;

import org.example.service.FetchPublicKeyCron;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
public class FetchPublicKeyCronImpl implements FetchPublicKeyCron {

    @Override
    @Scheduled(cron = "0 0 */6 * * *")
    public FetchPublicKeyCron fetchPublicKeyCron() {
        return null;
    }
}
