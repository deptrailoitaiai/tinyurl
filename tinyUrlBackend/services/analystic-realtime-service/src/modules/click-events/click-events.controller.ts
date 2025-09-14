import { Controller, Get, Post, Body, Query, Headers, UseGuards, BadRequestException } from '@nestjs/common';
import { ClickEventsService } from './click-events.service';
import { CreateClickEventDto, ClickEventResponseDto, ClickEventStatsDto } from './dto/click-event.dto';

@Controller('click-events')
export class ClickEventsController {
  constructor(private readonly clickEventsService: ClickEventsService) {}

  @Post()
  async createClickEvent(
    @Body() createClickEventDto: CreateClickEventDto,
    @Headers('X-User-Id') userId?: string,
  ): Promise<ClickEventResponseDto> {
    return this.clickEventsService.createClickEvent(createClickEventDto);
  }

  @Get()
  async getClickEvents(
    @Query('urlId') urlId: string,
    @Query('limit') limit: string = '10',
    @Headers('X-User-Id') userId?: string,
  ): Promise<ClickEventResponseDto[]> {
    if (!urlId) {
      throw new BadRequestException('urlId query parameter is required');
    }
    
    if (!userId) {
      throw new BadRequestException('User ID header is required');
    }

    const limitNum = parseInt(limit, 10);
    if (isNaN(limitNum) || limitNum > 10) {
      throw new BadRequestException('Limit must be a number and max 10');
    }

    return this.clickEventsService.getClickEvents(urlId, userId, limitNum);
  }

  @Get('stats')
  async getClickStats(
    @Query('urlId') urlId: string,
    @Headers('X-User-Id') userId?: string,
  ): Promise<ClickEventStatsDto> {
    if (!urlId) {
      throw new BadRequestException('urlId query parameter is required');
    }
    
    if (!userId) {
      throw new BadRequestException('User ID header is required');
    }

    return this.clickEventsService.getClickStats(urlId, userId);
  }
}