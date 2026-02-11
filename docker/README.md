# 🐳 Docker for Development

## 🛠️ Build Local Development Image

### Option 1: Build from Source (Full Build)
```bash
docker build -t browser4-dev .
```

### Option 2: Fast Build (Pre-built JAR)
If you have already built the JAR locally, you can use the fast Dockerfile to skip the Maven build stage:

```bash
# First, build the JAR locally
./mvnw clean package -DskipTests

# Then build the Docker image using the pre-built JAR
docker build -f Dockerfile.fast -t browser4-dev .
```

> 💡 **Fast Build Advantage**: This approach significantly reduces build time by copying the pre-built `browser4/browser4-agents/target/Browser4.jar` instead of rebuilding from source inside Docker.

## 🏠 Run Local Docker Image

```bash
docker run -p 8182:8182 \
  -e OPENROUTER_API_KEY=${OPENROUTER_API_KEY} \
  browser4-dev:latest
```

> 💡 Please make sure you have set `OPENROUTER_API_KEY` environment.

## ✅ Test Browser4 API

```bash
curl -X POST "http://localhost:8182/api/ai/command" \
  -H "Content-Type: text/plain" \
  -d '
    Go to https://www.amazon.com/dp/B08PP5MSVB

    After browser launch: clear browser cookies.
    After page load: scroll to the middle.

    Summarize the product.
    Extract: product name, price, ratings.
    Find all links containing /dp/.
  '
```

## 🚀 Run Hosted Docker Image

```bash
docker run -d -p 8182:8182 \
  -e OPENROUTER_API_KEY=${OPENROUTER_API_KEY} \
  galaxyeye88/browser4:latest
```

## ⚙️ Run with Docker Compose

```bash
export OPENROUTER_API_KEY=your-api-key
# export PROXY_ROTATION_URL=https://your-proxy-provider.com/rotation-endpoint
docker compose up -d
```

## 🌐 Run Docker Compose with Proxy Profile

```bash
docker compose up -d --profile proxy
```

## 🗄️ Run Only MongoDB Service

```bash
docker compose up -d mongodb
```

## 🔗 Run Only ProxyHub Service

```bash
docker compose up -d proxyhub
```
