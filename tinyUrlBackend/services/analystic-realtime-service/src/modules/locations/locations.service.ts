import { Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Location } from './entities/location.entity';

@Injectable()
export class LocationsService {
  constructor(
    @InjectRepository(Location, 'slave') private slaveLocationRepo: Repository<Location>,
  ) {}

  async getAllLocations(): Promise<Location[]> {
    return this.slaveLocationRepo.find({
      order: { countryName: 'ASC', city: 'ASC' },
    });
  }

  async getLocationById(id: number): Promise<Location | null> {
    return this.slaveLocationRepo.findOne({ where: { id } });
  }

  async getLocationsByCountry(countryCode: string): Promise<Location[]> {
    return this.slaveLocationRepo.find({
      where: { countryCode },
      order: { city: 'ASC' },
    });
  }
}