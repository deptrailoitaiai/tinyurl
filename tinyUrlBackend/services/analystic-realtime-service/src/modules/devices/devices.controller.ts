import { Controller, Get, Param, Query } from '@nestjs/common';
import { DevicesService } from './devices.service';
import { Device, DeviceType } from './entities/device.entity';

@Controller('devices')
export class DevicesController {
  constructor(private readonly devicesService: DevicesService) {}

  @Get()
  async getAllDevices(@Query('type') type?: DeviceType): Promise<Device[]> {
    if (type) {
      return this.devicesService.getDevicesByType(type);
    }
    return this.devicesService.getAllDevices();
  }

  @Get(':id')
  async getDeviceById(@Param('id') id: string): Promise<Device | null> {
    return this.devicesService.getDeviceById(+id);
  }
}