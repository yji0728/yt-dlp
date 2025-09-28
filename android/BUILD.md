# Building YT-DLP Android APK

This document provides detailed instructions for building the YT-DLP Android APK.

## Prerequisites

### Required Software
- **Android Studio** 2023.1.1 (Giraffe) or later
- **JDK 17** or later
- **Android SDK** with API Level 34
- **Python 3.9+** (for yt-dlp integration)

### Android SDK Requirements
- Android SDK Platform-tools 34.0.0+
- Android SDK Build-tools 34.0.0+
- Android Support Repository
- Google Repository

## Setup Instructions

### 1. Clone and Setup
```bash
git clone <repository-url>
cd yt-dlp/android
```

### 2. Install Python Dependencies
```bash
# Install yt-dlp in the assets directory for packaging
mkdir -p app/src/main/assets/python/yt_dlp
pip install yt-dlp --target app/src/main/assets/python/yt_dlp
```

### 3. Android Studio Setup
1. Open Android Studio
2. Select "Open an existing Android Studio project"
3. Navigate to the `android` directory and open it
4. Wait for Gradle sync to complete
5. Download any missing SDK components when prompted

### 4. Configure Build Environment
```bash
# Make gradlew executable (Linux/Mac)
chmod +x gradlew

# Set ANDROID_HOME environment variable
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

## Building the APK

### Command Line Build
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing configuration)
./gradlew assembleRelease

# Clean build
./gradlew clean

# Run tests
./gradlew test
```

### Android Studio Build
1. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**
2. Wait for build to complete
3. APK will be generated in `app/build/outputs/apk/`

## Build Variants

### Debug Build
- Includes debugging information
- No code obfuscation
- Larger file size
- Can be installed alongside release version

### Release Build
- Optimized and obfuscated
- Smaller file size
- Requires signing configuration
- Production-ready

## Signing Configuration

For release builds, configure signing in `app/build.gradle`:

```gradle
android {
    signingConfigs {
        release {
            storeFile file("path/to/keystore.jks")
            storePassword "store_password"
            keyAlias "key_alias"
            keyPassword "key_password"
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            // ... other config
        }
    }
}
```

## Python Integration Notes

### Chaquopy Integration
This project uses Chaquopy for Python integration. To properly set it up:

1. Add Chaquopy plugin to `app/build.gradle`:
```gradle
plugins {
    id 'com.chaquo.python'
}
```

2. Configure Python requirements:
```gradle
python {
    buildPython "python3.9"
    pip {
        install "yt-dlp"
    }
}
```

### Alternative: Manual Python Setup
If not using Chaquopy, ensure yt-dlp is properly bundled:
```bash
pip install yt-dlp --target app/src/main/assets/python/
```

## Troubleshooting

### Common Build Issues

**Gradle Sync Failed**
- Check internet connection
- Update Android Gradle Plugin
- Invalidate caches: File > Invalidate Caches and Restart

**Missing SDK Components**
- Open SDK Manager in Android Studio
- Install required SDK platforms and build tools
- Accept all license agreements

**Python Integration Issues**
- Verify Python installation
- Check yt-dlp installation in assets directory
- Review Python path configuration

**Permission Errors (Linux/Mac)**
```bash
chmod +x gradlew
```

**Out of Memory Errors**
Add to `gradle.properties`:
```
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=512m
```

### Build Performance Optimization
```gradle
# In gradle.properties
org.gradle.parallel=true
org.gradle.daemon=true
org.gradle.configureondemand=true
android.enableJetifier=true
android.useAndroidX=true
```

## Output Files

After successful build:
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release.apk`
- **Mapping files**: `app/build/outputs/mapping/release/`

## Installation

### Using ADB
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Manual Installation
1. Enable "Unknown Sources" in Android Settings
2. Transfer APK to device
3. Open APK file to install

## Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Manual Testing Checklist
- [ ] App launches successfully
- [ ] URL input accepts valid video URLs
- [ ] Download starts and shows progress
- [ ] Notifications appear during download
- [ ] Downloads complete successfully
- [ ] Downloaded files are accessible
- [ ] Error handling works properly
- [ ] Permissions are requested appropriately

## Distribution

### Google Play Store
1. Generate signed release APK
2. Create app listing in Play Console
3. Upload APK and configure store listing
4. Submit for review

### Alternative Distribution
- Direct APK download from website
- Third-party app stores (F-Droid, etc.)
- Enterprise distribution

## Next Steps

After successful build:
1. Test on various Android devices and versions
2. Optimize performance and battery usage
3. Add more video platform support
4. Implement additional features (playlists, quality selection)
5. Consider Play Store publication