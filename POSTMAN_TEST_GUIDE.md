# 🔧 POSTMAN TEST GUIDE - JWT AUTHENTICATION

## ✅ BACKEND IS READY

### 📋 FIXED ISSUES:
- ✅ Duplicate JwtService deleted
- ✅ AuthenticationManager bean configured
- ✅ Compilation successful
- ✅ Security config complete

---

## 🧪 POSTMAN TESTING STEPS

### 1️⃣ TEST LOGIN ENDPOINT

**Request:**
```
POST https://api.yashrajent.com/api/auth/login
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "your-email@company.com",
  "password": "your-password"
}
```

**Expected Success Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ5b3VyLWVtYWlsQGNvbXBhbnkuY29tIiwicm9sZSI6IkVYRUNVVElWRSIsImlhdCI6MTY3NjMwMDAwMCwiZXhwIjoxNjc2MzM2MDAwfQ.signature",
  "role": "EXECUTIVE",
  "employeeId": 2,
  "user": {
    "id": 2,
    "email": "your-email@company.com",
    "firstName": "Arbaj",
    "lastName": "Shaikh",
    "role": "EXECUTIVE"
  },
  "message": "Login successful"
}
```

**Error Response (401):**
```json
{
  "error": "Invalid credentials"
}
```

---

### 2️⃣ TEST AUTHENTICATED ENDPOINTS

**Copy the token from login response and use in:**

#### Test Tasks Endpoint:
```
GET https://api.yashrajent.com/api/tasks
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Test Location Endpoint:
```
POST https://api.yashrajent.com/api/employee-locations/2/location
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "latitude": 19.0760,
  "longitude": 72.8777
}
```

#### Test Debug Role:
```
GET https://api.yashrajent.com/api/debug/role
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Expected Debug Response:**
```json
{
  "authenticated": true,
  "name": "your-email@company.com",
  "authorities": ["ROLE_EXECUTIVE"],
  "hasRolePrefix": true
}
```

---

### 3️⃣ COMMON POSTMAN ISSUES

#### ❌ 401 Unauthorized:
- Check email/password are correct
- Verify user exists in database
- Check user status is ACTIVE

#### ❌ 403 Forbidden:
- Check that `hasRolePrefix: true` in debug response
- Verify role is included in SecurityConfig (EXECUTIVE)
- Ensure Authorization header format: `Bearer <token>`

#### ❌ 500 Internal Server Error:
- Check backend logs for errors
- Verify JWT dependencies are loaded
- Check database connection

---

## 🚀 NEXT STEPS AFTER POSTMAN TESTS

### ✅ If Login Works:
1. **Update Next.js frontend** - Add JWT token handling
2. **Update Flutter app** - Add JWT to Dio headers
3. **Test real-time notifications** - WebSocket should work
4. **Test address edit workflow** - Complete flow

### ❌ If Login Fails:
1. **Check backend logs** - Look for authentication errors
2. **Verify database user** - Check email/password hash
3. **Check role assignment** - Ensure user has proper role

---

## 📱 FRONTEND UPDATES NEEDED

### Next.js:
```javascript
// Store token after login
localStorage.setItem('jwt_token', response.data.token);

// Add to all API calls
const api = axios.create({
  baseURL: 'https://api.yashrajent.com/api',
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('jwt_token')}`
  }
});
```

### Flutter:
```dart
// Store token after login
prefs.setString('jwt_token', response['token']);

// Add to Dio
dio.options.headers['Authorization'] = 'Bearer $token';
```

---

## 🎯 SUCCESS CRITERIA

- ✅ Login returns JWT token
- ✅ Debug endpoint shows `hasRolePrefix: true`
- ✅ Tasks endpoint returns 200 (not 403)
- ✅ Location endpoint returns 200 (not 403)
- ✅ Real-time notifications work

**🔥 TEST IN POSTMAN FIRST!**
