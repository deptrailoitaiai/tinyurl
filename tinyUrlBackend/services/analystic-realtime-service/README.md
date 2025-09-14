# Analytics Real-time Service

Analytics Real-time Service là một microservice xử lý và cung cấp dữ liệu analytics theo thời gian thực cho hệ thống TinyURL.

## 🚀 Tính năng chính

### 📊 Real-time Analytics
- Tracking click events theo thời gian thực
- Thống kê vị trí địa lý (countries, cities)
- Phân tích thiết bị (device types, browsers, OS)
- Caching thông minh với Redis

### 🔌 WebSocket Support
- Real-time updates qua WebSocket
- Subscribe/unsubscribe to URL analytics
- Live notifications cho click events

### 🏗️ Architecture
- **Database**: MySQL Master-Slave configuration
- **Cache**: Redis với TTL strategies
- **Message Queue**: Kafka integration
- **Framework**: NestJS với TypeScript

## 📡 API Endpoints

### REST APIs

#### Click Events
```
GET /api/v1/click-events?urlId={urlId}&limit={limit}
Headers: X-User-Id: {userId}
```

```
POST /api/v1/click-events
Body: {
  "urlId": "string",
  "ipAddress": "string",
  "referrer": "string",
  "countryCode": "string",
  "countryName": "string",
  "city": "string",
  "deviceType": "desktop|mobile|tablet|unknown",
  "browserName": "string",
  "osName": "string"
}
```

```
GET /api/v1/click-events/stats?urlId={urlId}
Headers: X-User-Id: {userId}
```

#### Analytics Overview
```
GET /api/v1/analytics/overview?urlId={urlId}
Headers: X-User-Id: {userId}
```

#### System Stats
```
GET /api/v1/analytics/system
```

#### Locations & Devices
```
GET /api/v1/locations
GET /api/v1/locations/{id}
GET /api/v1/locations/country/{countryCode}

GET /api/v1/devices
GET /api/v1/devices/{id}
GET /api/v1/devices?type={deviceType}
```

### WebSocket Events

#### Connection
```
Connect to: ws://localhost:8082/analytics
Headers: x-user-id: {userId}
```

#### Subscribe to URL Analytics
```javascript
// Subscribe
socket.emit('subscribe-to-url', { urlId: 'abc123' });

// Unsubscribe  
socket.emit('unsubscribe-from-url', { urlId: 'abc123' });

// Get active subscriptions
socket.emit('get-active-subscriptions');
```

#### Receive Real-time Events
```javascript
// New click event
socket.on('new-click', (data) => {
  console.log('New click:', data);
});

// Stats update
socket.on('stats-update', (data) => {
  console.log('Stats updated:', data);
});

// Location update
socket.on('location-update', (data) => {
  console.log('Location data:', data);
});
```

## 🔧 Configuration

### Environment Variables

```bash
# Server
NODE_ENV=development
PORT=8082

# Database Master (Write operations)
DB_MASTER_HOST=localhost
DB_MASTER_PORT=3307
DB_MASTER_USERNAME=root
DB_MASTER_PASSWORD=root123
DB_MASTER_DATABASE=analytics_realtime_service

# Database Slave (Read operations)
DB_SLAVE_HOST=localhost
DB_SLAVE_PORT=3308
DB_SLAVE_USERNAME=root
DB_SLAVE_PASSWORD=root123
DB_SLAVE_DATABASE=analytics_realtime_service

# Redis Cache
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DATABASE=2
REDIS_TTL=300

# Kafka
KAFKA_BROKERS=localhost:9091,localhost:9092,localhost:9093
KAFKA_CLIENT_ID=analytics-realtime-service
KAFKA_GROUP_ID=analytics-realtime-group

# External Services
URL_SERVICE_BASE_URL=http://localhost:8081
BATCH_SERVICE_BASE_URL=http://localhost:8083

# Cache TTL (seconds)
CACHE_LOCATION_TTL=3600
CACHE_DEVICE_TTL=3600
CACHE_CLICK_STATS_TTL=60
CACHE_USER_URLS_TTL=300
```

## 🗃️ Database Schema

### Tables
- `click_events` - Lưu trữ sự kiện click
- `locations` - Thông tin vị trí địa lý
- `devices` - Thông tin thiết bị
- `service_references` - Cross-service references

### Master-Slave Strategy
- **Master DB**: Tất cả DML operations (INSERT, UPDATE, DELETE)
- **Slave DB**: Read operations cho performance
- **Special case**: Read operations cần cho DML sẽ query từ Master

## 🚀 Development

### Installation
```bash
cd tinyUrlBackend/services/analystic-realtime-service
npm install
```

### Development Server
```bash
npm run start:dev
```

### Build
```bash
npm run build
```

### Production
```bash
npm run start:prod
```

## 🐳 Docker

### Build Image
```bash
docker build -t analytics-realtime-service .
```

### Run with Docker Compose
```bash
docker-compose up analytics-realtime-service
```

## 📊 Caching Strategy

### Cache Keys & TTL
- **Locations**: `location:{countryCode}:{city}` - TTL: 3600s
- **Devices**: `device:{type}:{browser}:{os}` - TTL: 3600s  
- **Click Stats**: `click_stats:{urlId}` - TTL: 60s
- **Today Clicks**: `today_clicks:{urlId}:{date}` - TTL: 60s
- **URL Ownership**: `url_owner:{urlId}` - TTL: 300s

### Cache Invalidation
- Auto-invalidation khi có click mới
- Manual invalidation cho critical updates

## 🔄 Kafka Integration

### Topics Produced
- `analytics.batch.click.data` - Gửi data cho batch service
- `analytics.realtime.update` - Real-time updates
- `analytics.websocket.event` - WebSocket events

### Topics Consumed  
- `url.click.event` - Nhận click events từ URL service
- `url.ownership.check` - Verify URL ownership

## 🔐 Authorization

Service verify quyền sở hữu URL thông qua:
1. Cache lookup (url_owner:{urlId})
2. Kafka request tới URL service
3. Header `X-User-Id` từ API Gateway

## 📈 Performance Features

- **Connection pooling** với TypeORM
- **Redis caching** cho hot data
- **Master-Slave** read scaling
- **Kafka async processing**
- **WebSocket real-time** updates
- **Limited results** (max 10 click events)

## 🔍 Monitoring

### Health Check
```
GET /api/v1/analytics/system
```

### Metrics
- Connected WebSocket clients
- Cache hit rates  
- Database connection status
- Kafka producer/consumer lag

## 🛠️ Troubleshooting

### Common Issues
1. **Database connection**: Kiểm tra master/slave connectivity
2. **Redis connection**: Verify Redis host và port
3. **Kafka issues**: Check broker connectivity
4. **WebSocket problems**: CORS và authentication headers
5. **Cache misses**: TTL configuration và memory limits