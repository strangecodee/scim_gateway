#!/bin/bash
# verify-no-hardcoded.sh - Script to verify no hardcoded URLs or secrets exist

echo "============================================="
echo "  Checking for Hardcoded Values..."
echo "============================================="
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

ERRORS=0

# Check for hardcoded URLs
echo "1. Checking for hardcoded URLs..."
URL_COUNT=$(grep -r "https\?://" src/main/java --include="*.java" | grep -v "^[[:space:]]*\/\/" | grep -v "@Value" | wc -l)

if [ $URL_COUNT -eq 0 ]; then
    echo -e "   ${GREEN}✅ No hardcoded URLs found${NC}"
else
    echo -e "   ${RED}❌ Found $URL_COUNT hardcoded URLs:${NC}"
    grep -r "https\?://" src/main/java --include="*.java" | grep -v "^[[:space:]]*\/\/" | grep -v "@Value"
    ERRORS=$((ERRORS + 1))
fi

echo ""

# Check for hardcoded passwords
echo "2. Checking for hardcoded passwords..."
PASS_COUNT=$(grep -r "password.*=.*['\"]" src/main/java --include="*.java" | grep -v "@Value" | grep -v "//" | wc -l)

if [ $PASS_COUNT -eq 0 ]; then
    echo -e "   ${GREEN}✅ No hardcoded passwords found${NC}"
else
    echo -e "   ${RED}❌ Found $PASS_COUNT hardcoded passwords:${NC}"
    grep -r "password.*=.*['\"]" src/main/java --include="*.java" | grep -v "@Value" | grep -v "//"
    ERRORS=$((ERRORS + 1))
fi

echo ""

# Check for hardcoded secrets
echo "3. Checking for hardcoded secrets..."
SECRET_COUNT=$(grep -r "secret.*=.*['\"]" src/main/java --include="*.java" | grep -v "@Value" | grep -v "//" | wc -l)

if [ $SECRET_COUNT -eq 0 ]; then
    echo -e "   ${GREEN}✅ No hardcoded secrets found${NC}"
else
    echo -e "   ${RED}❌ Found $SECRET_COUNT hardcoded secrets:${NC}"
    grep -r "secret.*=.*['\"]" src/main/java --include="*.java" | grep -v "@Value" | grep -v "//"
    ERRORS=$((ERRORS + 1))
fi

echo ""

# Check for hardcoded emails
echo "4. Checking for hardcoded emails..."
EMAIL_COUNT=$(grep -r "[a-zA-Z0-9._%+-]\+@[a-zA-Z0-9.-]\+\.[a-zA-Z]\{2,\}" src/main/java --include="*.java" | grep -v "@Value" | grep -v "//" | wc -l)

if [ $EMAIL_COUNT -eq 0 ]; then
    echo -e "   ${GREEN}✅ No hardcoded emails found${NC}"
else
    echo -e "   ${YELLOW}⚠️  Found $EMAIL_COUNT hardcoded emails (may be acceptable):${NC}"
    grep -r "[a-zA-Z0-9._%+-]\+@[a-zA-Z0-9.-]\+\.[a-zA-Z]\{2,\}" src/main/java --include="*.java" | grep -v "@Value" | grep -v "//"
fi

echo ""

# Check for localhost URLs
echo "5. Checking for localhost URLs..."
LOCALHOST_COUNT=$(grep -r "localhost:" src/main/java --include="*.java" | grep -v "//" | wc -l)

if [ $LOCALHOST_COUNT -eq 0 ]; then
    echo -e "   ${GREEN}✅ No hardcoded localhost URLs found${NC}"
else
    echo -e "   ${RED}❌ Found $LOCALHOST_COUNT hardcoded localhost URLs:${NC}"
    grep -r "localhost:" src/main/java --include="*.java" | grep -v "//"
    ERRORS=$((ERRORS + 1))
fi

echo ""
echo "============================================="

if [ $ERRORS -eq 0 ]; then
    echo -e "  ${GREEN}✅ ALL CHECKS PASSED!${NC}"
    echo -e "  ${GREEN}No hardcoded values found in source code${NC}"
else
    echo -e "  ${RED}❌ FOUND $ERRORS ISSUE(S)${NC}"
    echo -e "  ${RED}Please remove all hardcoded values${NC}"
fi

echo "============================================="
echo ""

exit $ERRORS
