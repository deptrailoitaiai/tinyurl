import { Controller, Get, Param } from '@nestjs/common';
import { LocationsService } from './locations.service';
import { Location } from './entities/location.entity';

@Controller('locations')
export class LocationsController {
  constructor(private readonly locationsService: LocationsService) {}

  @Get()
  async getAllLocations(): Promise<Location[]> {
    return this.locationsService.getAllLocations();
  }

  @Get(':id')
  async getLocationById(@Param('id') id: string): Promise<Location | null> {
    return this.locationsService.getLocationById(+id);
  }

  @Get('country/:countryCode')
  async getLocationsByCountry(@Param('countryCode') countryCode: string): Promise<Location[]> {
    return this.locationsService.getLocationsByCountry(countryCode);
  }
}