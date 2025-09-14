import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Device, DeviceType } from './entities/device.entity';

@Injectable()
export class DevicesService {
  constructor(
    @InjectRepository(Device, 'slave') private slaveDeviceRepo: Repository<Device>,
  ) {}

  async getAllDevices(): Promise<Device[]> {
    return this.slaveDeviceRepo.find({
      order: { deviceType: 'ASC', browserName: 'ASC' },
    });
  }

  async getDeviceById(id: number): Promise<Device | null> {
    return this.slaveDeviceRepo.findOne({ where: { id } });
  }

  async getDevicesByType(deviceType: DeviceType): Promise<Device[]> {
    return this.slaveDeviceRepo.find({
      where: { deviceType },
      order: { browserName: 'ASC' },
    });
  }
}