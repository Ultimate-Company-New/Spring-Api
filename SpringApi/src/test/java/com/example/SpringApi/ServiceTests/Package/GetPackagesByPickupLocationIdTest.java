package com.example.SpringApi.ServiceTests.Package;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import com.example.SpringApi.Controllers.PackageController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.ResponseModels.PackageResponseModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Unit tests for PackageService.getPackagesByPickupLocationId() method. */
@DisplayName("Get Packages By Pickup Location ID Tests")
class GetPackagesByPickupLocationIdTest extends PackageServiceTestBase {

  // Total Tests: 31
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify retrieval handles cases where all result packages have identical properties.
   * Expected Result: List contains packages with duplicate property values.
   * Assertions: Result size is greater than 1 and properties match.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - All Identical Packages - Success")
  void getPackagesByPickupLocationId_s01_AllIdenticalPackages_Success() {
    // Arrange
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, 1L);

    List<com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping>
        identicalMappings = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      identicalMappings.add(testMapping);
    }
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, identicalMappings);

    // Act
    List<PackageResponseModel> result =
        packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

    // Assert
    assertEquals(5, result.size());
    for (PackageResponseModel pkg : result) {
      assertEquals(TEST_PACKAGE_ID, pkg.getPackageId());
    }
  }

  /*
   * Purpose: Verify empty result when no packages are mapped to a valid pickup location.
   * Expected Result: Empty list is returned.
   * Assertions: result.isEmpty() is true.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Empty Result - Success")
  void getPackagesByPickupLocationId_s02_EmptyResult_Success() {
    // Arrange
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, 1L);
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, Arrays.asList());

    // Act
    List<PackageResponseModel> result =
        packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  /*
   * Purpose: Verify retrieval succeeds with extremely large number of mapped packages (1000+).
   * Expected Result: All 1000+ packages are returned successfully.
   * Assertions: Result size equals 1000.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Extreme Package Count - Success")
  void getPackagesByPickupLocationId_s03_ExtremePackageCount_Success() {
    // Arrange
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, 1L);

    List<com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping> manyMappings =
        new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      manyMappings.add(testMapping);
    }
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, manyMappings);

    // Act
    List<PackageResponseModel> result =
        packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

    // Assert
    assertNotNull(result);
    assertEquals(1000, result.size());
  }

  /*
   * Purpose: Reject retrieval when using very large location ID that doesn't exist.
   * Expected Result: NotFoundException is thrown.
   * Assertions: Exception indicates location not found.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Very Large ID Not Found - Success")
  void getPackagesByPickupLocationId_s04_LargeId_NotFound() {
    // Arrange
    long locationId = Long.MAX_VALUE - 1;
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(locationId, TEST_CLIENT_ID, 0L);

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> packageService.getPackagesByPickupLocationId(locationId));

    // Assert
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, locationId),
        ex.getMessage());
  }

  /*
   * Purpose: Verify retrieval succeeds when 10+ packages are mapped to a location.
   * Expected Result: All packages are returned in the result list.
   * Assertions: result.size() equals the number of mappings.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Many Packages Result - Success")
  void getPackagesByPickupLocationId_s05_ManyPackagesResult_Success() {
    // Arrange
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, 1L);

    List<com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping> manyMappings =
        new ArrayList<>();
    for (int i = 0; i < 15; i++) {
      com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping mapping =
          createTestPackagePickupLocationMapping();
      mapping.setPackageId((long) i);
      manyMappings.add(mapping);
    }
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, manyMappings);

    // Act
    List<PackageResponseModel> result =
        packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

    // Assert
    assertEquals(15, result.size());
  }

  /*
   * Purpose: Verify multiple packages are returned for a pickup location.
   * Expected Result: List contains multiple package entries.
   * Assertions: result.size() matches the mapping count.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Multiple Packages - Success")
  void getPackagesByPickupLocationId_s06_MultiplePackages_Success() {
    // Arrange
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, 1L);
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, Arrays.asList(testMapping, testMapping));

    // Act
    List<PackageResponseModel> result =
        packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

    // Assert
    assertEquals(2, result.size());
  }

  /*
   * Purpose: Verify retrieval succeeds with exactly one package in result.
   * Expected Result: Single package is returned correctly.
   * Assertions: result.size() equals 1 and package ID matches.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Single Package Result - Success")
  void getPackagesByPickupLocationId_s07_OnePackageResult_Success() {
    // Arrange
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, 1L);
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, Arrays.asList(testMapping));

    // Act
    List<PackageResponseModel> result =
        packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

    // Assert
    assertEquals(1, result.size());
    assertEquals(TEST_PACKAGE_ID, result.get(0).getPackageId());
  }

  /*
   * Purpose: Verify successful retrieval of packages for a valid pickup location.
   * Expected Result: List of packages is returned.
   * Assertions: ID matches the mapped package entity.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Success")
  void getPackagesByPickupLocationId_s08_Success_Success() {
    // Arrange
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, 1L);
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, Arrays.asList(testMapping));

    // Act
    List<PackageResponseModel> result =
        packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

    // Assert
    assertNotNull(result);
    assertEquals(TEST_PACKAGE_ID, result.get(0).getPackageId());
  }

  /*
   * Purpose: Verify retrieval succeeds for very high location ID value.
   * Expected Result: Packages are returned for the high ID location.
   * Assertions: Result is not empty and contains package data.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Very High Location ID - Success")
  void getPackagesByPickupLocationId_s09_VeryHighLocationId_Success() {
    // Arrange
    long highLocationId = 9999999999L;
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(
        highLocationId, TEST_CLIENT_ID, 1L);
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdAndClientId(
        highLocationId, TEST_CLIENT_ID, Arrays.asList(testMapping));

    // Act
    List<PackageResponseModel> result =
        packageService.getPackagesByPickupLocationId(highLocationId);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Reject retrieval attempts for Long.MAX_VALUE if location not found.
   * Expected Result: NotFoundException is thrown.
   * Assertions: Error message indicates not found.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Max Long ID - Throws NotFoundException")
  void getPackagesByPickupLocationId_f01_MaxLongId_ThrowsNotFoundException() {
    // Arrange
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(
        Long.MAX_VALUE, TEST_CLIENT_ID, 0L);

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> packageService.getPackagesByPickupLocationId(Long.MAX_VALUE));

    // Assert
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, Long.MAX_VALUE),
        ex.getMessage());
  }

  /*
   * Purpose: Reject retrieval attempts for negative IDs.
   * Expected Result: NotFoundException is thrown.
   * Assertions: Error message indicates not found.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Negative ID - Throws NotFoundException")
  void getPackagesByPickupLocationId_f02_NegativeId_ThrowsNotFoundException() {
    // Arrange
    long invalidId = -1L;
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(invalidId, TEST_CLIENT_ID, 0L);

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> packageService.getPackagesByPickupLocationId(invalidId));

    // Assert
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, invalidId),
        ex.getMessage());
  }

  /*
   * Purpose: Reject retrieval attempts when the pickup location is not found.
   * Expected Result: NotFoundException is thrown.
   * Assertions: Exception message indicates not found.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Not Found - Throws NotFoundException")
  void getPackagesByPickupLocationId_f03_NotFound_ThrowsNotFoundException() {
    // Arrange
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, 0L);

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID));

    // Assert
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, TEST_PICKUP_LOCATION_ID),
        ex.getMessage());
  }

  /*
   * Purpose: Reject retrieval attempts for a zero ID.
   * Expected Result: NotFoundException is thrown.
   * Assertions: Error message indicates not found.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Zero ID - Throws NotFoundException")
  void getPackagesByPickupLocationId_f04_ZeroId_ThrowsNotFoundException() {
    // Arrange
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(0L, TEST_CLIENT_ID, 0L);

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> packageService.getPackagesByPickupLocationId(0L));

    // Assert
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, 0L), ex.getMessage());
  }

  /*
   **********************************************************************************************
   * CONTROLLER AUTHORIZATION TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify unauthorized access is blocked at the controller level.
   * Expected Result: Unauthorized status is returned.
   * Assertions: Response status is 401 UNAUTHORIZED.
   */
  @Test
  @DisplayName("getPackagesByPickupLocationId - Controller Permission - Unauthorized")
  void getPackagesByPickupLocationId_p01_controller_permission_unauthorized() {
    // Arrange
    PackageController controller = new PackageController(packageServiceMock, null);
    stubPackageServiceThrowsUnauthorizedException();

    // Act
    ResponseEntity<?> response = controller.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  /*
   * Purpose: Verify controller delegates to service for valid requests.
   * Expected Result: Service method is invoked and HTTP 200 returned.
   * Assertions: Service called once and status code is OK.
   */
  @Test
  @DisplayName("getPackagesByPickupLocationId - Controller delegates to service")
  void getPackagesByPickupLocationId_p03_WithValidRequest_DelegatesToService() {
    // Arrange
    PackageController controller = new PackageController(packageServiceMock, null);
    stubPackageServiceGetPackagesByPickupLocationIdReturns(
        Arrays.asList(new PackageResponseModel(testPackage)));

    // Act
    ResponseEntity<?> response = controller.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

    // Assert
    verify(packageServiceMock).getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}

/** Unit tests for PackageService.getPackagesByPickupLocationId() method. */
@DisplayName("Get Packages By Pickup Location ID Tests - Duplicate Block")
class GetPackagesByPickupLocationIdDuplicateTests extends PackageServiceTestBase {
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify retrieval handles cases where all result packages have identical properties.
   * Expected Result: List contains packages with duplicate property values. Assertions: Result size
   * is greater than 1 and properties match.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - All Identical Packages - Success")
  void getPackagesByPickupLocationId_s10_AllIdenticalPackages_Success() {
    // Arrange
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, 1L);
    List<com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping>
        identicalMappings = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      identicalMappings.add(testMapping);
    }
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, identicalMappings);

    // Act
    List<PackageResponseModel> result =
        packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

    // Assert
    assertEquals(5, result.size());
    for (PackageResponseModel pkg : result) {
      assertEquals(TEST_PACKAGE_ID, pkg.getPackageId());
    }
  }

  /**
   * Purpose: Verify empty result when no packages are mapped to a valid pickup location. Expected
   * Result: Empty list is returned. Assertions: result.isEmpty() is true.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Empty Result - Success")
  void getPackagesByPickupLocationId_s11_EmptyResult_Success() {
    // Arrange
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, 1L);
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, Arrays.asList());

    // Act
    List<PackageResponseModel> result =
        packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  /**
   * Purpose: Verify retrieval succeeds with extremely large number of mapped packages (1000+).
   * Expected Result: All 1000+ packages are returned successfully. Assertions: Result size equals
   * 1000.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Extreme Package Count - Success")
  void getPackagesByPickupLocationId_s12_ExtremePackageCount_Success() {
    // Arrange
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, 1L);
    List<com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping> manyMappings =
        new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      manyMappings.add(testMapping);
    }
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, manyMappings);

    // Act
    List<PackageResponseModel> result =
        packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

    // Assert
    assertNotNull(result);
    assertEquals(1000, result.size());
  }

  /**
   * Purpose: Reject retrieval when using very large location ID that doesn't exist. Expected
   * Result: NotFoundException is thrown. Assertions: Exception indicates location not found.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Very Large ID Not Found - Success")
  void getPackagesByPickupLocationId_s13_LargeId_NotFound() {
    // Arrange
    long locationId = Long.MAX_VALUE - 1;
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(locationId, TEST_CLIENT_ID, 0L);

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> packageService.getPackagesByPickupLocationId(locationId));

    // Assert
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, locationId),
        ex.getMessage());
  }

  /**
   * Purpose: Verify retrieval succeeds when 10+ packages are mapped to a location. Expected Result:
   * All packages are returned in the result list. Assertions: result.size() equals the number of
   * mappings.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Many Packages Result - Success")
  void getPackagesByPickupLocationId_s14_ManyPackagesResult_Success() {
    // Arrange
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, 1L);
    List<com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping> manyMappings =
        new ArrayList<>();
    for (int i = 0; i < 15; i++) {
      com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping mapping =
          createTestPackagePickupLocationMapping();
      mapping.setPackageId((long) i);
      manyMappings.add(mapping);
    }
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, manyMappings);

    // Act
    List<PackageResponseModel> result =
        packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

    // Assert
    assertEquals(15, result.size());
  }

  /**
   * Purpose: Verify multiple packages are returned for a pickup location. Expected Result: List
   * contains multiple package entries. Assertions: result.size() matches the mapping count.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Multiple Packages - Success")
  void getPackagesByPickupLocationId_s15_MultiplePackages_Success() {
    // Arrange
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, 1L);
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, Arrays.asList(testMapping, testMapping));

    // Act
    List<PackageResponseModel> result =
        packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

    // Assert
    assertEquals(2, result.size());
  }

  /**
   * Purpose: Verify retrieval succeeds with exactly one package in result. Expected Result: Single
   * package is returned correctly. Assertions: result.size() equals 1 and package ID matches.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Single Package Result - Success")
  void getPackagesByPickupLocationId_s16_OnePackageResult_Success() {
    // Arrange
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, 1L);
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, Arrays.asList(testMapping));

    // Act
    List<PackageResponseModel> result =
        packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

    // Assert
    assertEquals(1, result.size());
    assertEquals(TEST_PACKAGE_ID, result.get(0).getPackageId());
  }

  /**
   * Purpose: Verify successful retrieval of packages for a valid pickup location. Expected Result:
   * List of packages is returned. Assertions: ID matches the mapped package entity.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Success")
  void getPackagesByPickupLocationId_s17_Success_Success() {
    // Arrange
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, 1L);
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, Arrays.asList(testMapping));

    // Act
    List<PackageResponseModel> result =
        packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

    // Assert
    assertNotNull(result);
    assertEquals(TEST_PACKAGE_ID, result.get(0).getPackageId());
  }

  /**
   * Purpose: Verify retrieval succeeds for very high location ID value. Expected Result: Packages
   * are returned for the high ID location. Assertions: Result is not empty and contains package
   * data.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Very High Location ID - Success")
  void getPackagesByPickupLocationId_s18_VeryHighLocationId_Success() {
    // Arrange
    long highLocationId = 9999999999L;
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(
        highLocationId, TEST_CLIENT_ID, 1L);
    stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdAndClientId(
        highLocationId, TEST_CLIENT_ID, Arrays.asList(testMapping));

    // Act
    List<PackageResponseModel> result =
        packageService.getPackagesByPickupLocationId(highLocationId);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Reject retrieval attempts for Long.MAX_VALUE if location not found. Expected Result:
   * NotFoundException is thrown. Assertions: Error message indicates not found.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Max Long ID - Throws NotFoundException")
  void getPackagesByPickupLocationId_f05_MaxLongId_ThrowsNotFoundException() {
    // Arrange
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(
        Long.MAX_VALUE, TEST_CLIENT_ID, 0L);

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> packageService.getPackagesByPickupLocationId(Long.MAX_VALUE));

    // Assert
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, Long.MAX_VALUE),
        ex.getMessage());
  }

  /**
   * Purpose: Reject retrieval attempts for negative IDs. Expected Result: NotFoundException is
   * thrown. Assertions: Error message indicates not found.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Negative ID - Throws NotFoundException")
  void getPackagesByPickupLocationId_f06_NegativeId_ThrowsNotFoundException() {
    // Arrange
    long invalidId = -1L;
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(invalidId, TEST_CLIENT_ID, 0L);

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> packageService.getPackagesByPickupLocationId(invalidId));

    // Assert
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, invalidId),
        ex.getMessage());
  }

  /**
   * Purpose: Reject retrieval attempts when the pickup location is not found. Expected Result:
   * NotFoundException is thrown. Assertions: Exception is thrown.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Not Found - Throws NotFoundException")
  void getPackagesByPickupLocationId_f07_NotFound_ThrowsNotFoundException() {
    // Arrange
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, 0L);

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID));

    // Assert
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, TEST_PICKUP_LOCATION_ID),
        ex.getMessage());
  }

  /**
   * Purpose: Reject retrieval attempts for a zero ID. Expected Result: NotFoundException is thrown.
   * Assertions: Error message indicates not found.
   */
  @Test
  @DisplayName("Get Packages By Pickup Location ID - Zero ID - Throws NotFoundException")
  void getPackagesByPickupLocationId_f08_ZeroId_ThrowsNotFoundException() {
    // Arrange
    stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(0L, TEST_CLIENT_ID, 0L);

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> packageService.getPackagesByPickupLocationId(0L));

    // Assert
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, 0L), ex.getMessage());
  }

  /**
   * Purpose: Verify controller delegates to service for valid requests. Expected Result: Service
   * method is invoked and HTTP 200 returned. Assertions: Service called once and status code is OK.
   */
  @Test
  @DisplayName("getPackagesByPickupLocationId - Controller delegates to service")
  void getPackagesByPickupLocationId_p05_WithValidRequest_DelegatesToService() {
    // Arrange
    PackageController controller = new PackageController(packageServiceMock, null);
    stubPackageServiceGetPackagesByPickupLocationIdReturns(
        Arrays.asList(new PackageResponseModel(testPackage)));

    // Act
    ResponseEntity<?> response = controller.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

    // Assert
    verify(packageServiceMock).getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}

