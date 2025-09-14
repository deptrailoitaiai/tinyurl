import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn } from 'typeorm';

export enum DeviceType {
  DESKTOP = 'desktop',
  MOBILE = 'mobile',
  TABLET = 'tablet',
  UNKNOWN = 'unknown',
}

@Entity('devices')
export class Device {
  @PrimaryGeneratedColumn('increment', { type: 'bigint' })
  id: number;

  @Column({ 
    name: 'device_type',
    type: 'enum',
    enum: DeviceType,
    default: DeviceType.UNKNOWN
  })
  deviceType: DeviceType;

  @Column({ name: 'browser_name', type: 'varchar', length: 50, nullable: true })
  browserName: string;

  @Column({ name: 'os_name', type: 'varchar', length: 50, nullable: true })
  osName: string;
}