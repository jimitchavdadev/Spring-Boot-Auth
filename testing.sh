#!/bin/bash

# Base URL of the Spring Boot application
BASE_URL="http://localhost:8080/auth"

# User role for testing
ROLES="ROLE_USER"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

# Function to check if curl command was successful
check_status() {
    if [ $1 -eq $2 ]; then
        echo -e "${GREEN}Success: $3${NC}"
    else
        echo -e "${RED}Error: $3 failed (HTTP Status: $1, Expected: $2)${NC}"
        exit 1
    fi
}

while true; do
    # Prompt user for action
    echo "Choose an action: [1] Registration, [2] Login, [3] Exit"
    read -p "Enter choice (1, 2, or 3): " CHOICE

    if [ "$CHOICE" = "3" ]; then
        echo "Exiting..."
        exit 0
    fi

    if [ "$CHOICE" != "1" ] && [ "$CHOICE" != "2" ]; then
        echo -e "${RED}Invalid choice. Please enter 1, 2, or 3.${NC}"
        continue
    fi

    # Prompt for email and password
    echo "Enter your email:"
    read EMAIL
    echo "Enter your password:"
    read -s PASSWORD
    echo

    if [ "$CHOICE" = "1" ]; then
        # Registration
        echo "Registering user with email: $EMAIL"
        REGISTER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/register" \
            -H "Content-Type: application/json" \
            -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\",\"roles\":\"$ROLES\"}")

        REGISTER_BODY=$(echo "$REGISTER_RESPONSE" | head -n -1)
        REGISTER_STATUS=$(echo "$REGISTER_RESPONSE" | tail -n 1)
        echo "Register Response: $REGISTER_BODY (HTTP Status: $REGISTER_STATUS)"
        if [[ "$REGISTER_BODY" == *"OTP sent"* && "$REGISTER_STATUS" -eq 200 ]]; then
            check_status "$REGISTER_STATUS" 200 "User registration"
        elif [[ "$REGISTER_BODY" == *"Account already exists"* && "$REGISTER_STATUS" -eq 400 ]]; then
            echo -e "${RED}Error: $REGISTER_BODY${NC}"
            continue
        else
            echo -e "${RED}Error: User registration failed. Response: $REGISTER_BODY${NC}"
            exit 1
        fi

        # Verify OTP for registration
        echo "Enter the OTP sent to $EMAIL for registration:"
        read OTP_CODE
        VERIFY_OTP_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/verify-otp" \
            -H "Content-Type: application/json" \
            -d "{\"email\":\"$EMAIL\",\"otpCode\":\"$OTP_CODE\"}")

        VERIFY_OTP_BODY=$(echo "$VERIFY_OTP_RESPONSE" | head -n -1)
        VERIFY_OTP_STATUS=$(echo "$VERIFY_OTP_RESPONSE" | tail -n 1)
        echo "Verify OTP Response: $VERIFY_OTP_BODY (HTTP Status: $VERIFY_OTP_STATUS)"
        if [[ "$VERIFY_OTP_BODY" == *"Registered successfully"* && "$VERIFY_OTP_STATUS" -eq 200 ]]; then
            check_status "$VERIFY_OTP_STATUS" 200 "OTP verification for registration"
            echo -e "${GREEN}Registered successfully!${NC}"
        else
            echo -e "${RED}Error: OTP verification failed. Response: $VERIFY_OTP_BODY${NC}"
            exit 1
        fi

    elif [ "$CHOICE" = "2" ]; then
        # Login
        echo "Logging in with email: $EMAIL"
        LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/login" \
            -H "Content-Type: application/json" \
            -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")

        LOGIN_BODY=$(echo "$LOGIN_RESPONSE" | head -n -1)
        LOGIN_STATUS=$(echo "$LOGIN_RESPONSE" | tail -n 1)
        echo "Login Response: $LOGIN_BODY (HTTP Status: $LOGIN_STATUS)"
        if [[ "$LOGIN_BODY" == *"OTP sent"* && "$LOGIN_STATUS" -eq 200 ]]; then
            check_status "$LOGIN_STATUS" 200 "User login OTP request"
        elif [[ "$LOGIN_BODY" == *"Account does not exist"* && "$LOGIN_STATUS" -eq 400 ]]; then
            echo -e "${RED}Error: $LOGIN_BODY${NC}"
            continue
        else
            echo -e "${RED}Error: Login OTP request failed. Response: $LOGIN_BODY${NC}"
            exit 1
        fi

        # Verify OTP for login
        echo "Enter the OTP sent to $EMAIL for login:"
        read OTP_CODE
        VERIFY_LOGIN_OTP_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/verify-login-otp" \
            -H "Content-Type: application/json" \
            -d "{\"email\":\"$EMAIL\",\"otpCode\":\"$OTP_CODE\"}")

        VERIFY_LOGIN_OTP_BODY=$(echo "$VERIFY_LOGIN_OTP_RESPONSE" | head -n -1)
        VERIFY_LOGIN_OTP_STATUS=$(echo "$VERIFY_LOGIN_OTP_RESPONSE" | tail -n 1)
        echo "Verify Login OTP Response: $VERIFY_LOGIN_OTP_BODY (HTTP Status: $VERIFY_LOGIN_OTP_STATUS)"
        if [[ "$VERIFY_LOGIN_OTP_BODY" == *"Login successful"* && "$VERIFY_LOGIN_OTP_STATUS" -eq 200 ]]; then
            check_status "$VERIFY_LOGIN_OTP_STATUS" 200 "OTP verification for login"
            echo -e "${GREEN}Login successful!${NC}"
        else
            echo -e "${RED}Error: Login OTP verification failed. Response: $VERIFY_LOGIN_OTP_BODY${NC}"
            exit 1
        fi
    fi
done