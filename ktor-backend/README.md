# Strathmore Flea Market - Ktor Backend

Simple Ktor backend server for the Strathmore Flea Market app.

## Setup

1. Make sure you have Java 11+ installed
2. Run the server:
   ```bash
   ./gradlew run
   ```
   Or on Windows:
   ```bash
   gradlew.bat run
   ```

3. Server will start on `http://localhost:8080`

## API Endpoints

- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `GET /api/items` - Get all active items
- `GET /api/items/{id}` - Get single item
- `POST /api/items` - Create new item (requires X-User-Id header)
- `GET /api/items/{itemId}/bids` - Get bids for item
- `POST /api/items/{itemId}/bids` - Place bid (requires X-User-Id header)
- `GET /api/categories` - Get all categories
- `GET /api/notifications` - Get user notifications (requires X-User-Id header)
- `PUT /api/notifications/{id}/read` - Mark notification as read

## Database

SQLite database is created in `data/marketplace.db` on first run.

Sample data is preloaded:
- 5 categories (Electronics, Books, Clothing, Jewellery, Other)
- Admin user (admin@strathmore.edu / admin123)

