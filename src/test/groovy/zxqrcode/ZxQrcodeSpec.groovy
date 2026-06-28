package zxqrcode

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import lombok.val
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import spock.lang.Specification

import java.awt.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

import static zxqrcode.ZxQrcode.defaultHints
import static zxqrcode.ZxQrcode.toRgb

class ZxQrcodeSpec extends Specification {

    static def log = LoggerFactory.getLogger(ZxQrcodeSpec)

    static def out = Paths.get("target/qrs")

    def setupSpec() {
        if (Files.notExists(out))
            Files.createDirectory(out)
    }

    def "QR Code generate: #width x #height, errorCorrectionLevel: #errorCorrectionLevel"() {
        given:
        def logoStream = this.getClass().getResourceAsStream("/octocat.png")
        def gitLogoBytes = IOUtils.toByteArray(logoStream)
        def hints = defaultHints()
        hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel)
        def strokeWidth = 8
        def zx = ZxQrcode
                .builder()
                .content(content)
                .width(width)
                .height(height)
                .strokeWidth(strokeWidth)
                .padding(padding)
                .backgroundRgb(toRgb(backgroundRgb))
                .foregroundRgb(toRgb(foregroundRgb))
                .logoBackgroundRgb(toRgb(logoBackgroundRgb))
                .logoBytes(gitLogoBytes)
                .hints(hints)
                .build();

        expect:
        def imageBytes = zx.generate();
        log.info "Bytes: ${imageBytes.length}"
        imageBytes.length > width * 2
        Files.write(out.resolve(Paths.get(filename)), imageBytes)

        cleanup:
        logoStream.close()

        where:
        width | height | backgroundRgb | foregroundRgb | logoBackgroundRgb | padding | filename           | errorCorrectionLevel   | content
        50    | 50     | 0xFFFFFF      | 0x000000      | 0x000000          | 0       | "qr50px-L.png"     | ErrorCorrectionLevel.L | "zxqrcode"
        100   | 100    | 0xFFD5F5      | 0x1100FF      | 0xFFFFFF          | 10      | "qr100px-Q.png"    | ErrorCorrectionLevel.Q | "https://github.com/eduardoenemark/zxqrcode"
        200   | 200    | 0x00FF09      | 0xC15400      | 0xEFEFEF          | 10      | "qr200_01px-L.png" | ErrorCorrectionLevel.L | "https://github.com/eduardoenemark/zxqrcode"
        200   | 200    | 0x00FF09      | 0xC15400      | 0xEFEFEF          | 8       | "qr200_02px-H.png" | ErrorCorrectionLevel.H | 'T'.concat(StringUtils.repeat('x', 100)).concat('Z')
        300   | 300    | 0xFF09        | 0xC15400      | 0xEFEFEF          | 10      | "qr300px-M.png"    | ErrorCorrectionLevel.M | 'T'.concat(StringUtils.repeat('t1234', 100)).concat('Z+++')
        500   | 500    | 0xD1F003      | 0xFF003C      | 0xFFCEDA          | 6       | "qr500px-L.png"    | ErrorCorrectionLevel.L | 'T'.concat(StringUtils.repeat('123', 200)).concat('***')
        500   | 500    | 0xD1F003      | 0xFF003C      | 0xFFCEDA          | 10      | "qr500px-M.png"    | ErrorCorrectionLevel.M | 'T'.concat(StringUtils.repeat('123', 200)).concat('***')
        500   | 500    | 0xD1F003      | 0xFF003C      | 0xFFCEDA          | 10      | "qr500px-Q.png"    | ErrorCorrectionLevel.Q | 'T'.concat(StringUtils.repeat('123', 200)).concat('***')
        500   | 500    | 0xD1F003      | 0xFF003C      | 0xFFCEDA          | 10      | "qr500px-H.png"    | ErrorCorrectionLevel.H | 'T'.concat(StringUtils.repeat('123', 200)).concat('***')
        1000  | 1000   | 0xFFFFFF      | 0x000000      | 0x000000          | 0       | "qr1000px-L.png"   | ErrorCorrectionLevel.L | "github.com/Xyz.ee"
    }

    def "hex to rbg"() {
        expect:
        Color.PINK.getRGB() == toRgb(0xFFAFAF)
        Color.BLACK.getRGB() == toRgb(0x0)
        Color.WHITE.getRGB() == toRgb("FFFFFF")
    }

    def "generate QR code without logo"() {
        given:
        def zx = ZxQrcode
                .builder()
                .content(content)
                .width(width)
                .height(height)
                .logoBytes(null)
                .build()

        when:
        def imageBytes = zx.generate()

        then:
        imageBytes.length > 0

        where:
        width | height | content
        200   | 200    | "no-logo-test"
    }

    def "generate QR code with different sizes"() {
        given:
        def zx = ZxQrcode
                .builder()
                .content(content)
                .width(width)
                .height(height)
                .build()

        when:
        def imageBytes = zx.generate()

        then:
        imageBytes.length > 0

        where:
        width | height | content
        50    | 50     | "tiny"
        1000  | 1000   | "huge"
    }

    def "generate QR code with custom colors"() {
        given:
        def zx = ZxQrcode
                .builder()
                .content(content)
                .width(width)
                .height(height)
                .backgroundRgb(toRgb("0000FF"))
                .foregroundRgb(toRgb("FFFFFF"))
                .build()

        when:
        def imageBytes = zx.generate()

        then:
        imageBytes.length > 0

        where:
        width | height | content
        200   | 200    | "custom-colors"
    }

    def "generate QR code with padding"() {
        given:
        def zx = ZxQrcode
                .builder()
                .content(content)
                .width(width)
                .height(height)
                .padding(padding)
                .build()

        when:
        def imageBytes = zx.generate()

        then:
        imageBytes.length > 0

        where:
        width | height | padding | content
        200   | 200    | 0       | "no-padding"
        200   | 200    | 10      | "with-padding"
        200   | 200    | 20      | "more-padding"
    }

    def "decode QR code content from generated image"() {
        given:
        def zx = ZxQrcode
                .builder()
                .content(content)
                .width(width)
                .height(height)
                .build()

        when:
        def imageBytes = zx.generate()
        def decodedContent = ZxQrcode.getTextOfQrCodeImage(imageBytes)

        then:
        decodedContent == content

        where:
        width | height | content
        200   | 200    | "simple-text"
        300   | 300    | "https://example.com/path?query=1&foo=bar"
        200   | 200    | 'special chars: !@#$%^&*()_+-=[]{}|;:,.<>?/'
    }

    def "decode QR code with logo"() {
        given:
        def logoStream = this.getClass().getResourceAsStream("/octocat.png")
        def gitLogoBytes = IOUtils.toByteArray(logoStream)
        def zx = ZxQrcode
                .builder()
                .content("qr-with-logo-decode")
                .width(300)
                .height(300)
                .logoBytes(gitLogoBytes)
                .build()

        when:
        def imageBytes = zx.generate()
        def decodedContent = ZxQrcode.getTextOfQrCodeImage(imageBytes)

        then:
        decodedContent == "qr-with-logo-decode"

        cleanup:
        logoStream.close()
    }

    def "decode QR code with UTF-8 characters"() {
        given:
        def zx = ZxQrcode
                .builder()
                .content(content)
                .width(300)
                .height(300)
                .build()

        when:
        def imageBytes = zx.generate()
        def decodedContent = ZxQrcode.getTextOfQrCodeImage(imageBytes)

        then:
        decodedContent == content

        where:
        content << [
            "Hello 世界",
            "Café résumé naïve",
            "emoji: 🎉🚀⭐",
            "Russian: Привет мир",
            "Arabic: مرحبا بالعالم"
        ]
    }

    def "decode QR code with custom colors"() {
        given:
        def zx = ZxQrcode
                .builder()
                .content("color-coded")
                .width(300)
                .height(300)
                .backgroundRgb(toRgb("0000FF"))
                .foregroundRgb(toRgb("FFFFFF"))
                .build()

        when:
        def imageBytes = zx.generate()
        def decodedContent = ZxQrcode.getTextOfQrCodeImage(imageBytes)

        then:
        decodedContent == "color-coded"
    }

    def "decode QR code with different error correction levels"() {
        given:
        def hints = defaultHints()
        hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel)
        def zx = ZxQrcode
                .builder()
                .content("ec-level-test")
                .width(300)
                .height(300)
                .hints(hints)
                .build()

        when:
        def imageBytes = zx.generate()
        def decodedContent = ZxQrcode.getTextOfQrCodeImage(imageBytes)

        then:
        decodedContent == "ec-level-test"

        where:
        errorCorrectionLevel << [
            ErrorCorrectionLevel.L,
            ErrorCorrectionLevel.M,
            ErrorCorrectionLevel.Q,
            ErrorCorrectionLevel.H
        ]
    }

    def "decode QR code generated with JPEG format"() {
        given:
        def zx = ZxQrcode
                .builder()
                .content("jpeg-format-test")
                .width(300)
                .height(300)
                .format("jpeg")
                .build()

        when:
        def imageBytes = zx.generate()
        def decodedContent = ZxQrcode.getTextOfQrCodeImage(imageBytes)

        then:
        decodedContent == "jpeg-format-test"
    }

    def "decode QR code with large content"() {
        given:
        def largeContent = 'T'.concat(StringUtils.repeat('a', 500)).concat('Z')
        def zx = ZxQrcode
                .builder()
                .content(largeContent)
                .width(500)
                .height(500)
                .build()

        when:
        def imageBytes = zx.generate()
        def decodedContent = ZxQrcode.getTextOfQrCodeImage(imageBytes)

        then:
        decodedContent == largeContent
    }

    def "throw exception when decoding invalid image"() {
        given:
        def invalidBytes = "not an image".getBytes(StandardCharsets.UTF_8)

        when:
        ZxQrcode.getTextOfQrCodeImage(invalidBytes)

        then:
        thrown(Exception)
    }

    def "throw exception when decoding empty bytes"() {
        given:
        byte[] emptyBytes = new byte[0]

        when:
        ZxQrcode.getTextOfQrCodeImage(emptyBytes)

        then:
        thrown(Exception)
    }

    def "generate QR code with JPEG format"() {
        given:
        def zx = ZxQrcode
                .builder()
                .content("jpeg-test")
                .width(200)
                .height(200)
                .format("jpeg")
                .build()

        when:
        def imageBytes = zx.generate()

        then:
        imageBytes.length > 0
        Files.write(out.resolve(Paths.get("qr200-jpeg.jpg")), imageBytes)
    }

    def "generate QR code with empty logo bytes"() {
        given:
        def zx = ZxQrcode
                .builder()
                .content("empty-logo-test")
                .width(200)
                .height(200)
                .logoBytes(new byte[0])
                .build()

        when:
        def imageBytes = zx.generate()

        then:
        imageBytes.length > 0
    }

    def "generate QR code with custom arc and stroke settings"() {
        given:
        def logoStream = this.getClass().getResourceAsStream("/octocat.png")
        def gitLogoBytes = IOUtils.toByteArray(logoStream)
        def zx = ZxQrcode
                .builder()
                .content("custom-shape-test")
                .width(300)
                .height(300)
                .logoBytes(gitLogoBytes)
                .arcWidth(20)
                .arcHeight(20)
                .strokeWidth(10)
                .padding(15)
                .build()

        when:
        def imageBytes = zx.generate()

        then:
        imageBytes.length > 0
        Files.write(out.resolve(Paths.get("qr300-custom-shape.png")), imageBytes)

        cleanup:
        logoStream.close()
    }

    def "generate QR code with different aspect ratios"() {
        given:
        def zx = ZxQrcode
                .builder()
                .content("aspect-ratio-test")
                .width(width)
                .height(height)
                .build()

        when:
        def imageBytes = zx.generate()

        then:
        imageBytes.length > 0

        where:
        width | height
        150   | 300
        300   | 150
        400   | 200
    }

    def "builder defaults are correct"() {
        given:
        def zx = ZxQrcode.builder().content("defaults-test").build()

        expect:
        zx.getWidth() == 200
        zx.getHeight() == 200
        zx.getPadding() == 8
        zx.getArcWidth() == 10
        zx.getArcHeight() == 10
        zx.getStrokeWidth() == 5
        zx.getFormat() == "png"
        zx.getBackgroundRgb() == Color.white.getRGB()
        zx.getForegroundRgb() == Color.black.getRGB()
        zx.getLogoBackgroundRgb() == Color.white.getRGB()
    }

    def "putLogoOverQrCodeImage with null logo returns unchanged image"() {
        given:
        def zx = ZxQrcode
                .builder()
                .content("logo-null-test")
                .width(200)
                .height(200)
                .logoBytes(null)
                .build()

        def originalImage = MatrixToImageWriter.toBufferedImage(
                new MultiFormatWriter().encode("test", BarcodeFormat.QR_CODE, 200, 200))

        when:
        def resultImage = zx.putLogoOverQrCodeImage(originalImage)

        then:
        resultImage != null
        resultImage.width == originalImage.width
        resultImage.height == originalImage.height
    }

    def "putLogoOverQrCodeImage with logo draws overlay"() {
        given:
        def logoStream = this.getClass().getResourceAsStream("/octocat.png")
        def gitLogoBytes = IOUtils.toByteArray(logoStream)
        def zx = ZxQrcode
                .builder()
                .content("logo-overlay-test")
                .width(300)
                .height(300)
                .logoBytes(gitLogoBytes)
                .build()

        def originalImage = MatrixToImageWriter.toBufferedImage(
                new MultiFormatWriter().encode("test", BarcodeFormat.QR_CODE, 300, 300))

        when:
        def resultImage = zx.putLogoOverQrCodeImage(originalImage)

        then:
        resultImage != null
        resultImage.width == 300
        resultImage.height == 300

        cleanup:
        logoStream.close()
    }

    def "default hints configuration"() {
        given:
        def hints = ZxQrcode.defaultHints()

        expect:
        hints.get(EncodeHintType.CHARACTER_SET) == StandardCharsets.UTF_8
        hints.get(EncodeHintType.ERROR_CORRECTION) == ErrorCorrectionLevel.Q
        hints.get(EncodeHintType.MARGIN) == 2
    }

    def "toRgb conversion edge cases"() {
        expect:
        // Pure colors
        Color.RED.getRGB() == toRgb("FF0000")
        Color.GREEN.getRGB() == toRgb("00FF00")
        Color.BLUE.getRGB() == toRgb("0000FF")

        // Grayscale
        Color.BLACK.getRGB() == toRgb("000000")
        Color.WHITE.getRGB() == toRgb("FFFFFF")

        // Hex int conversion
        toRgb(0xFF0000) == Color.RED.getRGB()
        toRgb(0x00FF00) == Color.GREEN.getRGB()
        toRgb(0x0000FF) == Color.BLUE.getRGB()

        // Lowercase hex string
        toRgb("ff0000") == Color.RED.getRGB()
        toRgb("00ff00") == Color.GREEN.getRGB()
    }

    def "generate QR code with all builder options"() {
        given:
        def logoStream = this.getClass().getResourceAsStream("/github-logo.png")
        def gitLogoBytes = IOUtils.toByteArray(logoStream)
        def hints = defaultHints()
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
        def zx = ZxQrcode
                .builder()
                .content("full-builder-test")
                .width(400)
                .height(400)
                .backgroundRgb(toRgb("F0F0F0"))
                .foregroundRgb(toRgb("1A1A2E"))
                .logoBackgroundRgb(toRgb("FFFFFF"))
                .logoBytes(gitLogoBytes)
                .padding(12)
                .arcWidth(15)
                .arcHeight(15)
                .strokeWidth(8)
                .format("png")
                .hints(hints)
                .build()

        when:
        def imageBytes = zx.generate()

        then:
        imageBytes.length > 400 * 2
        Files.write(out.resolve(Paths.get("qr400-full-builder.png")), imageBytes)

        cleanup:
        logoStream.close()
    }
}
