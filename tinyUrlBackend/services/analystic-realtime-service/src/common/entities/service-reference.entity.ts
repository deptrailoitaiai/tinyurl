import { Entity, PrimaryGeneratedColumn, Column, CreateDateColumn, UpdateDateColumn } from 'typeorm';

@Entity('service_references')
export class ServiceReference {
  @PrimaryGeneratedColumn('increment', { type: 'bigint' })
  id: number;

  @Column({ name: 'local_id', type: 'bigint' })
  localId: number;

  @Column({ name: 'local_table', type: 'varchar', length: 100 })
  localTable: string;

  @Column({ name: 'target_id', type: 'bigint' })
  targetId: number;

  @Column({ name: 'target_table', type: 'varchar', length: 100 })
  targetTable: string;
}