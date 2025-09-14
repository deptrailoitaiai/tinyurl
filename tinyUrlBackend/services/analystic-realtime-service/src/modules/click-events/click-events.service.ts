import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository, DataSource } from 'typeorm';
import { ClickEvent } from './entities/click-event.entity';
import { Location } from '../locations/entities/location.entity';
import { Device, DeviceType } from '../devices/entities/device.entity';
import { ServiceReference } from '../../common/entities/service-reference.entity';
import { CacheService } from '../../common/services/cache.service';
import { KafkaService } from '../../common/services/kafka.service';
import { CreateClickEventDto, ClickEventResponseDto, ClickEventStatsDto } from './dto/click-event.dto';

@Injectable()
export class ClickEventsService {
  constructor(
    @InjectRepository(ClickEvent, 'master') private masterClickEventRepo: Repository<ClickEvent>,
    @InjectRepository(ClickEvent, 'slave') private slaveClickEventRepo: Repository<ClickEvent>,
    @InjectRepository(Location, 'master') private masterLocationRepo: Repository<Location>,
    @InjectRepository(Location, 'slave') private slaveLocationRepo: Repository<Location>,
    @InjectRepository(Device, 'master') private masterDeviceRepo: Repository<Device>,
    @InjectRepository(Device, 'slave') private slaveDeviceRepo: Repository<Device>,
    @InjectRepository(ServiceReference, 'master') private masterServiceRefRepo: Repository<ServiceReference>,
    @InjectRepository(ServiceReference, 'slave') private slaveServiceRefRepo: Repository<ServiceReference>,
    private cacheService: CacheService,
    private kafkaService: KafkaService,
  ) {}

  async createClickEvent(dto: CreateClickEventDto): Promise<ClickEventResponseDto> {
    // Get or create location
    const locationId = await this.getOrCreateLocation(dto.countryCode, dto.countryName, dto.city);
    
    // Get or create device
    const deviceId = await this.getOrCreateDevice(dto.deviceType, dto.browserName, dto.osName);

    // Create click event
    const clickEvent = this.masterClickEventRepo.create({
      ipAddress: dto.ipAddress,
      referrer: dto.referrer,
      locationId,
      deviceId,
    });

    const savedEvent = await this.masterClickEventRepo.save(clickEvent);

    // Create service reference if urlId provided
    if (dto.urlId) {
      await this.createServiceReference(savedEvent.id, dto.urlId);
      
      // Update cache and send to batch service
      await this.updateAnalytics(dto.urlId);
    }

    return this.mapToResponseDto(savedEvent);
  }

  async getClickEvents(urlId: string, userId: string, limit: number = 10): Promise<ClickEventResponseDto[]> {
    // Verify URL ownership first
    const hasAccess = await this.verifyUrlAccess(urlId, userId);
    if (!hasAccess) {
      throw new Error('Access denied: User does not own this URL');
    }

    // Get click event IDs from service references
    const serviceRefs = await this.slaveServiceRefRepo.find({
      where: {
        targetId: parseInt(urlId),
        targetTable: 'urls',
        localTable: 'click_events',
      },
      take: limit,
      order: { id: 'DESC' },
    });

    if (serviceRefs.length === 0) {
      return [];
    }

    const clickEventIds = serviceRefs.map(ref => ref.localId);

    // Get click events with relations
    const clickEvents = await this.slaveClickEventRepo.find({
      where: { id: { $in: clickEventIds } as any },
      relations: ['location', 'device'],
      order: { id: 'DESC' },
    });

    return clickEvents.map(event => this.mapToResponseDto(event));
  }

  async getClickStats(urlId: string, userId: string): Promise<ClickEventStatsDto> {
    // Verify URL ownership first
    const hasAccess = await this.verifyUrlAccess(urlId, userId);
    if (!hasAccess) {
      throw new Error('Access denied: User does not own this URL');
    }

    // Check cache first
    const cachedStats = await this.cacheService.getCachedClickStats(urlId);
    if (cachedStats) {
      return cachedStats;
    }

    // Get today's date in UTC
    const today = new Date().toISOString().split('T')[0];
    
    // Get total clicks from service references
    const totalClicks = await this.slaveServiceRefRepo.count({
      where: {
        targetId: parseInt(urlId),
        targetTable: 'urls',
        localTable: 'click_events',
      },
    });

    // Get today's clicks count
    const todayClicks = await this.getCachedTodayClicks(urlId, today);

    // Get last click time
    const lastClick = await this.slaveServiceRefRepo.findOne({
      where: {
        targetId: parseInt(urlId),
        targetTable: 'urls',
        localTable: 'click_events',
      },
      order: { id: 'DESC' },
      relations: ['localTable'],
    });

    let lastClickTime: Date | undefined;
    if (lastClick) {
      const clickEvent = await this.slaveClickEventRepo.findOne({
        where: { id: lastClick.localId },
      });
      lastClickTime = clickEvent?.clickedAt;
    }

    // Get top countries and devices (simplified for now)
    const topCountries = await this.getTopCountries(urlId);
    const topDevices = await this.getTopDevices(urlId);

    const stats: ClickEventStatsDto = {
      totalClicks,
      todayClicks,
      lastClickTime,
      topCountries,
      topDevices,
    };

    // Cache the stats
    await this.cacheService.cacheClickStats(urlId, stats);

    return stats;
  }

  private async getOrCreateLocation(countryCode?: string, countryName?: string, city?: string): Promise<number | undefined> {
    if (!countryCode || !countryName) {
      return undefined;
    }

    // Check cache first
    const cachedLocationId = await this.cacheService.getCachedLocation(countryCode, city);
    if (cachedLocationId) {
      return cachedLocationId;
    }

    // Try to find existing location
    const existingLocation = await this.slaveLocationRepo.findOne({
      where: { countryCode, city: city || null },
    });

    if (existingLocation) {
      await this.cacheService.cacheLocation(countryCode, city, existingLocation.id);
      return existingLocation.id;
    }

    // Create new location
    const newLocation = this.masterLocationRepo.create({
      countryCode,
      countryName,
      city,
    });

    const savedLocation = await this.masterLocationRepo.save(newLocation);
    await this.cacheService.cacheLocation(countryCode, city, savedLocation.id);

    return savedLocation.id;
  }

  private async getOrCreateDevice(deviceType?: DeviceType, browserName?: string, osName?: string): Promise<number | undefined> {
    if (!deviceType) {
      return undefined;
    }

    // Check cache first
    const cachedDeviceId = await this.cacheService.getCachedDevice(deviceType, browserName, osName);
    if (cachedDeviceId) {
      return cachedDeviceId;
    }

    // Try to find existing device
    const existingDevice = await this.slaveDeviceRepo.findOne({
      where: { 
        deviceType, 
        browserName: browserName || null,
        osName: osName || null,
      },
    });

    if (existingDevice) {
      await this.cacheService.cacheDevice(deviceType, browserName, osName, existingDevice.id);
      return existingDevice.id;
    }

    // Create new device
    const newDevice = this.masterDeviceRepo.create({
      deviceType,
      browserName,
      osName,
    });

    const savedDevice = await this.masterDeviceRepo.save(newDevice);
    await this.cacheService.cacheDevice(deviceType, browserName, osName, savedDevice.id);

    return savedDevice.id;
  }

  private async createServiceReference(clickEventId: number, urlId: string): Promise<void> {
    const serviceRef = this.masterServiceRefRepo.create({
      localId: clickEventId,
      localTable: 'click_events',
      targetId: parseInt(urlId),
      targetTable: 'urls',
    });

    await this.masterServiceRefRepo.save(serviceRef);
  }

  private async verifyUrlAccess(urlId: string, userId: string): Promise<boolean> {
    // Check cache first
    const cachedOwner = await this.cacheService.getCachedUrlOwner(urlId);
    if (cachedOwner) {
      return cachedOwner === userId;
    }

    // Check via Kafka (URL service)
    const hasAccess = await this.kafkaService.checkUrlAuthorization(userId, urlId);
    
    if (hasAccess) {
      await this.cacheService.cacheUrlOwner(urlId, userId);
    }

    return hasAccess;
  }

  private async updateAnalytics(urlId: string): Promise<void> {
    const today = new Date().toISOString().split('T')[0];
    
    // Increment today's clicks count
    const todayClicks = await this.cacheService.incrementTodayClicks(urlId, today);
    
    // Send data to batch service
    await this.kafkaService.sendClickDataToBatchService({
      urlId,
      date: today,
      totalClicksToday: todayClicks,
      lastClickTime: new Date().toISOString(),
    });

    // Invalidate cache
    await this.cacheService.invalidateClickStats(urlId);
  }

  private async getCachedTodayClicks(urlId: string, date: string): Promise<number> {
    return await this.cacheService.getCachedTodayClicks(urlId, date) || 0;
  }

  private async getTopCountries(urlId: string): Promise<Array<{countryCode: string, countryName: string, count: number}>> {
    // Simplified implementation - in production, you'd want more complex aggregation
    return [];
  }

  private async getTopDevices(urlId: string): Promise<Array<{deviceType: DeviceType, count: number}>> {
    // Simplified implementation - in production, you'd want more complex aggregation
    return [];
  }

  private mapToResponseDto(clickEvent: ClickEvent): ClickEventResponseDto {
    return {
      id: clickEvent.id,
      ipAddress: clickEvent.ipAddress,
      referrer: clickEvent.referrer,
      deviceId: clickEvent.deviceId,
      locationId: clickEvent.locationId,
      clickedAt: clickEvent.clickedAt,
      processed: clickEvent.processed,
      location: clickEvent.location ? {
        id: clickEvent.location.id,
        countryCode: clickEvent.location.countryCode,
        countryName: clickEvent.location.countryName,
        city: clickEvent.location.city,
      } : undefined,
      device: clickEvent.device ? {
        id: clickEvent.device.id,
        deviceType: clickEvent.device.deviceType,
        browserName: clickEvent.device.browserName,
        osName: clickEvent.device.osName,
      } : undefined,
    };
  }
}