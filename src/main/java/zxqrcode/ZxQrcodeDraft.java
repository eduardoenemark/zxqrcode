package zxqrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Getter
@ToString
@Builder
@Accessors(chain = true)
public class ZxQrcodeDraft {

    private String content;
    @Builder.Default
    private int width, height;
    @Builder.Default
    private int arcWidth = 10;
    @Builder.Default
    private int arcHeight = 10;
    @Builder.Default
    private int strokeWidth = 5;
    @Builder.Default
    private int padding = 8;
    @Builder.Default
    private String format = "png";
    @Builder.Default
    private Map<EncodeHintType, Object> hints = defaultHints();
    private byte[] logoBytes;
    @Builder.Default
    private int backgroundRgb = Color.white.getRGB();
    @Builder.Default
    private int foregroundRgb = Color.black.getRGB();
    @Builder.Default
    private int logoBackgroundRgb = Color.white.getRGB();
    @Builder.Default
    private BarcodeFormat barcodeFormat = BarcodeFormat.QR_CODE;

    public static Map<EncodeHintType, Object> defaultHints() {
        val hints = new HashMap<EncodeHintType, Object>();
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
        hints.put(EncodeHintType.MARGIN, 0);

        return hints;
    }

    public byte[] generate() throws WriterException, IOException {
        try (var output = new ByteArrayOutputStream()) {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    this.getContent(), this.getBarcodeFormat(), this.getWidth(), this.getHeight(), this.getHints());
            MatrixToImageWriter.writeToStream(bitMatrix, this.getFormat(), output);
            MatrixToImageConfig matrixToImageConfig = new MatrixToImageConfig(this.getBackgroundRgb(), this.getForegroundRgb());

            BufferedImage bufferedImage = putLogoOverQrCodeImage(MatrixToImageWriter.toBufferedImage(bitMatrix, matrixToImageConfig));
            try (var outWithLogo = new ByteArrayOutputStream()) {
                ImageIO.write(bufferedImage, this.getFormat(), outWithLogo);
                return outWithLogo.toByteArray();
            }
        }
    }

    public BufferedImage putLogoOverQrCodeImage(BufferedImage matrixImage) throws IOException {
        val graphics = matrixImage.createGraphics();
        val matrixWidth = matrixImage.getWidth();
        val matrixHeight = matrixImage.getHeight();
        val x = matrixWidth / 5 * 2;
        val y = matrixHeight / 5 * 2;
        val w = matrixWidth / 5;
        val h = matrixHeight / 5;

        graphics.setColor(Color.getColor(String.valueOf(this.getLogoBackgroundRgb())));
        graphics.fillRect(x, y, w, h);

        val logo = ImageIO.read(new ByteArrayInputStream(this.getLogoBytes()));
        graphics.drawImage(logo, x, y, w, h, null);

        val stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_SQUARE, BasicStroke.CAP_SQUARE);
        graphics.setStroke(stroke);

        val round = new RoundRectangle2D.Float(
                x - (padding / 2), y - (padding / 2), w + padding, h + padding, arcWidth, arcHeight);
        graphics.draw(round);

        graphics.dispose();
        matrixImage.flush();
        return matrixImage;
    }
}
