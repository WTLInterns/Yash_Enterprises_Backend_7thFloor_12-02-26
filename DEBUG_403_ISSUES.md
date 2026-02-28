# 🔍 DEBUGGING 403 ISSUES - TEST STEPS

## 1️⃣ TEST DEBUG ENDPOINT
```bash
# Test if authentication is working
curl -X GET "http://localhost:8080/api/debug/role" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 2️⃣ EXPECTED RESPONSE
If role is working correctly, you should see:
```json
{
  "authenticated": true,
  "name": "arbaj@company.com",
  "authorities": ["ROLE_EXECUTIVE"],
  "hasRolePrefix": true
}
```

## 3️⃣ IF YOU SEE:
```json
{
  "authorities": ["EXECUTIVE"],
  "hasRolePrefix": false
}
```
→ Then ROLE_ prefix is missing. Need to fix authentication.

## 4️⃣ TEST TASKS ENDPOINT
```bash
# Should now work with EXECUTIVE role
curl -X GET "http://localhost:8080/api/tasks" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 5️⃣ TEST LOCATION ENDPOINT  
```bash
# Should now work with EXECUTIVE role
curl -X POST "http://localhost:8080/api/employee-locations/2/location" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"latitude": 19.0760, "longitude": 72.8777}'
```

## ✅ SUCCESS CRITERIA
- `/api/debug/role` shows `hasRolePrefix: true`
- `/api/tasks` returns 200 (not 403)
- `/api/employee-locations/2/location` returns 200 (not 403)

## 🔧 IF STILL 403
1. Check JWT token is valid
2. Check `authorities` array in debug response
3. Verify role name matches exactly (EXECUTIVE vs Executive)
4. Check Spring Security logs for detailed error

## 🚀 NEXT STEPS
If quick fix works, consider implementing full JWT authentication as outlined in the enterprise solution.
