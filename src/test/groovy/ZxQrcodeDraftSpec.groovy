import org.apache.commons.io.IOUtils
import spock.lang.Specification
import zxqrcode.ZxQrcodeDraft

import java.awt.*
import java.nio.file.Files
import java.nio.file.Paths

class ZxQrcodeDraftSpec extends Specification {

    def "QR Code with Git logo"() {
        given:
        def gitLogoBytes = IOUtils.toByteArray(this.getClass().getResourceAsStream("octocat.png"))
        def zx = ZxQrcodeDraft
                .builder()
                .content("https://github.com/eduardoenemark/zxqrcode")
                .width(500)
                .height(500)
                .backgroundRgb(new Color(255, 213, 245).getRGB())
                .foregroundRgb(Color.BLUE.getRGB())
                .logoBackgroundRgb(new Color(205, 205, 205).getRGB())
                .logoBytes(gitLogoBytes)
                .build();

        expect:
        def imageBytes = zx.generate();
        Files.write(Paths.get("out01.png"), imageBytes)
    }
}
