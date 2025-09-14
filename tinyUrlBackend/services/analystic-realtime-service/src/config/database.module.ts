import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ConfigModule, ConfigService } from '@nestjs/config';

import { ClickEvent } from '../modules/click-events/entities/click-event.entity';
import { Location } from '../modules/locations/entities/location.entity';
import { Device } from '../modules/devices/entities/device.entity';
import { ServiceReference } from '../common/entities/service-reference.entity';

@Module({
  imports: [
    // Master database configuration (for writes)
    TypeOrmModule.forRootAsync({
      name: 'master',
      imports: [ConfigModule],
      useFactory: (configService: ConfigService) => ({
        type: 'mysql',
        host: configService.get('DB_MASTER_HOST'),
        port: +configService.get('DB_MASTER_PORT'),
        username: configService.get('DB_MASTER_USERNAME'),
        password: configService.get('DB_MASTER_PASSWORD'),
        database: configService.get('DB_MASTER_DATABASE'),
        entities: [ClickEvent, Location, Device, ServiceReference],
        synchronize: false,
        timezone: 'Z',
        logging: process.env.NODE_ENV === 'development',
        extra: {
          charset: 'utf8mb4_unicode_ci',
        },
      }),
      inject: [ConfigService],
    }),

    // Slave database configuration (for reads)
    TypeOrmModule.forRootAsync({
      name: 'slave',
      imports: [ConfigModule],
      useFactory: (configService: ConfigService) => ({
        type: 'mysql',
        host: configService.get('DB_SLAVE_HOST'),
        port: +configService.get('DB_SLAVE_PORT'),
        username: configService.get('DB_SLAVE_USERNAME'),
        password: configService.get('DB_SLAVE_PASSWORD'),
        database: configService.get('DB_SLAVE_DATABASE'),
        entities: [ClickEvent, Location, Device, ServiceReference],
        synchronize: false,
        timezone: 'Z',
        logging: process.env.NODE_ENV === 'development',
        extra: {
          charset: 'utf8mb4_unicode_ci',
        },
      }),
      inject: [ConfigService],
    }),
  ],
})
export class DatabaseModule {}