# Billy - Android Billing Library

Billy is a modern Android library that simplifies BillingClient integration for Compose apps. It consists of a core library module (`flow`) and sample applications (`app`).

**ALWAYS reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.**

## Working Effectively

### Bootstrap, Build, and Test
- **Prerequisites**: Java 21 (required), Android SDK 34-36, Gradle 9.0.0+
- **Initial setup**: No additional setup scripts required - Gradle handles all dependencies
- **Clean and build**: `./gradlew clean build` -- takes 1m30s to complete. **NEVER CANCEL**. Set timeout to 120+ minutes.
- **Incremental builds**: `./gradlew build` -- takes 2-8s when up-to-date
- **CI verification**: `./gradlew check --stacktrace` -- includes linting, tests, and all checks
- **Unit tests only**: `./gradlew test` -- takes 13s from clean. **NEVER CANCEL**. Set timeout to 30+ minutes.
- **Linting**: `./gradlew detekt` -- runs Kotlin linting with auto-fix enabled
- **Build sample APK**: `./gradlew :app:assembleDebug` -- creates `app/build/outputs/apk/debug/app-debug.apk`

### Sample Application Testing
- **Main Activity**: Shows menu to choose between "Advance Sample" and "Compose Sample"
- **Compose Sample**: Demonstrates billing flow using Compose UI (`ComposeBillingScreen.kt`)
- **Advance Sample**: Shows traditional Android View-based billing integration
- **Test Configuration**: Add `APPLICATION_ID=your.app.id` to `local.properties` to test with your own billing products

## Validation

**CRITICAL**: Always manually validate any billing-related code changes by:

1. **Build and run the sample app**: `./gradlew :app:assembleDebug`
2. **Test complete billing scenarios**: 
   - **Main Menu**: Launch app, verify "Advance Sample" and "Compose Sample" buttons work
   - **Compose Sample**: Navigate to Compose sample, verify product status shows (Loading -> Available/Unavailable/Owned), test purchase button interactions
   - **Advance Sample**: Navigate to traditional view sample, verify "Early bird product: [status]" text updates, test FAB purchase button
   - **Product Status Flow**: Verify status transitions work correctly for subscription products
3. **Manual testing scenarios**:
   - Test with default product ID "early_bird" (should show Available in debug mode)
   - Create `local.properties` with `APPLICATION_ID=your.app.id` to test real billing products
   - Verify library auto-initialization works without manual BillingProvider setup
4. **Run all verification steps**: `./gradlew check --stacktrace` includes lint, tests, API validation
5. **Always run linting before committing**: `./gradlew detekt --auto-correct`
6. **API compatibility**: `./gradlew apiCheck` verifies public API doesn't break

## Common Tasks

### Repository Structure
```
.
├── README.md
├── CONTRIBUTING.md
├── build.gradle.kts          # Root build configuration
├── settings.gradle.kts       # Multi-module setup
├── gradle.properties         # Build properties
├── app/                      # Sample application module
│   ├── build.gradle.kts     # App module build
│   └── src/main/java/se/warting/sampleapp/
│       ├── MainActivity.kt   # Main sample app entry
│       ├── compose/ComposeActivity.kt    # Compose billing demo
│       └── advance/AdvanceActivity.kt    # Traditional view demo
├── flow/                     # Core library module
│   ├── build.gradle.kts     # Library module build
│   ├── api/                 # API compatibility files
│   └── src/main/java/se/warting/billy/flow/
│       ├── BillingProvider.kt    # Main library entry point
│       ├── Product.kt           # Product types (Subscription, InApp)
│       ├── ProductStatus.kt     # Status states
│       └── internal/           # Internal implementation
├── .github/
│   └── workflows/pr.yml     # CI configuration
├── config/detekt/           # Kotlin linting configuration
└── scripts/                 # Utility scripts
```

### Key Library Components
- **BillingProvider**: Main entry point - auto-initializes via startup-runtime
- **Product.Subscription/InApp**: Product type definitions
- **ProductStatus**: Available/Loading/Unavailable/Owned states
- **Sample Usage**: See `ComposeBillingScreen.kt` for complete integration example

### Build Configuration
- **Multi-module**: Root project + app + flow modules
- **Android SDK**: Targets API 34-36, minimum API 21
- **Kotlin**: Version 2.2.0 with explicit API mode for library
- **Compose**: Uses BOM 2025.08.00 in sample app
- **Publishing**: Configured for Maven Central via Gradle plugin

### CI Pipeline
- **Runner**: Uses custom "tart" runners (likely macOS)
- **Java**: OpenJDK 21 (AdoptOpenJDK distribution)
- **Cache**: Gradle build cache enabled for dependencies
- **Checks**: `./gradlew check --stacktrace` runs all verification
- **Timeout**: CI build completes in ~2-5 minutes typically

## Timing Expectations

- **NEVER CANCEL**: Build may take up to 1m30s, check task up to 27s from clean state
- **Clean**: `./gradlew clean` -- 1s
- **Full clean build**: `./gradlew clean build` -- 1m30s (**Set timeout to 120+ minutes**)
- **Incremental build**: `./gradlew build` -- 2-8s when up-to-date
- **Tests from clean**: `./gradlew clean test` -- 13s (**Set timeout to 30+ minutes**)
- **Check from clean**: `./gradlew clean check` -- 27s (**Set timeout to 60+ minutes**)
- **Lint**: `./gradlew detekt` -- 1-2s when up-to-date
- **APK build**: `./gradlew :app:assembleDebug` -- 8s when dependencies built

## Development Workflow

### Making Library Changes
1. **Always check `flow/src/main/java/se/warting/billy/flow/` for core logic**
2. **Test changes**: Modify sample in `app/src/main/java/se/warting/sampleapp/compose/ComposeBillingScreen.kt`
3. **API validation**: Run `./gradlew apiCheck` after public API changes
4. **Lint fixes**: Run `./gradlew detekt --auto-correct` before committing

### Adding New Features
- **Product types**: Extend `Product.kt` sealed classes
- **Status states**: Modify `ProductStatus.kt` sealed classes  
- **Internal logic**: Update files in `flow/src/main/java/se/warting/billy/flow/internal/`
- **Sample usage**: Add examples to `ComposeBillingScreen.kt`

### Common File Locations
- **Main library API**: `flow/src/main/java/se/warting/billy/flow/BillingProvider.kt`
- **Complete usage example**: `app/src/main/java/se/warting/sampleapp/compose/ComposeBillingScreen.kt`
- **Build configuration**: `build.gradle.kts` (root), `app/build.gradle.kts`, `flow/build.gradle.kts`
- **Lint rules**: `config/detekt/detekt.yml`
- **CI setup**: `.github/workflows/pr.yml`

### Troubleshooting
- **Dependency resolution**: Clear gradle cache with `./gradlew clean --build-cache`
- **Configuration cache**: Enable for faster builds: `./gradlew --configuration-cache build`
- **API changes**: Update API files: `./gradlew apiDump` then commit changes
- **Lint issues**: Auto-fix with `./gradlew detekt --auto-correct`
- **Build failures**: Check `build/reports/` directories for detailed error reports
- **Sample app issues**: Verify `local.properties` has correct `APPLICATION_ID` for billing products
- **Dokka warnings**: Library uses deprecated V1 plugin - warnings are expected and safe to ignore