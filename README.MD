# Facelytics-Android-DemoApp

Facelytics is an SDK to give you the possibility to detect gender and age from a face thanks to artificial intelligence recognition.

## Installation

```
allprojects {
    repositories {
        // ...
        maven { url "http://artifactory.wassa.io/artifactory/public" }
    }
}

dependencies {
    implementation("io.wassa.libraries:facelytics-sdk:2.0.10")
}
```

## Usage

**Warning :** To use the SDK, you need a license key that you can from the support contact@wassa.io

The sample code is commented and show usage examples of the SDK.

The main sdk entry points is the Facelytics object. You have to keep reference of the instance to use the service.

## Documentation

You can find a more detailled documentation of the SDK at the following [address](https://services.wassa.io/sdk/library/android/).
If you need to contact us, send an email at support@wassa.io

## Requirements

* Android 6

## License

Facelytics is available under a commercial license. See the LICENSE file for more info.

## Author

Wassa, contact@wassa.fr
