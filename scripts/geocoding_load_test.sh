#!/bin/bash

# Simple load test for geocoding endpoint
# Tests moderate load (10 concurrent users, 50 requests each)

BASE_URL="http://localhost:8080"
CONCURRENT_USERS=10
REQUESTS_PER_USER=50

echo "Starting geocoding load test..."
echo "Concurrent users: $CONCURRENT_USERS"
echo "Requests per user: $REQUESTS_PER_USER"
echo "Total requests: $((CONCURRENT_USERS * REQUESTS_PER_USER))"

# Test data
ADDRESSES=(
    '123 Main St,Bangalore,560001,Karnataka,India'
    '456 Park Ave,Mumbai,400001,Maharashtra,India'
    '789 Market St,Delhi,110001,Delhi,India'
    '321 Church Rd,Chennai,600001,Tamil Nadu,India'
    '654 College St,Kolkata,700001,West Bengal,India'
)

# Function to send requests
send_requests() {
    local user_id=$1
    echo "User $user_id started..."
    
    for i in $(seq 1 $REQUESTS_PER_USER); do
        # Pick random address
        addr_index=$((RANDOM % 5))
        IFS=',' read -r address city pincode state country <<< "${ADDRESSES[$addr_index]}"
        
        # Send request
        curl -s -w "%{http_code}" -o /dev/null -X POST \
            -H "Content-Type: application/json" \
            -d "{\"addressLine\":\"$address\",\"city\":\"$city\",\"pincode\":\"$pincode\",\"state\":\"$state\",\"country\":\"$country\"}" \
            "$BASE_URL/api/clients/addresses/geocode" \
            >> "load_test_user_${user_id}_results.txt"
        
        # Small delay between requests
        sleep 0.1
    done
    
    echo "User $user_id completed."
}

# Start concurrent users
for user in $(seq 1 $CONCURRENT_USERS); do
    send_requests $user &
done

# Wait for all users to complete
wait

echo "Load test completed. Analyzing results..."

# Analyze results
total_requests=0
success_count=0
error_count=0

for user in $(seq 1 $CONCURRENT_USERS); do
    if [ -f "load_test_user_${user}_results.txt" ]; then
        while read -r status_code; do
            total_requests=$((total_requests + 1))
            if [ "$status_code" = "200" ]; then
                success_count=$((success_count + 1))
            else
                error_count=$((error_count + 1))
            fi
        done < "load_test_user_${user}_results.txt"
        
        # Cleanup
        rm "load_test_user_${user}_results.txt"
    fi
done

echo "=== LOAD TEST RESULTS ==="
echo "Total requests: $total_requests"
echo "Successful: $success_count"
echo "Errors: $error_count"
echo "Success rate: $((success_count * 100 / total_requests))%"

if [ $success_count -gt $((total_requests * 80 / 100)) ]; then
    echo "✅ LOAD TEST PASSED - Success rate > 80%"
else
    echo "❌ LOAD TEST FAILED - Success rate < 80%"
fi
