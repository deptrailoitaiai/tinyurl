import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { CacheModule } from '@nestjs/cache-manager';
import { ScheduleModule } from '@nestjs/schedule';

import { DatabaseModule } from './config/database.module';
import { RedisModule } from './config/redis.module';
import { KafkaModule } from './config/kafka.module';
import { CommonModule } from './common/common.module';

import { AnalyticsModule } from './modules/analytics/analytics.module';
import { ClickEventsModule } from './modules/click-events/click-events.module';
import { LocationsModule } from './modules/locations/locations.module';
import { DevicesModule } from './modules/devices/devices.module';
import { WebsocketModule } from './modules/websocket/websocket.module';

@Module({
  imports: [
    // Configuration
    ConfigModule.forRoot({
      isGlobal: true,
      envFilePath: '.env',
    }),
    
    // Schedule module for cron jobs
    ScheduleModule.forRoot(),

    // Database
    DatabaseModule,
    
    // Cache
    RedisModule,
    
    // Kafka
    KafkaModule,
    
    // Common services (global)
    CommonModule,
    
    // Business modules
    AnalyticsModule,
    ClickEventsModule,
    LocationsModule,
    DevicesModule,
    WebsocketModule,
  ],
  controllers: [],
  providers: [],
})
export class AppModule {}