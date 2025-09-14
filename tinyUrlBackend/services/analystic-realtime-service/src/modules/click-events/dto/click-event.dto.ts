import { IsOptional, IsString, IsIP, IsNumber, IsDateString } from 'class-validator';
import { DeviceType } from '../../devices/entities/device.entity';

export class CreateClickEventDto {
  @IsOptional()
  @IsString()
  urlId?: string; // URL ID từ URL service

  @IsOptional()
  @IsIP()
  ipAddress?: string;

  @IsOptional()
  @IsString()
  referrer?: string;

  @IsOptional()
  @IsString()
  countryCode?: string;

  @IsOptional()
  @IsString()
  countryName?: string;

  @IsOptional()
  @IsString()
  city?: string;

  @IsOptional()
  deviceType?: DeviceType;

  @IsOptional()
  @IsString()
  browserName?: string;

  @IsOptional()
  @IsString()
  osName?: string;
}

export class ClickEventResponseDto {
  id: number;
  ipAddress?: string;
  referrer?: string;
  deviceId?: number;
  locationId?: number;
  clickedAt: Date;
  processed: boolean;
  location?: {
    id: number;
    countryCode: string;
    countryName: string;
    city?: string;
  };
  device?: {
    id: number;
    deviceType: DeviceType;
    browserName?: string;
    osName?: string;
  };
}

export class ClickEventStatsDto {
  totalClicks: number;
  todayClicks: number;
  lastClickTime?: Date;
  topCountries: Array<{
    countryCode: string;
    countryName: string;
    count: number;
  }>;
  topDevices: Array<{
    deviceType: DeviceType;
    count: number;
  }>;
}