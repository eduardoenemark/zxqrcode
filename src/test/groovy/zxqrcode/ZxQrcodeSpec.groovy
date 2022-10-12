package zxqrcode

import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import lombok.val
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import spock.lang.Specification

import java.awt.*
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
}
