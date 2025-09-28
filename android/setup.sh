#!/bin/bash

# YT-DLP Android Setup Script
# This script sets up the development environment for building the YT-DLP Android APK

set -e

echo "🚀 YT-DLP Android Setup Script"
echo "================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if we're in the android directory
if [ ! -f "build.gradle" ] || [ ! -f "settings.gradle" ]; then
    print_error "Please run this script from the android directory"
    exit 1
fi

print_status "Setting up YT-DLP Android development environment..."

# Check for required tools
print_status "Checking for required tools..."

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f 2)
    print_success "Java found: $JAVA_VERSION"
else
    print_error "Java not found. Please install JDK 17 or later."
    exit 1
fi

# Check Python
if command -v python3 &> /dev/null; then
    PYTHON_VERSION=$(python3 --version)
    print_success "Python found: $PYTHON_VERSION"
else
    print_error "Python 3 not found. Please install Python 3.9 or later."
    exit 1
fi

# Check Android SDK
if [ -z "$ANDROID_HOME" ]; then
    print_warning "ANDROID_HOME not set. Please set it to your Android SDK path."
    if [ -d "$HOME/Android/Sdk" ]; then
        export ANDROID_HOME="$HOME/Android/Sdk"
        print_status "Using default Android SDK path: $ANDROID_HOME"
    else
        print_error "Android SDK not found. Please install Android Studio and set ANDROID_HOME."
        exit 1
    fi
else
    print_success "Android SDK found: $ANDROID_HOME"
fi

# Make gradlew executable
if [ -f "gradlew" ]; then
    chmod +x gradlew
    print_success "Made gradlew executable"
fi

# Setup Python dependencies
print_status "Setting up Python dependencies..."

PYTHON_ASSETS_DIR="app/src/main/assets/python"
mkdir -p "$PYTHON_ASSETS_DIR"

# Install yt-dlp for Python integration
print_status "Installing yt-dlp Python library..."
if command -v pip3 &> /dev/null; then
    pip3 install yt-dlp --target "$PYTHON_ASSETS_DIR/yt_dlp" --upgrade
    print_success "yt-dlp installed to assets directory"
else
    print_warning "pip3 not found. You may need to manually install yt-dlp."
fi

# Create local.properties if it doesn't exist
if [ ! -f "local.properties" ]; then
    print_status "Creating local.properties..."
    echo "sdk.dir=$ANDROID_HOME" > local.properties
    print_success "local.properties created"
fi

# Download Gradle wrapper if not present
if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    print_status "Downloading Gradle wrapper..."
    ./gradlew wrapper
    print_success "Gradle wrapper downloaded"
fi

# Check Android SDK components
print_status "Checking Android SDK components..."
if [ -f "$ANDROID_HOME/tools/bin/sdkmanager" ]; then
    SDKMANAGER="$ANDROID_HOME/tools/bin/sdkmanager"
elif [ -f "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]; then
    SDKMANAGER="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
else
    print_warning "SDK Manager not found. Please install required SDK components manually."
    SDKMANAGER=""
fi

if [ -n "$SDKMANAGER" ]; then
    print_status "Installing required SDK components..."
    
    # Accept licenses
    yes | $SDKMANAGER --licenses > /dev/null 2>&1 || true
    
    # Install required components
    $SDKMANAGER "platform-tools" "build-tools;34.0.0" "platforms;android-34" > /dev/null 2>&1
    print_success "SDK components installed"
fi

# Test Gradle build
print_status "Testing Gradle configuration..."
if ./gradlew tasks > /dev/null 2>&1; then
    print_success "Gradle configuration is valid"
else
    print_warning "Gradle configuration test failed. You may need to sync in Android Studio."
fi

# Create gitignore for Android-specific files
if [ ! -f ".gitignore" ]; then
    print_status "Creating .gitignore..."
    cat > .gitignore << 'EOF'
# Android specific
*.iml
.gradle
/local.properties
/.idea/
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties

# Python cache
__pycache__/
*.py[cod]
*$py.class

# Logs
*.log

# APK files
*.apk
*.aab

# Signing files
*.jks
*.keystore

# Proguard
/proguard/

# OS generated files
.DS_Store
.DS_Store?
._*
.Spotlight-V100
.Trashes
ehthumbs.db
Thumbs.db
EOF
    print_success ".gitignore created"
fi

# Setup complete
echo ""
print_success "🎉 Setup complete!"
echo ""
echo "Next steps:"
echo "1. Open this directory in Android Studio"
echo "2. Wait for Gradle sync to complete"
echo "3. Build the project: ./gradlew assembleDebug"
echo "4. Install on device: adb install app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "For more information, see BUILD.md and FEATURES.md"
echo ""

# Offer to build immediately
read -p "Would you like to build the debug APK now? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    print_status "Building debug APK..."
    if ./gradlew assembleDebug; then
        print_success "Build completed successfully!"
        APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
        if [ -f "$APK_PATH" ]; then
            print_success "APK created at: $APK_PATH"
            echo ""
            echo "To install on connected device:"
            echo "adb install $APK_PATH"
        fi
    else
        print_error "Build failed. Check the output above for errors."
        exit 1
    fi
fi

print_success "All done! Happy coding! 🚀"