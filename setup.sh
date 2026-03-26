#!/bin/bash
# ============================================================
# setup.sh — Aegis Infra SaaS Backend
# Automates Docker Compose startup for the full stack
# ============================================================
# Usage:
#   bash setup.sh          → Start all services (foreground)
#   bash setup.sh -d       → Start in detached/background mode
#   bash setup.sh --stop   → Stop all services
#   bash setup.sh --clean  → Stop & remove volumes (fresh DB)
# ============================================================

set -e  # Exit on any error

# ── Colours ───────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ── Banner ────────────────────────────────────────────────
echo -e "${CYAN}"
echo "╔══════════════════════════════════════════════╗"
echo "║         Aegis Infra — SaaS Backend           ║"
echo "║         Docker Compose Setup Script          ║"
echo "╚══════════════════════════════════════════════╝"
echo -e "${NC}"

# ── Check Docker is running ───────────────────────────────
if ! docker info > /dev/null 2>&1; then
  echo -e "${RED}✗ Docker is not running. Please start Docker Desktop and try again.${NC}"
  exit 1
fi
echo -e "${GREEN}✓ Docker is running${NC}"

# ── Check .env file exists ───────────────────────────────
if [ ! -f ".env" ]; then
  echo -e "${RED}✗ .env file not found! Copy .env.example and fill in your credentials.${NC}"
  exit 1
fi
echo -e "${GREEN}✓ .env file found${NC}"

# ── Handle flags ─────────────────────────────────────────
case "$1" in
  --stop)
    echo -e "${YELLOW}► Stopping all services...${NC}"
    docker compose down
    echo -e "${GREEN}✓ All services stopped.${NC}"
    exit 0
    ;;
  --clean)
    echo -e "${YELLOW}► Stopping services and removing volumes (database will be erased)...${NC}"
    docker compose down -v
    echo -e "${GREEN}✓ All services stopped and volumes removed.${NC}"
    exit 0
    ;;
  -d)
    DETACHED="-d"
    ;;
  *)
    DETACHED=""
    ;;
esac

# ── Start services ────────────────────────────────────────
echo -e "${YELLOW}► Building and starting services...${NC}"
docker compose up --build $DETACHED

# ── If detached, show status ──────────────────────────────
if [ "$DETACHED" = "-d" ]; then
  echo ""
  echo -e "${GREEN}✓ Services started in background!${NC}"
  echo -e "${CYAN}"
  echo "  App:      http://localhost:8080"
  echo "  Database: localhost:3306 (saasdb)"
  echo ""
  echo "  Logs:     docker compose logs -f app"
  echo "  Stop:     bash setup.sh --stop"
  echo -e "${NC}"
  docker compose ps
fi
