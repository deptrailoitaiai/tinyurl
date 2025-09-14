import {
  WebSocketGateway,
  WebSocketServer,
  SubscribeMessage,
  MessageBody,
  ConnectedSocket,
  OnGatewayInit,
  OnGatewayConnection,
  OnGatewayDisconnect,
} from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';

interface ClientInfo {
  userId?: string;
  urlIds: Set<string>;
}

@Injectable()
@WebSocketGateway({
  namespace: '/analytics',
  cors: {
    origin: '*',
    credentials: true,
  },
})
export class AnalyticsGateway implements OnGatewayInit, OnGatewayConnection, OnGatewayDisconnect {
  @WebSocketServer()
  server: Server;

  private readonly logger = new Logger(AnalyticsGateway.name);
  private clients = new Map<string, ClientInfo>();

  constructor(private configService: ConfigService) {}

  afterInit(server: Server) {
    this.logger.log('🔌 WebSocket Gateway initialized');
  }

  handleConnection(client: Socket) {
    const userId = client.handshake.headers['x-user-id'] as string;
    
    this.clients.set(client.id, {
      userId,
      urlIds: new Set(),
    });

    this.logger.log(`✅ Client connected: ${client.id}, User: ${userId || 'anonymous'}`);
  }

  handleDisconnect(client: Socket) {
    this.clients.delete(client.id);
    this.logger.log(`❌ Client disconnected: ${client.id}`);
  }

  @SubscribeMessage('subscribe-to-url')
  handleSubscribeToUrl(
    @MessageBody() data: { urlId: string },
    @ConnectedSocket() client: Socket,
  ) {
    const clientInfo = this.clients.get(client.id);
    if (!clientInfo) {
      client.emit('error', { message: 'Client not found' });
      return;
    }

    if (!data.urlId) {
      client.emit('error', { message: 'URL ID is required' });
      return;
    }

    // Add URL to client's subscription list
    clientInfo.urlIds.add(data.urlId);
    
    // Join room for this URL
    client.join(`url:${data.urlId}`);
    
    client.emit('subscribed', { 
      urlId: data.urlId,
      message: `Subscribed to real-time updates for URL ${data.urlId}` 
    });

    this.logger.log(`📡 Client ${client.id} subscribed to URL: ${data.urlId}`);
  }

  @SubscribeMessage('unsubscribe-from-url')
  handleUnsubscribeFromUrl(
    @MessageBody() data: { urlId: string },
    @ConnectedSocket() client: Socket,
  ) {
    const clientInfo = this.clients.get(client.id);
    if (!clientInfo) {
      client.emit('error', { message: 'Client not found' });
      return;
    }

    if (!data.urlId) {
      client.emit('error', { message: 'URL ID is required' });
      return;
    }

    // Remove URL from client's subscription list
    clientInfo.urlIds.delete(data.urlId);
    
    // Leave room for this URL
    client.leave(`url:${data.urlId}`);
    
    client.emit('unsubscribed', { 
      urlId: data.urlId,
      message: `Unsubscribed from URL ${data.urlId}` 
    });

    this.logger.log(`📡 Client ${client.id} unsubscribed from URL: ${data.urlId}`);
  }

  @SubscribeMessage('get-active-subscriptions')
  handleGetActiveSubscriptions(@ConnectedSocket() client: Socket) {
    const clientInfo = this.clients.get(client.id);
    if (!clientInfo) {
      client.emit('error', { message: 'Client not found' });
      return;
    }

    client.emit('active-subscriptions', {
      urlIds: Array.from(clientInfo.urlIds),
      count: clientInfo.urlIds.size,
    });
  }

  // Methods to be called by services to broadcast updates
  broadcastClickEvent(urlId: string, clickData: any) {
    this.server.to(`url:${urlId}`).emit('new-click', {
      urlId,
      event: 'click',
      data: clickData,
      timestamp: new Date().toISOString(),
    });

    this.logger.log(`📤 Broadcasted click event for URL: ${urlId}`);
  }

  broadcastStatsUpdate(urlId: string, stats: any) {
    this.server.to(`url:${urlId}`).emit('stats-update', {
      urlId,
      event: 'stats-update',
      data: stats,
      timestamp: new Date().toISOString(),
    });

    this.logger.log(`📤 Broadcasted stats update for URL: ${urlId}`);
  }

  broadcastLocationUpdate(urlId: string, locationData: any) {
    this.server.to(`url:${urlId}`).emit('location-update', {
      urlId,
      event: 'location-update',
      data: locationData,
      timestamp: new Date().toISOString(),
    });

    this.logger.log(`📤 Broadcasted location update for URL: ${urlId}`);
  }

  // Get connected clients count
  getConnectedClientsCount(): number {
    return this.clients.size;
  }

  // Get clients subscribed to a specific URL
  getUrlSubscribersCount(urlId: string): number {
    let count = 0;
    this.clients.forEach(client => {
      if (client.urlIds.has(urlId)) {
        count++;
      }
    });
    return count;
  }

  // Send message to specific user
  sendToUser(userId: string, event: string, data: any) {
    let sent = false;
    this.clients.forEach((clientInfo, clientId) => {
      if (clientInfo.userId === userId) {
        this.server.to(clientId).emit(event, data);
        sent = true;
      }
    });

    if (sent) {
      this.logger.log(`📤 Sent message to user ${userId}: ${event}`);
    } else {
      this.logger.warn(`⚠️ User ${userId} not connected`);
    }

    return sent;
  }
}