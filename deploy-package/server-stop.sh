#!/bin/bash
# ==========================================
# SCIM Gateway - Stop Script
# ==========================================
# Usage: ./stop.sh

PID_FILE="scim-gateway.pid"

echo ""
echo "========================================="
echo "  Stopping SCIM Gateway"
echo "========================================="
echo ""

if [ ! -f "$PID_FILE" ]; then
    echo "[INFO] No PID file found. Application may not be running."
    
    # Try to find Java process
    JAVA_PID=$(ps aux | grep -i "scim-app" | grep -v grep | awk '{print $2}' | head -n 1)
    
    if [ -n "$JAVA_PID" ]; then
        echo "[INFO] Found Java process: $JAVA_PID"
        read -p "Stop it? (y/n): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            kill $JAVA_PID 2>/dev/null || true
            sleep 2
            kill -9 $JAVA_PID 2>/dev/null || true
            echo "[OK] Process stopped"
        fi
    else
        echo "[OK] No running instance found"
    fi
    exit 0
fi

PID=$(cat "$PID_FILE")

if ps -p "$PID" > /dev/null 2>&1; then
    echo "Stopping application (PID: $PID)..."
    
    # Try graceful shutdown
    curl -s -X POST http://localhost:8181/actuator/shutdown > /dev/null 2>&1 &
    sleep 3
    
    # Check if stopped
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "Graceful shutdown failed, force stopping..."
        kill -9 "$PID" 2>/dev/null || true
        sleep 1
    fi
    
    rm -f "$PID_FILE"
    echo "[OK] Application stopped"
else
    echo "[INFO] Process $PID is not running"
    rm -f "$PID_FILE"
    echo "[OK] PID file removed"
fi

echo ""
