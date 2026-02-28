package springapi.helpers;

import com.itextpdf.text.DocumentException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import org.xhtmlrenderer.pdf.ITextRenderer;

/**
 * Helper class for PDF generation and manipulation.
 *
 * <p>This class provides utility methods for converting HTML content to PDF using Flying Saucer
 * (xhtmlrenderer) with iText.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public class PdfHelper {
  private PdfHelper() {}

  /**
   * Converts HTML content to PDF bytes.
   *
   * <p>This method uses Flying Saucer (xhtmlrenderer) library to convert HTML to PDF format.
   *
   * @param htmlContent The HTML content to convert
   * @return PDF as byte array
   * @throws IOException if PDF generation fails
   * @throws DocumentException if PDF document creation fails
   */
  public static byte[] convertPurchaseOrderHtmlToPdf(String htmlContent)
      throws IOException, DocumentException {
    return generatePdf(htmlContent);
  }

  /**
   * Converts HTML content to PDF bytes (simplified version).
   *
   * @param htmlContent The HTML content to convert
   * @return PDF as byte array
   * @throws DocumentException if PDF document creation fails
   * @throws IOException if PDF generation fails
   */
  public static byte[] convertHtmlToPdf(String htmlContent) throws DocumentException, IOException {
    return generatePdf(htmlContent);
  }

  private static byte[] generatePdf(String htmlContent) throws IOException, DocumentException {
    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      ITextRenderer renderer = new ITextRenderer();
      renderer.setDocumentFromString(htmlContent);
      renderer.layout();
      renderer.createPDF(os);
      return os.toByteArray();
    }
  }

  /**
   * Converts PDF bytes to Base64 encoded string.
   *
   * @param pdfBytes PDF content as byte array
   * @return Base64 encoded string
   */
  public static String toBase64(byte[] pdfBytes) {
    return Base64.getEncoder().encodeToString(pdfBytes);
  }
}
