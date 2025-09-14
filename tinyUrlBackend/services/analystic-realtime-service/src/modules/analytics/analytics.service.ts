import { Injectable } from '@nestjs/common';
import { ClickEventsService } from '../click-events/click-events.service';
import { LocationsService } from '../locations/locations.service';
import { DevicesService } from '../devices/devices.service';
import { AnalyticsGateway } from '../websocket/analytics.gateway';
import { CacheService } from '../../common/services/cache.service';

@Injectable()
export class AnalyticsService {
  constructor(
    private clickEventsService: ClickEventsService,
    private locationsService: LocationsService,
    private devicesService: DevicesService,
    private analyticsGateway: AnalyticsGateway,
    private cacheService: CacheService,
  ) {}

  async getAnalyticsOverview(urlId: string, userId: string) {
    const [clickStats, recentClicks] = await Promise.all([
      this.clickEventsService.getClickStats(urlId, userId),
      this.clickEventsService.getClickEvents(urlId, userId, 5),
    ]);

    return {
      stats: clickStats,
      recentClicks,
      connectedClients: this.analyticsGateway.getUrlSubscribersCount(urlId),
      lastUpdated: new Date().toISOString(),
    };
  }

  async getSystemStats() {
    // Return static data for testing without database
    if (process.env.NODE_ENV === 'development') {
      return {
        totalLocations: 5,
        totalDevices: 10,
        connectedClients: this.analyticsGateway.getConnectedClientsCount(),
        timestamp: new Date().toISOString(),
        status: 'running',
        version: '1.0.0'
      };
    }

    const [totalLocations, totalDevices] = await Promise.all([
      this.locationsService.getAllLocations(),
      this.devicesService.getAllDevices(),
    ]);

    return {
      totalLocations: totalLocations.length,
      totalDevices: totalDevices.length,
      connectedClients: this.analyticsGateway.getConnectedClientsCount(),
      timestamp: new Date().toISOString(),
    };
  }

  // Method to handle real-time events and broadcast via WebSocket
  async handleRealTimeEvent(urlId: string, eventType: string, data: any) {
    switch (eventType) {
      case 'new-click':
        this.analyticsGateway.broadcastClickEvent(urlId, data);
        break;
      case 'stats-update':
        this.analyticsGateway.broadcastStatsUpdate(urlId, data);
        break;
      case 'location-update':
        this.analyticsGateway.broadcastLocationUpdate(urlId, data);
        break;
      default:
        console.warn(`Unknown event type: ${eventType}`);
    }
  }
}