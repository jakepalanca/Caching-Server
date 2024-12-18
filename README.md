# Caching Server

A Javalin-based API that caches cryptocurrency data from the CoinGecko API and provides endpoints for fetching and searching this data. It supports various data types, such as market cap, volume, price change, rank, and more. The API also features Quartz-based scheduled tasks to keep the cached data up-to-date.

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Environment Variables](#environment-variables)
- [Endpoints](#endpoints)
    - [GET /v1/bubbles/list](#get-v1bubbleslist)
    - [GET /v1/coins/all](#get-v1coinssall)
    - [GET /v1/coins/top100](#get-v1coinstop100)
    - [GET /v1/coins/search](#get-v1coinssearch)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [License](#license)

## Features

- **Cache Cryptocurrency Data**: Store and retrieve data for various cryptocurrencies from the CoinGecko API.
- **Multiple Data Types**: Support for different data types like market cap, volume, price change, rank, etc.
- **Bubbles Visualization**: Generate bubble chart data based on various criteria.
- **Scheduled Updates**: Automatically update the cache every 30 seconds using Quartz scheduler.

## Installation

### Prerequisites

- Java 14 or higher
- Maven 3.6.0 or higher

### Steps

1. **Clone the repository:**

   ```bash
   git clone https://github.com/jakepalanca/Caching-Server.git
   cd Caching-Server
   ```

2. **Build the project using Maven:**

   ```bash
   mvn clean install
   ```

3. **Run the application:**

   ```bash
   mvn exec:java -Dexec.mainClass="jakepalanca.caching.server.CryptoCacheApplication"
   ```

## Environment Variables

The following environment variables are necessary to run the application:

- **`DEMO_MODE`**: Set to `true` for demo mode, which uses the demo CoinGecko API endpoint. Set to `false` for production mode.
- **`COINGECKO_API_KEY`**: Your CoinGecko API key for accessing the CoinGecko API. Both demo mode and production mode require an API key.
- **`COIN_UPDATE_INTERVAL_SECONDS`** *(Optional)*: Interval in seconds for updating the cache. Defaults to `30` seconds if not set.

**Example:**

```bash
export DEMO_MODE=false
export COINGECKO_API_KEY="your-api-key-here"
export COIN_UPDATE_INTERVAL_SECONDS=30
```

## Endpoints

### GET /v1/bubbles/list

Fetches a list of bubbles (coins) with data based on the specified parameters.

**Query Parameters:**

- **`ids`** *(required)*: Comma-separated list of coin IDs.
- **`data_type`** *(required)*: Type of data to fetch. Must be one of:
  - `market_cap`
  - `total_volume`
  - `price_change`
  - `rank`
  - `market_cap_change_percentage_24hr`
  - `total_supply`
- **`time_interval`** *(optional)*: Time interval for price change. Valid only if `data_type` is `price_change`. Must be one of:
  - `1h`
  - `24h`
  - `7d`
  - `14d`
  - `30d`
  - `200d`
  - `1y`
- **`chart_width`** *(required)*: Chart width in pixels.
- **`chart_height`** *(required)*: Chart height in pixels.

**Example Request:**

```http
GET /v1/bubbles/list?ids=bitcoin,ethereum&data_type=market_cap&chart_width=1920&chart_height=1080
```

### GET /v1/coins/all

Returns the entire list of cached coins.

**Example Request:**

```http
GET /v1/coins/all
```

### GET /v1/coins/top100

Returns the top 100 coins by market cap from the cached data.

**Example Request:**

```http
GET /v1/coins/top100
```

### GET /v1/coins/search

Searches the cached coins based on a query.

**Query Parameters:**

- **`query`** *(required)*: The search query.

**Example Request:**

```http
GET /v1/coins/search?query=bitcoin
```

## Running the Application

To run the application on an EC2 instance or any server, use the following command:

```bash
nohup mvn exec:java -Dexec.mainClass="jakepalanca.caching.server.CryptoCacheApplication" > app.log 2>&1 &
```

This command starts the application in the background and logs the output to `app.log`.

## Testing

The project includes unit tests that can be run with Maven:

```bash
mvn test
```

The tests cover the core functionality of the `BubbleService`, `CoinGeckoClient`, and other key components. Additional test coverage is in progress.

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE.md) file for details.
