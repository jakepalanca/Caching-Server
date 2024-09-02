# CryptoCacheAPI

CryptoCacheAPI is a Javalin-based API that caches cryptocurrency data from the CoinGecko API and provides endpoints for fetching and searching this data. It supports various data types, such as market cap, volume, price change, rank, and more. The API also features Quartz-based scheduled tasks to keep the cached data up-to-date.

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Environment Variables](#environment-variables)
- [Endpoints](#endpoints)
    - [GET /bubbles/list](#get-bubbleslist)
    - [GET /coins](#get-coins)
    - [GET /top100](#get-top100)
    - [GET /search](#get-search)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [Javadocs](#javadocs)
- [License](#license)

## Features

- **Cache Cryptocurrency Data**: Store and retrieve data for various cryptocurrencies from the CoinGecko API.
- **Multiple Data Types**: Support for different data types like market cap, volume, price change, rank, etc.
- **Bubbles Visualization**: Generate bubble chart data based on various criteria.
- **Scheduled Updates**: Automatically update the cache every 15 seconds using Quartz scheduler.
- **Basic Authentication**: Secure your API endpoints using Basic Authentication.

## Installation

### Prerequisites

- Java 14 or higher
- Maven 3.6.0 or higher

### Steps

1. Clone the repository:

   ```bash
   git clone https://github.com/jakepalanca/CryptoCacheAPI.git
   cd CryptoCacheAPI
   ```

2. Build the project using Maven:

   ```bash
   mvn clean install
   ```

3. Run the application:

   ```bash
   mvn exec:java -Dexec.mainClass="jakepalanca.cryptocache.javalin.CryptoCacheApplication"
   ```

## Environment Variables

The following environment variables are necessary to run the application:

- **`DEMO_MODE`**: Set to `true` for demo mode, which uses the demo CoinGecko API endpoint. Set to `false` for production mode.
- **`COINGECKO_API_KEY`**: Your CoinGecko API key for accessing the CoinGecko API. Both demo mode and pro mode require API key.

Example:

```bash
export DEMO_MODE=false
export COINGECKO_API_KEY="your-api-key-here"
```

## Endpoints

### GET /bubbles/list

Fetches a list of bubbles (coins) with data based on the specified parameters.

**Query Parameters:**

- **`ids`**: Comma-separated list of coin IDs (required).
- **`data_type`**: Type of data to fetch (required). Must be one of:
    - `market_cap`
    - `total_volume`
    - `price_change`
    - `sentiment`
    - `rank`
    - `market_cap_change_percentage_24hr`
    - `total_supply`
- **`time_interval`**: Time interval for price change. Valid only if `data_type` is `price_change`. Must be one of:
    - `1h`
    - `24h`
    - `7d`
    - `14d`
    - `30d`
    - `200d`
    - `1y`
- **`x_height_chartview`**: Chart view height in pixels (required).
- **`y_width_chartview`**: Chart view width in pixels (required).

**Example Request:**

```http
GET /bubbles/list?ids=bitcoin,ethereum&data_type=market_cap&x_height_chartview=1080&y_width_chartview=1920
```

### GET /coins

Returns the entire list of cached coins.

**Example Request:**

```http
GET /coins
```

### GET /top100

Returns the top 100 coins by market cap from the cached data.

**Example Request:**

```http
GET /top100
```

### GET /search

Searches the cached coins based on a query.

**Query Parameters:**

- **`query`**: The search query (required).

**Example Request:**

```http
GET /search?query=bitcoin
```

## Running the Application

To run the application on an EC2 instance or any server, you can use the following command:

```bash
nohup mvn exec:java -Dexec.mainClass="jakepalanca.cryptocache.javalin.CryptoCacheApplication" > app.log 2>&1 &
```

This command starts the application in the background and logs the output to `app.log`.

## Testing

The project includes unit tests that can be run with Maven:

```bash
mvn test
```

The tests cover the core functionality of the `BubbleService`, `CoinGeckoClient`, and other key components. I am working on adding more coverage.

## Javadocs

The full API documentation for this project is available online. You can access the Javadocs at the following URL:

[![Javadocs](https://img.shields.io/badge/Javadocs-Online-blue)](https://javadoc.jakepalanca.com/)

This documentation provides detailed information about all classes, methods, and fields used in this project, including usage examples and descriptions.

Visit the Javadocs here: [https://javadoc.jakepalanca.com/](https://javadoc.jakepalanca.com/)

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE.md](LICENSE.md) file for details.