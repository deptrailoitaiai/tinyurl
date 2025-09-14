import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ClickEventsService } from './click-events.service';
import { ClickEventsController } from './click-events.controller';
import { ClickEvent } from './entities/click-event.entity';
import { Location } from '../locations/entities/location.entity';
import { Device } from '../devices/entities/device.entity';
import { ServiceReference } from '../../common/entities/service-reference.entity';

@Module({
  imports: [
    // Master database repositories
    TypeOrmModule.forFeature([
      ClickEvent,
      Location,
      Device,
      ServiceReference,
    ], 'master'),
    
    // Slave database repositories
    TypeOrmModule.forFeature([
      ClickEvent,
      Location,
      Device,
      ServiceReference,
    ], 'slave'),
  ],
  controllers: [ClickEventsController],
  providers: [ClickEventsService],
  exports: [ClickEventsService],
})
export class ClickEventsModule {}