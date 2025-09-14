#!/bin/bash

# Analytics Real-time Service Test Script
echo "🧪 Testing Analytics Real-time Service..."

BASE_URL="http://localhost:8082/api/v1"
USER_ID="test-user-123"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test functions
test_endpoint() {
    local endpoint=$1
    local expected_status=$2
    local description=$3
    local headers=$4
    
    echo -n "Testing $description... "
    
    if [ -n "$headers" ]; then
        response=$(curl -s -w "%{http_code}" -H "$headers" "$BASE_URL$endpoint")
    else
        response=$(curl -s -w "%{http_code}" "$BASE_URL$endpoint")
    fi
    
    status_code=${response: -3}
    body=${response%???}
    
    if [ "$status_code" -eq "$expected_status" ]; then
        echo -e "${GREEN}✓ PASS${NC} (Status: $status_code)"
        return 0
    else
        echo -e "${RED}✗ FAIL${NC} (Status: $status_code, Expected: $expected_status)"
        echo "Response: $body"
        return 1
    fi
}

echo "🔧 Starting tests..."
echo

# Test system health
test_endpoint "/analytics/system" 200 "System health check"

# Test locations endpoint
test_endpoint "/locations" 200 "Get all locations"

# Test devices endpoint  
test_endpoint "/devices" 200 "Get all devices"

# Test click events without required headers (should fail)
test_endpoint "/click-events?urlId=test123" 400 "Click events without user header (should fail)"

# Test click events with headers
test_endpoint "/click-events?urlId=test123&limit=5" 200 "Click events with user header" "X-User-Id: $USER_ID"

# Test click stats
test_endpoint "/click-events/stats?urlId=test123" 200 "Click stats with user header" "X-User-Id: $USER_ID"

# Test analytics overview
test_endpoint "/analytics/overview?urlId=test123" 200 "Analytics overview" "X-User-Id: $USER_ID"

echo
echo "🧪 Basic API tests completed!"
echo

# WebSocket test (basic connectivity)
echo "🔌 Testing WebSocket connectivity..."

# Simple WebSocket test using wscat if available
if command -v wscat &> /dev/null; then
    echo "Testing WebSocket connection..."
    timeout 5 wscat -c ws://localhost:8082/analytics -H "x-user-id: $USER_ID" -x '{"event":"subscribe-to-url","data":{"urlId":"test123"}}' || echo "WebSocket test completed (timeout expected)"
else
    echo "⚠️  wscat not found. Install with: npm install -g wscat"
fi

echo
echo "📊 Testing POST endpoint (create click event)..."

# Test creating a click event
curl -X POST "$BASE_URL/click-events" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: $USER_ID" \
  -d '{
    "urlId": "test123",
    "ipAddress": "192.168.1.1",
    "referrer": "https://google.com",
    "countryCode": "US",
    "countryName": "United States",
    "city": "New York",
    "deviceType": "desktop",
    "browserName": "Chrome",
    "osName": "Windows"
  }' \
  -w "\nStatus: %{http_code}\n"

echo
echo "🎉 All tests completed!"
echo
echo "📝 Manual test suggestions:"
echo "1. Connect to WebSocket: ws://localhost:8082/analytics"
echo "2. Subscribe to URL: {\"event\":\"subscribe-to-url\",\"data\":{\"urlId\":\"test123\"}}"
echo "3. Create click events and watch real-time updates"
echo "4. Check database tables for data persistence"
echo "5. Verify Redis cache with: redis-cli"
echo "6. Monitor Kafka topics for message flow"