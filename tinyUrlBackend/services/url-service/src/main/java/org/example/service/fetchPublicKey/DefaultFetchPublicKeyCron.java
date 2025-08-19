package org.example.service.fetchPublicKey;

import org.example.service.fetchPublicKey.FetchPublicKeyCron;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
public class DefaultFetchPublicKeyCron implements FetchPublicKeyCron {

    @Override
    @Scheduled(cron = "0 0 */6 * * *")
    public FetchPublicKeyCron fetchPublicKeyCron() {
        return null;
    }
}
