package zxqrcode;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.*;
import lombok.experimental.Accessors;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for generating and decoding QR codes with customizable appearance.
 * <p>
 * This class provides a fluent builder API to create QR code images with options for
 * dimensions, colors, logo overlay, and error correction levels. Generated QR codes
 * can be decoded back to extract the original content.
 * <p>
 * Example usage:
 * <pre>{@code
 * byte[] qrImage = ZxQrcode.builder()
 *     .content("Hello World")
 *     .width(300)
 *     .height(300)
 *     .foregroundRgb(ZxQrcode.toRgb("FF0000"))
 *     .build()
 *     .generate();
 *
 * String decoded = ZxQrcode.getTextOfQrCodeImage(qrImage);
 * }</pre>
 *
 * @author Eduardo Enemark
 * @see <a href="https://github.com/eduardoenemark/zxqrcode">zxqrcode on GitHub</a>
 */
@Setter
@Getter
@ToString
@Builder
@Accessors(chain = true)
public class ZxQrcode {

    /**
     * The text or data content to encode in the QR code.
     */
    private String content;

    /**
     * Width of the generated QR code image in pixels.
     * Default: 200
     */
    @Builder.Default
    private int width = 200;

    /**
     * Height of the generated QR code image in pixels.
     * Default: 200
     */
    @Builder.Default
    private int height = 200;

    /**
     * Width of the rounded rectangle arc for the logo border in pixels.
     * Default: 10
     */
    @Builder.Default
    private int arcWidth = 10;

    /**
     * Height of the rounded rectangle arc for the logo border in pixels.
     * Default: 10
     */
    @Builder.Default
    private int arcHeight = 10;

    /**
     * Stroke width (border thickness) around the logo in pixels.
     * Default: 5
     */
    @Builder.Default
    private int strokeWidth = 5;

    /**
     * Padding around the logo border in pixels.
     * Default: 8
     */
    @Builder.Default
    private int padding = 8;

    /**
     * Output image format (e.g., "png", "jpeg").
     * Default: "png"
     */
    @Builder.Default
    private String format = "png";

    /**
     * Encoding hints for the QR code generation, including character set,
     * error correction level, and margin.
     * Default: UTF-8 encoding, HIGH error correction (Q), 2 modules margin
     */
    @Builder.Default
    private Map<EncodeHintType, Object> hints = defaultHints();

    /**
     * Optional logo bytes to overlay on the QR code image.
     * If null or empty, no logo will be drawn.
     */
    private byte[] logoBytes;

    /**
     * Background color of the QR code as an RGB integer value.
     * Default: white (0xFFFFFF)
     */
    @Builder.Default
    private int backgroundRgb = Color.white.getRGB();

    /**
     * Foreground color of the QR code as an RGB integer value.
     * Default: black (0x000000)
     */
    @Builder.Default
    private int foregroundRgb = Color.black.getRGB();

    /**
     * Background color behind the logo overlay as an RGB integer value.
     * This creates a solid background for the logo area.
     * Default: white (0xFFFFFF)
     */
    @Builder.Default
    private int logoBackgroundRgb = Color.white.getRGB();

    /**
     * The barcode format to encode. Only QR_CODE is supported.
     * Default: BarcodeFormat.QR_CODE
     */
    @Builder.Default
    private BarcodeFormat barcodeFormat = BarcodeFormat.QR_CODE;

    /**
     * Creates a default configuration of encoding hints for QR code generation.
     * <p>
     * The default hints include:
     * <ul>
     *   <li>CHARACTER_SET: UTF-8</li>
     *   <li>ERROR_CORRECTION: HIGH (Q level - can recover up to 25% data loss)</li>
     *   <li>MARGIN: 2 modules</li>
     * </ul>
     *
     * @return a Map of EncodeHintType to Object containing default encoding settings
     */
    public static Map<EncodeHintType, Object> defaultHints() {
        val hints = new HashMap<EncodeHintType, Object>();
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
        hints.put(EncodeHintType.MARGIN, 2);

        return hints;
    }

    /**
     * Converts a hexadecimal color string to an RGB integer value.
     * <p>
     * The hex string can be in the format "FFFFFF" or "FF0000".
     *
     * @param hex a hexadecimal color string (e.g., "FF0000" for red)
     * @return the RGB integer representation of the color
     * @throws NumberFormatException if the string is not a valid hexadecimal number
     */
    public static int toRgb(final String hex) {
        return toRgb(Integer.parseInt(hex, 16));
    }

    /**
     * Converts a hexadecimal integer to an RGB integer value.
     * <p>
     * Extracts the red, green, and blue components from the hex value
     * and creates a Color object to get the standard RGB integer.
     *
     * @param hex a hexadecimal color value (e.g., 0xFF0000 for red)
     * @return the RGB integer representation of the color
     */
    public static int toRgb(final int hex) {
        return new Color((hex >> 16) & 0xFF, (hex >> 8) & 0xFF, hex & 0xFF).getRGB();
    }

    /**
     * Generates a QR code image based on the configured parameters.
     * <p>
     * This method:
     * <ol>
     *   <li>Encodes the content into a BitMatrix using ZXing</li>
     *   <li>Converts the BitMatrix to a BufferedImage with specified colors</li>
     *   <li>Optionally overlays a logo if logoBytes is provided</li>
     *   <li>Writes the result to the specified image format (PNG or JPEG)</li>
     * </ol>
     *
     * @return byte array containing the QR code image in the specified format
     * @throws WriterException if encoding fails
     * @throws IOException if image writing fails
     */
    public byte[] generate() throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                this.getContent(), this.getBarcodeFormat(), this.getWidth(), this.getHeight(), this.getHints());
        BufferedImage bufferedImage;
        if (this.getBackgroundRgb() == Color.white.getRGB() && this.getForegroundRgb() == Color.black.getRGB()) {
            bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        } else {
            MatrixToImageConfig matrixToImageConfig = new MatrixToImageConfig(this.getBackgroundRgb(), this.getForegroundRgb());
            bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix, matrixToImageConfig);
        }
        bufferedImage = putLogoOverQrCodeImage(bufferedImage);
        try (var outWithLogo = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, this.getFormat(), outWithLogo);
            return outWithLogo.toByteArray();
        }
    }

    /**
     * Overlays a logo image onto the QR code matrix image.
     * <p>
     * The logo is positioned in the center-right area of the QR code (at 2/5 width and height).
     * A background rectangle is drawn behind the logo, and a rounded border is added.
     * <p>
     * If logoBytes is null or empty, the original matrix image is returned unchanged.
     *
     * @param matrixImage the QR code BufferedImage to overlay the logo onto
     * @return the BufferedImage with the logo overlay (or the original if no logo provided)
     * @throws IOException if reading the logo bytes fails
     */
    public BufferedImage putLogoOverQrCodeImage(BufferedImage matrixImage) throws IOException {
        if (this.getLogoBytes() == null || this.getLogoBytes().length == 0) {
            return matrixImage;
        }
        val graphics = matrixImage.createGraphics();
        val matrixWidth = matrixImage.getWidth();
        val matrixHeight = matrixImage.getHeight();
        val x = matrixWidth / 5 * 2;
        val y = matrixHeight / 5 * 2;
        val w = matrixWidth / 5;
        val h = matrixHeight / 5;

        graphics.setColor(new Color(this.getLogoBackgroundRgb()));
        graphics.fillRect(x, y, w, h);

        val logo = ImageIO.read(new ByteArrayInputStream(this.getLogoBytes()));
        graphics.drawImage(logo, x, y, w, h, null);

        val stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_SQUARE, BasicStroke.CAP_SQUARE);
        graphics.setStroke(stroke);

        val round = new RoundRectangle2D.Float(
                x - (this.getPadding() / 2), y - (this.getPadding() / 2), w + this.getPadding(), h + this.getPadding(), arcWidth, arcHeight);
        graphics.draw(round);

        graphics.dispose();
        return matrixImage;
    }

    /**
     * Decodes and extracts the text content from a QR code image byte array.
     * <p>
     * This method reads the image bytes, converts them to a binary bitmap,
     * and uses ZXing to decode the QR code content.
     * <p>
     * Note: The image must be a valid QR code image (PNG or JPEG format).
     *
     * @param imageBytes byte array containing the QR code image data
     * @return the decoded text content from the QR code
     * @throws NotFoundException if no QR code is found in the image
     * @throws IOException if the image bytes cannot be read
     */
    public static String getTextOfQrCodeImage(final byte[] imageBytes) throws NotFoundException, IOException {
        try (var imgStream = new ByteArrayInputStream(imageBytes)) {
            val bufferedImage = ImageIO.read(imgStream);
            if (bufferedImage == null) {
                throw new IOException("Failed to read image from bytes");
            }
            val luminanceSource = new BufferedImageLuminanceSource(bufferedImage);
            val binaryBitmap = new BinaryBitmap(new HybridBinarizer(luminanceSource));
            val reader = new MultiFormatReader();
            var hints = new java.util.HashMap<DecodeHintType, Object>();
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            return reader.decode(binaryBitmap, hints).getText();
        }
    }
}
