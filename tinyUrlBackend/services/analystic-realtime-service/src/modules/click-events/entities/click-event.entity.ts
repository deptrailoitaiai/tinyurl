import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, ManyToOne, JoinColumn } from 'typeorm';
import { Location } from '../../locations/entities/location.entity';
import { Device } from '../../devices/entities/device.entity';

@Entity('click_events')
export class ClickEvent {
  @PrimaryGeneratedColumn('increment', { type: 'bigint' })
  id: number;

  @Column({ name: 'ip_address', type: 'varchar', length: 45, nullable: true })
  ipAddress: string;

  @Column({ name: 'referrer', type: 'varchar', length: 500, nullable: true })
  referrer: string;

  @Column({ name: 'device_id', type: 'bigint', nullable: true })
  deviceId: number;

  @Column({ name: 'location_id', type: 'bigint', nullable: true })
  locationId: number;

  @CreateDateColumn({ name: 'clicked_at' })
  clickedAt: Date;

  @Column({ name: 'processed', type: 'boolean', default: false })
  processed: boolean;

  // Relations
  @ManyToOne(() => Location, { nullable: true })
  @JoinColumn({ name: 'location_id' })
  location?: Location;

  @ManyToOne(() => Device, { nullable: true })
  @JoinColumn({ name: 'device_id' })
  device?: Device;
}