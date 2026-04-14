#!/bin/bash
# ==========================================
# SCIM Gateway - Linux Server Startup Script
# ==========================================
# This script loads environment variables from .env and starts the application
# Usage: ./start.sh

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo ""
echo "========================================="
echo "  SCIM Gateway - Server Startup"
echo "========================================="
echo ""

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo -e "${RED}[ERROR] .env file not found!${NC}"
    echo ""
    echo "Please create a .env file with your configuration:"
    echo "  cp .env.example .env"
    echo "  nano .env"
    echo ""
    exit 1
fi

# Load environment variables from .env
echo -e "${CYAN}[1/4] Loading environment variables...${NC}"
set -a
source .env
set +a
echo -e "${GREEN}[OK] Environment loaded${NC}"

# Verify required variables
echo -e "${CYAN}[2/4] Verifying configuration...${NC}"
MISSING_VARS=()

if [ -z "$JWT_SECRET" ] || [ "$JWT_SECRET" == "CHANGE-THIS-TO-YOUR-OWN-SECRET-KEY" ]; then
    MISSING_VARS+=("JWT_SECRET")
fi

if [ -z "$SCIM_ADMIN_PASSWORD" ] || [ "$SCIM_ADMIN_PASSWORD" == "CHANGE-THIS-TO-STRONG-PASSWORD" ]; then
    MISSING_VARS+=("SCIM_ADMIN_PASSWORD")
fi

if [ -z "$MONGODB_URI" ] || [[ "$MONGODB_URI" == *"CHANGE_ME"* ]]; then
    MISSING_VARS+=("MONGODB_URI")
fi

if [ ${#MISSING_VARS[@]} -gt 0 ]; then
    echo -e "${YELLOW}[WARN] Missing or default configuration:${NC}"
    for var in "${MISSING_VARS[@]}"; do
        echo -e "${YELLOW}  - $var${NC}"
    done
    echo ""
    read -p "Continue anyway? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
else
    echo -e "${GREEN}[OK] All required variables set${NC}"
fi

# Find JAR file
echo -e "${CYAN}[3/4] Finding application JAR...${NC}"
JAR_FILE=$(find . -name "scim-app-*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo -e "${RED}[ERROR] No JAR file found!${NC}"
    echo ""
    echo "Please build the application first:"
    echo "  mvn clean package -DskipTests"
    echo ""
    echo "Or upload the JAR file to this directory."
    exit 1
fi

echo -e "${GREEN}[OK] Found: $JAR_FILE${NC}"

# Check if already running
echo -e "${CYAN}[4/4] Checking for running instances...${NC}"
PID_FILE="scim-gateway.pid"

if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")
    if ps -p "$OLD_PID" > /dev/null 2>&1; then
        echo -e "${YELLOW}[WARN] Application already running (PID: $OLD_PID)${NC}"
        read -p "Stop and restart? (y/n): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            echo "Stopping application..."
            kill "$OLD_PID" 2>/dev/null || true
            sleep 3
            
            # Force kill if still running
            if ps -p "$OLD_PID" > /dev/null 2>&1; then
                kill -9 "$OLD_PID" 2>/dev/null || true
                sleep 1
            fi
            echo -e "${GREEN}[OK] Stopped${NC}"
        else
            exit 0
        fi
    else
        rm -f "$PID_FILE"
    fi
fi

# Set Spring profile
SPRING_PROFILE="${SPRING_PROFILES_ACTIVE:-prod}"

# Start application
echo ""
echo "========================================="
echo "  Starting SCIM Gateway"
echo "========================================="
echo "Profile: $SPRING_PROFILE"
echo "JAR: $JAR_FILE"
echo "Log: scim-gateway.log"
echo "PID File: $PID_FILE"
echo "========================================="
echo ""

# Start in background
nohup java -jar "$JAR_FILE" \
    --spring.profiles.active="$SPRING_PROFILE" \
    > scim-gateway.log 2>&1 &

APP_PID=$!
echo $APP_PID > "$PID_FILE"

echo -e "${GREEN}[OK] Application started (PID: $APP_PID)${NC}"
echo ""

# Wait for startup
echo "Waiting for application to start..."
MAX_WAIT=60
WAITED=0
STARTED=false

while [ $WAITED -lt $MAX_WAIT ]; do
    sleep 2
    WAITED=$((WAITED + 2))
    
    if curl -s http://localhost:8181/actuator/health > /dev/null 2>&1; then
        STARTED=true
        break
    fi
    
    echo "  Waiting... ($WAITED/$MAX_WAIT seconds)"
done

echo ""

if [ "$STARTED" = true ]; then
    echo "========================================="
    echo -e "  ${GREEN}Application Started Successfully!${NC}"
    echo "========================================="
    echo "URL: http://localhost:8181"
    echo "Swagger: http://localhost:8181/swagger-ui.html"
    echo "Health: http://localhost:8181/actuator/health"
    echo "PID: $APP_PID"
    echo "Log: scim-gateway.log"
    echo "========================================="
    echo ""
    echo "Quick Commands:"
    echo "  View logs:   tail -f scim-gateway.log"
    echo "  Stop app:    ./stop.sh"
    echo "  Check status: ./status.sh"
    echo ""
else
    echo "========================================="
    echo -e "  ${YELLOW}Application may still be starting...${NC}"
    echo "========================================="
    echo "Check logs: tail -f scim-gateway.log"
    echo ""
fi
