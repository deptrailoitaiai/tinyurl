import { Module, Global } from '@nestjs/common';
import { CacheService } from './services/cache.service';
import { KafkaService } from './services/kafka.service';
import { KafkaModule } from '../config/kafka.module';
import { RedisModule } from '../config/redis.module';

@Global()
@Module({
  imports: [KafkaModule, RedisModule],
  providers: [CacheService, KafkaService],
  exports: [CacheService, KafkaService],
})
export class CommonModule {}