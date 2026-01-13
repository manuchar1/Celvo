# Provisioning Module

This module handles digital service fulfillment from various third-party vendors (e.g., eSIM providers, VPN providers).

## Architecture

The module follows **Hexagonal (Ports & Adapters) Architecture** to decouple business logic from vendor implementations.

```
provisioning/
├── domain/                          # Core business logic (vendor-agnostic)
│   └── esim/
│       ├── models/                  # Domain entities
│       │   ├── EsimBundle.kt
│       │   ├── PurchaseResult.kt
│       │   └── UsageInfo.kt
│       └── ports/                   # Interfaces (contracts)
│           └── EsimProvider.kt
│
├── infrastructure/                  # Vendor-specific implementations
│   └── esim/
│       └── esimgo/
│           ├── adapter/             # Domain interface implementation
│           │   ├── EsimGoAdapter.kt
│           │   └── EsimGoMapper.kt
│           ├── client/              # HTTP client
│           │   └── EsimGoClient.kt
│           ├── config/              # Configuration
│           │   ├── EsimGoConfig.kt
│           │   └── EsimGoProperties.kt
│           └── dto/                 # API DTOs
│               └── EsimGoDto.kt
│
└── application/                     # Application services
    └── EsimService.kt
```

## Components

### Domain Layer

- **Models**: Clean, vendor-agnostic domain entities (`EsimBundle`, `PurchaseResult`, `UsageInfo`)
- **Ports**: `EsimProvider` interface defining the contract for any eSIM provider

### Infrastructure Layer

- **EsimGoClient**: Low-level HTTP client using Spring's `RestClient`
  - Handles authentication (X-API-KEY header)
  - Error handling and response parsing
  - Communicates with eSIMGo API v2.5

- **EsimGoAdapter**: Implementation of `EsimProvider` for eSIMGo
  - Translates between domain models and eSIMGo DTOs
  - Orchestrates client calls

- **EsimGoMapper**: Converts between DTOs and domain models

### Configuration

Configuration is managed through `application.yml`:

```yaml
esimgo:
  base-url: https://api.esimgo.com
  api-key: ${uedANQhuW5aZMw3TUCVQZeuVOYavTvuzl_XHS3jW}
  timeout-seconds: 30
```

Set the `ESIMGO_API_KEY` environment variable in production.

## Usage

### Get Available Bundles

```kotlin
@Autowired
lateinit var esimService: EsimService

val bundles = esimService.getAvailableBundles()
bundles.forEach { bundle ->
    println("${bundle.name}: ${bundle.dataAmount.toReadableString()} - ${bundle.price.amount} ${bundle.price.currency}")
}
```

### Purchase New eSIM

```kotlin
val result = esimService.purchaseNewEsim(
    bundleId = "bundle-123",
    customerEmail = "customer@example.com"
)

println("Order ID: ${result.orderId}")
println("ICCID: ${result.iccid}")
println("QR Code: ${result.qrCode}")
```

### Top-Up Existing eSIM

```kotlin
val result = esimService.topUpEsim(
    iccid = "89012345678901234567",
    bundleId = "topup-bundle-456"
)
```

### Check Usage

```kotlin
val usage = esimService.checkUsage(iccid = "89012345678901234567")
println("Used: ${usage.dataUsed.toReadableString()}")
println("Remaining: ${usage.dataRemaining.toReadableString()}")
println("Status: ${usage.status}")
```

## Adding New Providers

To add support for another eSIM provider:

1. Create new adapter package: `infrastructure/esim/newprovider/`
2. Implement DTOs for the provider's API
3. Create a client class (similar to `EsimGoClient`)
4. Create an adapter implementing `EsimProvider`
5. Create a mapper for DTO ↔ domain conversion
6. Add configuration properties

The domain layer remains unchanged, ensuring clean separation of concerns.

## Testing

Run tests with:

```bash
./gradlew :provisioning:test
```

## Dependencies

- Spring Boot 4.0.0
- Spring Web (RestClient)
- Jackson (JSON serialization)
- Kotlin 2.2.0

## API Reference

### EsimProvider Interface

```kotlin
interface EsimProvider {
    fun getCatalogue(): List<EsimBundle>
    fun orderNewEsim(bundleId: String, email: String? = null): PurchaseResult
    fun applyTopUp(iccid: String, bundleId: String): PurchaseResult
    fun getUsage(iccid: String): UsageInfo
    fun getProviderName(): String
}
```

## Error Handling

The `EsimGoClient` throws `EsimGoApiException` for API errors, which includes:
- Error message
- Error code (if provided by API)
- HTTP status code

Handle exceptions appropriately in your application layer.
