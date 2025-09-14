import { Injectable, Inject, OnModuleInit, OnModuleDestroy } from '@nestjs/common';
import { ClientKafka } from '@nestjs/microservices';
import { ConfigService } from '@nestjs/config';
import { firstValueFrom } from 'rxjs';

export interface ClickEventMessage {
  urlId: string;
  userId?: string;
  ipAddress?: string;
  referrer?: string;
  userAgent?: string;
  timestamp: string;
}

export interface BatchServiceRequest {
  urlId: string;
  date: string; // YYYY-MM-DD format
  totalClicksToday: number;
  lastClickTime: string;
}

@Injectable()
export class KafkaService implements OnModuleInit, OnModuleDestroy {
  constructor(
    @Inject('KAFKA_SERVICE') private kafkaClient: ClientKafka,
    private configService: ConfigService,
  ) {}

  async onModuleInit() {
    // Skip Kafka connection in development mode if brokers are not available
    if (process.env.NODE_ENV === 'development') {
      console.log('⚠️  Kafka disabled in development mode');
      return;
    }

    try {
      // Subscribe to topics that this service will consume
      this.kafkaClient.subscribeToResponseOf('url.click.event');
      this.kafkaClient.subscribeToResponseOf('url.authorization.check');
      
      await this.kafkaClient.connect();
    } catch (error) {
      console.error('❌ Failed to connect to Kafka:', error.message);
      console.log('🔄 Service will continue without Kafka...');
    }
  }

  async onModuleDestroy() {
    await this.kafkaClient.close();
  }

  // Send click event data to batch service
  async sendClickDataToBatchService(data: BatchServiceRequest): Promise<void> {
    try {
      const pattern = 'analytics.batch.click.data';
      await firstValueFrom(
        this.kafkaClient.emit(pattern, {
          key: data.urlId,
          value: JSON.stringify(data),
          headers: {
            'service': 'analytics-realtime',
            'timestamp': new Date().toISOString(),
          },
        })
      );
      console.log(`📤 Sent click data to batch service for URL: ${data.urlId}`);
    } catch (error) {
      console.error('❌ Failed to send click data to batch service:', error);
      throw error;
    }
  }

  // Request URL ownership verification from URL service
  async checkUrlAuthorization(userId: string, shortUrl: string): Promise<boolean> {
    try {
      const response = await this.kafkaClient.send('url.authorization.check', {
        userId,
        shortUrl
      });
      return (response as any)?.isOwner || false;
    } catch (error) {
      console.error('Failed to check URL authorization:', error);
      return false;
    }
  }

  // Send real-time analytics update
  async sendRealTimeUpdate(urlId: string, updateData: any): Promise<void> {
    try {
      const pattern = 'analytics.realtime.update';
      await firstValueFrom(
        this.kafkaClient.emit(pattern, {
          key: urlId,
          value: JSON.stringify({
            urlId,
            timestamp: new Date().toISOString(),
            data: updateData,
          }),
        })
      );
    } catch (error) {
      console.error('❌ Failed to send real-time update:', error);
    }
  }

  // Handle incoming click events from URL service
  async handleClickEvent(message: ClickEventMessage): Promise<void> {
    try {
      console.log(`📥 Received click event for URL: ${message.urlId}`);
      // This will be handled by the ClickEventsService
      // Just acknowledge the message here
    } catch (error) {
      console.error('❌ Failed to handle click event:', error);
      throw error;
    }
  }

  // Publish event for WebSocket notifications
  async publishWebSocketEvent(urlId: string, eventType: string, data: any): Promise<void> {
    try {
      const pattern = 'analytics.websocket.event';
      await firstValueFrom(
        this.kafkaClient.emit(pattern, {
          key: urlId,
          value: JSON.stringify({
            urlId,
            eventType,
            data,
            timestamp: new Date().toISOString(),
          }),
        })
      );
    } catch (error) {
      console.error('❌ Failed to publish WebSocket event:', error);
    }
  }
}