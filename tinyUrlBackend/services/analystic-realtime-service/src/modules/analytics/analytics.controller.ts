import { Controller, Get, Query, Headers, BadRequestException } from '@nestjs/common';
import { AnalyticsService } from './analytics.service';

@Controller('analytics')
export class AnalyticsController {
  constructor(private readonly analyticsService: AnalyticsService) {}

  @Get('overview')
  async getAnalyticsOverview(
    @Query('urlId') urlId: string,
    @Headers('X-User-Id') userId?: string,
  ) {
    if (!urlId) {
      throw new BadRequestException('urlId query parameter is required');
    }
    
    if (!userId) {
      throw new BadRequestException('User ID header is required');
    }

    return this.analyticsService.getAnalyticsOverview(urlId, userId);
  }

  @Get('system')
  async getSystemStats() {
    return this.analyticsService.getSystemStats();
  }
}