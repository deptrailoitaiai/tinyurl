import { Module } from '@nestjs/common';
import { AnalyticsService } from './analytics.service';
import { AnalyticsController } from './analytics.controller';
import { ClickEventsModule } from '../click-events/click-events.module';
import { LocationsModule } from '../locations/locations.module';
import { DevicesModule } from '../devices/devices.module';
import { WebsocketModule } from '../websocket/websocket.module';

@Module({
  imports: [
    ClickEventsModule, 
    LocationsModule, 
    DevicesModule, 
    WebsocketModule,
  ],
  controllers: [AnalyticsController],
  providers: [AnalyticsService],
  exports: [AnalyticsService],
})
export class AnalyticsModule {}