import { Injectable, Inject } from '@nestjs/common';
import { CACHE_MANAGER } from '@nestjs/cache-manager';
import { Cache } from 'cache-manager';
import { ConfigService } from '@nestjs/config';

@Injectable()
export class CacheService {
  constructor(
    @Inject(CACHE_MANAGER) private cacheManager: Cache,
    private configService: ConfigService,
  ) {}

  // Cache keys constants
  private readonly KEYS = {
    LOCATION: (countryCode: string, city?: string) => 
      `location:${countryCode}${city ? ':' + city : ''}`,
    DEVICE: (deviceType: string, browser?: string, os?: string) => 
      `device:${deviceType}:${browser || 'unknown'}:${os || 'unknown'}`,
    CLICK_STATS: (urlId: string) => `click_stats:${urlId}`,
    TODAY_CLICKS: (urlId: string, date: string) => `today_clicks:${urlId}:${date}`,
    USER_URLS: (userId: string) => `user_urls:${userId}`,
    URL_OWNER: (urlId: string) => `url_owner:${urlId}`,
  };

  // TTL configurations
  private readonly TTL = {
    LOCATION: +this.configService.get('CACHE_LOCATION_TTL', 3600),
    DEVICE: +this.configService.get('CACHE_DEVICE_TTL', 3600),
    CLICK_STATS: +this.configService.get('CACHE_CLICK_STATS_TTL', 60),
    USER_URLS: +this.configService.get('CACHE_USER_URLS_TTL', 300),
  };

  // Location caching
  async cacheLocation(countryCode: string, city: string | undefined, locationId: number): Promise<void> {
    const key = this.KEYS.LOCATION(countryCode, city);
    await this.cacheManager.set(key, locationId, this.TTL.LOCATION * 1000);
  }

  async getCachedLocation(countryCode: string, city?: string): Promise<number | null> {
    const key = this.KEYS.LOCATION(countryCode, city);
    return await this.cacheManager.get<number>(key);
  }

  // Device caching
  async cacheDevice(deviceType: string, browser: string | undefined, os: string | undefined, deviceId: number): Promise<void> {
    const key = this.KEYS.DEVICE(deviceType, browser, os);
    await this.cacheManager.set(key, deviceId, this.TTL.DEVICE * 1000);
  }

  async getCachedDevice(deviceType: string, browser?: string, os?: string): Promise<number | null> {
    const key = this.KEYS.DEVICE(deviceType, browser, os);
    return await this.cacheManager.get<number>(key);
  }

  // Click stats caching
  async cacheClickStats(urlId: string, stats: any): Promise<void> {
    const key = this.KEYS.CLICK_STATS(urlId);
    await this.cacheManager.set(key, stats, this.TTL.CLICK_STATS * 1000);
  }

  async getCachedClickStats(urlId: string): Promise<any | null> {
    const key = this.KEYS.CLICK_STATS(urlId);
    return await this.cacheManager.get(key);
  }

  // Today's clicks count caching
  async cacheTodayClicks(urlId: string, date: string, count: number): Promise<void> {
    const key = this.KEYS.TODAY_CLICKS(urlId, date);
    await this.cacheManager.set(key, count, this.TTL.CLICK_STATS * 1000);
  }

  async getCachedTodayClicks(urlId: string, date: string): Promise<number | null> {
    const key = this.KEYS.TODAY_CLICKS(urlId, date);
    return await this.cacheManager.get<number>(key);
  }

  async incrementTodayClicks(urlId: string, date: string): Promise<number> {
    const key = this.KEYS.TODAY_CLICKS(urlId, date);
    const current = await this.cacheManager.get<number>(key) || 0;
    const newCount = current + 1;
    await this.cacheManager.set(key, newCount, this.TTL.CLICK_STATS * 1000);
    return newCount;
  }

  // User URLs caching
  async cacheUserUrls(userId: string, urlIds: string[]): Promise<void> {
    const key = this.KEYS.USER_URLS(userId);
    await this.cacheManager.set(key, urlIds, this.TTL.USER_URLS * 1000);
  }

  async getCachedUserUrls(userId: string): Promise<string[] | null> {
    const key = this.KEYS.USER_URLS(userId);
    return await this.cacheManager.get<string[]>(key);
  }

  // URL ownership caching
  async cacheUrlOwner(urlId: string, userId: string): Promise<void> {
    const key = this.KEYS.URL_OWNER(urlId);
    await this.cacheManager.set(key, userId, this.TTL.USER_URLS * 1000);
  }

  async getCachedUrlOwner(urlId: string): Promise<string | null> {
    const key = this.KEYS.URL_OWNER(urlId);
    return await this.cacheManager.get<string>(key);
  }

  // Cache invalidation
  async invalidateClickStats(urlId: string): Promise<void> {
    const key = this.KEYS.CLICK_STATS(urlId);
    await this.cacheManager.del(key);
  }

  async invalidateUserUrls(userId: string): Promise<void> {
    const key = this.KEYS.USER_URLS(userId);
    await this.cacheManager.del(key);
  }

  // Utility methods
  async clearCache(): Promise<void> {
    await this.cacheManager.reset();
  }

  async deleteKey(key: string): Promise<void> {
    await this.cacheManager.del(key);
  }
}