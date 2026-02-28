package springapi.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.itextpdf.text.DocumentException;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Pdf Helper Tests")
class PdfHelperTest {

  // Total Tests: 4

  /**
   * Purpose: Verify HTML to PDF conversion produces valid PDF bytes. Expected Result: PDF bytes are
   * returned with a PDF header signature. Assertions: Byte array is non-null, non-empty, and starts
   * with "%PDF".
   */
  @Test
  @DisplayName("Pdf Helper - Convert Html To Pdf Valid Html - Success")
  void pdfHelper_s01_convertHtmlToPdfValidHtml_success()
      throws DocumentException, java.io.IOException {
    // Arrange
    String html = "<html><body><h1>Receipt</h1><p>Payment successful</p></body></html>";

    // Act
    byte[] pdfBytes = PdfHelper.convertHtmlToPdf(html);

    // Assert
    assertNotNull(pdfBytes);
    assertTrue(pdfBytes.length > 4);
    assertEquals("%PDF", new String(pdfBytes, 0, 4, StandardCharsets.ISO_8859_1));
  }

  /**
   * Purpose: Verify purchase-order HTML conversion delegates to shared PDF generation path.
   * Expected Result: PDF bytes are generated for valid purchase-order HTML. Assertions: Byte array
   * is non-null, non-empty, and starts with "%PDF".
   */
  @Test
  @DisplayName("Pdf Helper - Convert Purchase Order Html To Pdf Valid Html - Success")
  void pdfHelper_s02_convertPurchaseOrderHtmlToPdfValidHtml_success()
      throws DocumentException, java.io.IOException {
    // Arrange
    String html = "<html><body><h2>PO-123</h2><table><tr><td>Item</td></tr></table></body></html>";

    // Act
    byte[] pdfBytes = PdfHelper.convertPurchaseOrderHtmlToPdf(html);

    // Assert
    assertNotNull(pdfBytes);
    assertTrue(pdfBytes.length > 4);
    assertEquals("%PDF", new String(pdfBytes, 0, 4, StandardCharsets.ISO_8859_1));
  }

  /**
   * Purpose: Verify PDF bytes can be converted to Base64. Expected Result: Base64 string matches
   * expected encoding. Assertions: Encoded value equals known Base64 output.
   */
  @Test
  @DisplayName("Pdf Helper - To Base64 Known Bytes - Success")
  void pdfHelper_s03_toBase64KnownBytes_success() {
    // Arrange
    byte[] bytes = new byte[] {1, 2, 3};

    // Act
    String encoded = PdfHelper.toBase64(bytes);

    // Assert
    assertEquals("AQID", encoded);
  }

  /**
   * Purpose: Verify private constructor is covered for utility-class contract. Expected Result:
   * Constructor can be invoked reflectively. Assertions: Reflected instance is created
   * successfully.
   */
  @Test
  @DisplayName("Pdf Helper - Private Constructor Reflection - Success")
  void pdfHelper_s04_privateConstructorReflection_success() throws Exception {
    // Arrange
    Constructor<PdfHelper> constructor = PdfHelper.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    // Act
    PdfHelper instance = constructor.newInstance();

    // Assert
    assertNotNull(instance);
  }
}
