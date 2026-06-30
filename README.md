<!-- This project is licensed under the GNU General Public License v3.0 — see the full license text above. -->

# zxqrcode

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Java 17](https://img.shields.io/badge/Java-17-blue.svg)](https://www.oracle.com/java/)
[![ZXing 3.5.4](https://img.shields.io/badge/ZXing-3.5.4-orange.svg)](https://github.com/zxing/zxing)

> **Example project demonstrating how to use the ZXing library for QR code generation and decoding.**

---

## Important Notice

This is an **example project** intended to showcase practical usage of the [ZXing](https://github.com/zxing/zxing) library. It is not a production-ready library or SDK. Use it as a reference for integrating ZXing into your own projects.

### QR Code Model 2 Support

This project supports **only QR Code Model 2** (ISO/IEC 18004:2006), which is the standard QR code format used worldwide. It is compatible with:

- **ISO/IEC 18004:2006** — Original specification
- **ISO/IEC 18004:2015** — Amendments and updates
- **ISO/IEC 18004:2024** — Latest compatibility

QR Code Model 2 is the most widely deployed QR code format, supporting alphanumeric, numeric, binary, and Kanji encoding with configurable error correction.

---

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
  - [Basic QR Code Generation](#basic-qr-code-generation)
  - [QR Code with Logo Overlay](#qr-code-with-logo-overlay)
  - [Customized QR Code with Colors](#customized-qr-code-with-colors)
  - [Decoding a QR Code](#decoding-a-qr-code)
  - [Using Default Hints](#using-default-hints)
- [API Reference](#api-reference)
  - [Static Methods](#static-methods)
  - [Builder Properties](#builder-properties)
  - [Instance Methods](#instance-methods)
- [Error Correction Levels](#error-correction-levels)
- [Dependencies](#dependencies)
- [Building the Project](#building-the-project)
- [Running Tests](#running-tests)
- [License](#license)

---

## Features

- **Fluent builder API** for intuitive QR code generation
- **Logo overlay** with rounded borders on generated QR codes
- **Custom colors** via hex string conversion (`"FF0000"` to RGB integer)
- **Multiple output formats** — PNG and JPEG
- **QR code text decoding** from image byte arrays
- **Configurable error correction levels** (L, M, Q, H)
- **Adjustable dimensions**, margins, and styling parameters

---

## Prerequisites

- **Java 17** or later
- **Maven 3.6+** for building the project

---

## Installation

Add the following dependencies to your `pom.xml`:

```xml
<dependencies>
    <!-- ZXing Core -->
    <dependency>
        <groupId>com.google.zxing</groupId>
        <artifactId>core</artifactId>
        <version>3.5.4</version>
    </dependency>

    <!-- ZXing JavaSE (for barcode writing/reading) -->
    <dependency>
        <groupId>com.google.zxing</groupId>
        <artifactId>javase</artifactId>
        <version>3.5.4</version>
    </dependency>

    <!-- Lombok (optional, for code generation) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.46</version>
        <scope>provided</scope>
    </dependency>

    <!-- Commons IO (for file I/O utilities) -->
    <dependency>
        <groupId>commons-io</groupId>
        <artifactId>commons-io</artifactId>
        <version>2.22.0</version>
    </dependency>

    <!-- SLF4J API (for logging) -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>2.0.16</version>
    </dependency>
</dependencies>
```

---

## Usage

### Basic QR Code Generation

Generate a simple QR code with default settings:

```java
import br.com.eduardoenemark.zxqrcode.ZxQrcode;

byte[] qrImage = ZxQrcode.builder()
    .content("Hello World")
    .generate();
```

### QR Code with Logo Overlay

Embed a logo image in the center of the QR code:

```java
import br.com.eduardoenemark.zxqrcode.ZxQrcode;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

BufferedImage logo = ImageIO.read(new File("logo.png"));

byte[] qrImage = ZxQrcode.builder()
    .content("https://example.com")
    .width(400)
    .height(400)
    .putLogoOverQrCodeImage(logo)
    .generate();
```

### Customized QR Code with Colors

Customize the foreground and background colors using hex strings:

```java
import br.com.eduardoenemark.zxqrcode.ZxQrcode;

byte[] qrImage = ZxQrcode.builder()
    .content("Custom QR")
    .width(300)
    .height(300)
    .format("jpeg")
    .foregroundRgb(ZxQrcode.toRgb("0000FF"))   // Blue foreground
    .backgroundRgb(ZxQrcode.toRgb("FFFFFF"))   // White background
    .generate();
```

### Decoding a QR Code

Decode text content from a QR code image byte array:

```java
import br.com.eduardoenemark.zxqrcode.ZxQrcode;

String text = ZxQrcode.getTextOfQrCodeImage(qrImageBytes);
System.out.println("Decoded text: " + text);
```

### Using Default Hints

Apply default encoding hints (UTF-8, Q error correction, 2-module margin):

```java
import br.com.eduardoenemark.zxqrcode.ZxQrcode;
import com.google.zxing.EncodeHintType;

import java.util.Map;

Map<EncodeHintType, Object> hints = ZxQrcode.defaultHints();

byte[] qrImage = ZxQrcode.builder()
    .content("With defaults")
    .hints(hints)
    .generate();
```

---

## API Reference

The project exposes a single public class: **`br.com.eduardoenemark.zxqrcode.ZxQrcode`** — a fluent builder for QR code generation and decoding.

### Static Methods

| Method | Description |
|--------|-------------|
| `defaultHints()` | Returns default encoding hints (UTF-8 charset, Q error correction level, 2-module margin) |
| `toRgb(String hex)` | Converts a hex string like `"FF0000"` to an RGB integer |
| `toRgb(int hex)` | Converts a hex integer to an RGB integer |
| `getTextOfQrCodeImage(byte[] imageBytes)` | Decodes and returns the text content from QR code image bytes |

### Builder Properties

The builder supports 14 configurable properties:

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `content` | `String` | *(required)* | The text/data to encode in the QR code |
| `width` | `int` | `200` | Width of the generated QR code image in pixels |
| `height` | `int` | `200` | Height of the generated QR code image in pixels |
| `arcWidth` | `int` | `10` | Arc width for rounded logo corners |
| `arcHeight` | `int` | `10` | Arc height for rounded logo corners |
| `strokeWidth` | `int` | `5` | Border stroke width around the logo |
| `padding` | `int` | `8` | Padding between QR code and image edges |
| `format` | `String` | `"png"` | Output image format (`"png"` or `"jpeg"`) |
| `hints` | `Map<EncodeHintType, Object>` | *(null)* | ZXing encoding hints map |
| `logoBytes` | `byte[]` | *(null)* | Raw logo image bytes |
| `backgroundRgb` | `int` | White (`0xFFFFFF`) | Background color as RGB integer |
| `foregroundRgb` | `int` | Black (`0x000000`) | Foreground (module) color as RGB integer |
| `logoBackgroundRgb` | `int` | White (`0xFFFFFF`) | Background behind the logo |
| `barcodeFormat` | `BarcodeFormat` | `QR_CODE` | Barcode format (fixed to QR Code Model 2) |

### Instance Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `generate()` | `byte[]` | Generates the QR code image and returns it as a byte array (PNG or JPEG based on `format`) |
| `putLogoOverQrCodeImage(BufferedImage logo)` | `ZxQrcode.Builder` | Overlays a logo onto the QR code with rounded borders; returns the builder for chaining |

---

## Error Correction Levels

QR Code Model 2 supports four error correction levels. Higher levels provide more redundancy at the cost of reduced data capacity.

| Level | Capacity Recovery | Description |
|-------|-------------------|-------------|
| **L** | 7% | Low recovery — suitable for clean environments |
| **M** | 15% | Medium recovery — default balance of capacity and robustness |
| **Q** | 25% | Quartile recovery — good for logos or partial damage |
| **H** | 30% | High recovery — maximum redundancy, lowest data capacity |

---

## Dependencies

### Compile Scope

| Dependency | Version | Purpose |
|------------|---------|---------|
| `com.google.zxing:core` | 3.5.4 | Core ZXing barcode encoding/decoding library |
| `com.google.zxing:javase` | 3.5.4 | Java SE extensions for image I/O and barcode handling |
| `org.projectlombok:lombok` | 1.18.36 | Code generation (compile-time annotation processor) |
| `commons-io:commons-io` | 2.22.0 | File and stream I/O utilities |
| `org.slf4j:slf4j-api` | 2.0.16 | Logging facade |

### Test Scope

| Dependency | Version | Purpose |
|------------|---------|---------|
| `org.apache.groovy:groovy-all` | 4.0.29 | Groovy language support for Spock tests |
| `org.spockframework:spock-core` | 2.4-groovy-4.0 | BDD testing framework |
| `junit:junit` | 4.13.2 | JUnit 4 test runner |
| `org.apache.commons:commons-lang3` | 3.20.0 | Common Java utility methods |

---

## Building the Project

```bash
# Clean and package the project
mvn clean package

# Install to local Maven repository
mvn install
```

---

## Running Tests

This project uses **Spock** (BDD framework) with **Groovy 4.0** and **JUnit 4.13.2**.

```bash
# Run all tests
mvn test
```

Test output images are written to the `target/qrs/` directory for visual inspection.

---

## License

This project is licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0).

See the full license text at the top of this file for details on usage and distribution rights.

---

*This example project is maintained by [@eduardoenemark](https://t.me/eduardoenemark). For issues and contributions, please visit the [GitHub repository](https://github.com/eduardoenemark/zxqrcode).*
