# Strathmore Flea Market

A complete Kotlin-based marketplace app with Android frontend and Ktor backend.

## Project Structure

```
strathmore-fleamarket/
├── android-app/          # Android frontend (Kotlin)
│   └── app/              # Main Android app module
├── ktor-backend/          # Backend server (Kotlin)
└── README.md
```

## Tech Stack

- **Android**: Room, Retrofit, MVVM
- **Backend**: Ktor, SQLite, Exposed ORM
- **100% Kotlin** - shared data classes

## Setup Instructions

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd ktor-backend
   ```

2. Run the server:
   ```bash
   ./gradlew run
   ```
   Or on Windows:
   ```bash
   gradlew.bat run
   ```

3. Server will start on `http://localhost:8080`

### Android App Setup

1. Open the project in Android Studio
2. Sync Gradle files
3. Run the app on an emulator or device

**Note**: For Android emulator, the backend URL is already configured to `http://10.0.2.2:8080/`. For physical devices, update `ApiClient.BASE_URL` in `app/src/main/java/com/labs/fleamarketapp/api/ApiClient.kt` to your computer's IP address.

## Features

- ✅ User registration (requires @strathmore.edu email)
- ✅ User login
- ✅ Post items (with admin approval)
- ✅ Browse all approved items
- ✅ Place bids on auction items
- ✅ Notifications
- ✅ Categories

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user

### Items
- `GET /api/items` - Get all active items
- `GET /api/items/{id}` - Get single item
- `POST /api/items` - Create new item (requires X-User-Id header)

### Bids
- `GET /api/items/{itemId}/bids` - Get bids for item
- `POST /api/items/{itemId}/bids` - Place bid (requires X-User-Id header)

### Categories
- `GET /api/categories` - Get all categories

### Notifications
- `GET /api/notifications` - Get user notifications (requires X-User-Id header)
- `PUT /api/notifications/{id}/read` - Mark notification as read

## Database

SQLite database is created in `ktor-backend/data/marketplace.db` on first run.

Sample data is preloaded:
- 5 categories (Electronics, Books, Clothing, Jewellery, Other)
- Admin user (admin@strathmore.edu / admin123)

## Architecture

- **MVVM** pattern in Android app
- **Repository pattern** for data access
- **Room** for local caching
- **Retrofit** for API calls
- **Ktor** for backend API
- **Exposed ORM** for database access

## Development Notes

- The app uses a simple header-based authentication (`X-User-Id`) for now. In production, implement JWT tokens.
- Passwords are stored in plain text for simplicity. In production, use proper hashing (bcrypt, etc.).
- Items require admin approval before becoming active.
- All users start with `PENDING` status and need admin approval.

