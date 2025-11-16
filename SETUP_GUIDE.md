# Setup Guide - Strathmore Flea Market

## Quick Start

### 1. Backend Setup (Ktor)

```bash
cd ktor-backend
./gradlew run
# Or on Windows:
gradlew.bat run
```

The server will start on `http://localhost:8080`

### 2. Android App Setup

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Run the app on an emulator or device

**Important**: 
- For Android emulator, the backend URL is already configured to `http://10.0.2.2:8080/`
- For physical devices, you need to:
  1. Find your computer's IP address (e.g., `192.168.1.100`)
  2. Update `ApiClient.BASE_URL` in `app/src/main/java/com/labs/fleamarketapp/api/ApiClient.kt`
  3. Make sure your phone and computer are on the same WiFi network

## Project Structure

```
MobileApp/
├── app/                          # Android app
│   ├── src/main/java/.../
│   │   ├── api/                  # Retrofit API client
│   │   ├── data/                 # Data models
│   │   ├── fragments/            # UI fragments
│   │   ├── local/                # Room database
│   │   ├── repository/          # Data repositories
│   │   └── viewmodel/            # ViewModels
│   └── build.gradle.kts
├── ktor-backend/                 # Ktor backend
│   ├── src/main/kotlin/.../
│   │   ├── database/             # Database setup
│   │   ├── models/               # Data models
│   │   └── routes/               # API routes
│   └── build.gradle.kts
└── README.md
```

## Features Implemented

✅ **Backend (Ktor)**
- User registration with @strathmore.edu validation
- User login
- Create items (with admin approval)
- Get all active items
- Place bids on auction items
- Get categories
- Notifications

✅ **Android App**
- Login/Signup screens
- Home tab (browse items)
- Sell tab (create listings)
- Profile tab
- Notifications tab
- Item detail view with bidding
- Pickup location workflow (seller + buyer selections)
- Room database for local caching

## API Endpoints

All endpoints return JSON in this format:
```json
{
  "success": true,
  "data": {...},
  "message": "Optional message"
}
```

### Authentication
- `POST /api/auth/register` - Register (requires @strathmore.edu email)
- `POST /api/auth/login` - Login

### Items
- `GET /api/items` - Get all active items
- `GET /api/items?category={id}` - Filter by category
- `GET /api/items?search={query}` - Search items
- `GET /api/items/{id}` - Get single item
- `POST /api/items` - Create item (header: `X-User-Id`)

### Bids
- `GET /api/items/{itemId}/bids` - Get bids for item
- `POST /api/items/{itemId}/bids` - Place bid (header: `X-User-Id`)

### Categories
- `GET /api/categories` - Get all categories

### Notifications
- `GET /api/notifications` - Get user notifications (header: `X-User-Id`)
- `PUT /api/notifications/{id}/read` - Mark as read

### Admin
- `GET /api/admin/users` - List all users (header: `X-Admin-Id`)
- `PUT /api/admin/users/{id}/status` - Approve / reject users
- `GET /api/admin/items` - Review every item
- `PUT /api/admin/items/{id}/status` - Change listing status
- `DELETE /api/admin/items/{id}` - Remove a listing

## Default Credentials

- **Admin**: admin@strathmore.edu / admin123
- New users start with `PENDING` status and need admin approval

## Troubleshooting

### Backend won't start
- Check if port 8080 is already in use
- Make sure Java 11+ is installed
- Run `./gradlew clean` then `./gradlew run`

### Android app can't connect to backend
- Make sure backend is running
- For emulator: Use `10.0.2.2:8080`
- For physical device: Update `ApiClient.BASE_URL` to your computer's IP
- Check network security config allows cleartext traffic

### Gradle sync errors
- Make sure you have internet connection
- Try "Invalidate Caches / Restart" in Android Studio
- Delete `.gradle` folder and sync again

## Next Steps

1. **Add JWT Authentication** - Replace simple `X-User-Id` header with JWT tokens
2. **Password Hashing** - Use bcrypt or similar for password storage
3. **Image Upload** - Add file upload endpoint for item images
4. **Admin Panel** - Create admin interface for approving users/items
5. **Push Notifications** - Add Firebase Cloud Messaging for real-time notifications

