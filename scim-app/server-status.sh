#!/bin/bash
# ==========================================
# SCIM Gateway - Status Check Script
# ==========================================
# Usage: ./status.sh

PID_FILE="scim-gateway.pid"

echo ""
echo "========================================="
echo "  SCIM Gateway Status"
echo "========================================="
echo ""

# Check PID file
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "Status: ✅ RUNNING"
        echo "PID: $PID"
        echo ""
        
        # Get process info
        echo "Process Info:"
        ps -p "$PID" -o pid,vsz,rss,%cpu,%mem,etime,cmd --no-headers | while read line; do
            echo "  $line"
        done
        echo ""
    else
        echo "Status: ❌ NOT RUNNING (stale PID file)"
        echo "PID: $PID (not found)"
        rm -f "$PID_FILE"
    fi
else
    echo "Status: ❌ NOT RUNNING"
    echo "PID: N/A"
fi

echo ""

# Check port
echo "Port Check:"
if command -v netstat > /dev/null 2>&1; then
    if netstat -tuln 2>/dev/null | grep -q ":8181 "; then
        echo "  Port 8181: ✅ LISTENING"
    else
        echo "  Port 8181: ❌ NOT LISTENING"
    fi
elif command -v ss > /dev/null 2>&1; then
    if ss -tuln 2>/dev/null | grep -q ":8181 "; then
        echo "  Port 8181: ✅ LISTENING"
    else
        echo "  Port 8181: ❌ NOT LISTENING"
    fi
fi

echo ""

# Check health endpoint
echo "Health Check:"
if command -v curl > /dev/null 2>&1; then
    HEALTH_RESPONSE=$(curl -s http://localhost:8181/actuator/health 2>/dev/null)
    
    if [ $? -eq 0 ]; then
        echo "  ✅ Application is healthy"
        echo "  Response: $HEALTH_RESPONSE"
    else
        echo "  ❌ Health check failed"
    fi
else
    echo "  ⚠️ curl not available"
fi

echo ""

# Check log file
if [ -f "scim-gateway.log" ]; then
    LOG_SIZE=$(du -h scim-gateway.log | cut -f1)
    LOG_LINES=$(wc -l < scim-gateway.log)
    echo "Log File:"
    echo "  File: scim-gateway.log"
    echo "  Size: $LOG_SIZE"
    echo "  Lines: $LOG_LINES"
    echo ""
    
    echo "Recent Errors:"
    grep -i "error\|exception\|fail" scim-gateway.log | tail -3 | while read line; do
        echo "  $line"
    done
    echo ""
fi

echo "========================================="
echo ""
echo "Quick Commands:"
echo "  Start:   ./start.sh"
echo "  Stop:    ./stop.sh"
echo "  Logs:    tail -f scim-gateway.log"
echo ""
