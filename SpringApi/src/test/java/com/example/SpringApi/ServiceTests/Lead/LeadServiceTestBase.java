package com.example.SpringApi.ServiceTests.Lead;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.FilterQueryBuilder.LeadFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.Lead;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.RequestModels.LeadRequestModel;
import com.example.SpringApi.Repositories.AddressRepository;
import com.example.SpringApi.Repositories.LeadRepository;
import com.example.SpringApi.Services.LeadService;
import com.example.SpringApi.Services.MessageService;
import com.example.SpringApi.Services.UserLogService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Base test class for LeadService tests. Contains common mocks, dependencies, and setup logic
 * shared across all LeadService test classes.
 */
@ExtendWith(MockitoExtension.class)
abstract class LeadServiceTestBase {

  // ==================== COMMON TEST CONSTANTS ====================

  protected static final Long DEFAULT_ADDRESS_ID = 1L;
  protected static final Long DEFAULT_USER_ID = 1L;
  protected static final Long DEFAULT_CLIENT_ID = 100L;
  protected static final String DEFAULT_ADDRESS_TYPE = "HOME";
  protected static final String DEFAULT_STREET_ADDRESS = "123 Main St";
  protected static final String DEFAULT_CITY = "New York";
  protected static final String DEFAULT_STATE = "NY";
  protected static final String DEFAULT_POSTAL_CODE = "10001";
  protected static final String DEFAULT_COUNTRY = "USA";
  protected static final String DEFAULT_CREATED_USER = "admin";
  protected static final String DEFAULT_LOGIN_NAME = "testuser";
  protected static final String DEFAULT_EMAIL = "test@example.com";
  protected static final String DEFAULT_FIRST_NAME = "Test";
  protected static final String DEFAULT_LAST_NAME = "User";
  protected static final Long DEFAULT_LEAD_ID = 1L;
  protected static final String DEFAULT_PHONE = "1234567890";
  protected static final String DEFAULT_LEAD_STATUS = "Not Contacted";
  protected static final String DEFAULT_COMPANY = "Test Company";
  protected static final int DEFAULT_COMPANY_SIZE = 50;
  protected static final Long DEFAULT_CREATED_BY_ID = 1L;
  protected static final Long DEFAULT_ASSIGNED_AGENT_ID = 2L;

  protected static final String[] LEAD_STRING_COLUMNS = {
    "firstName", "lastName", "email", "phone", "company", "leadStatus"
  };
  protected static final String[] LEAD_NUMBER_COLUMNS = {"leadId", "companySize"};
  protected static final String[] LEAD_BOOLEAN_COLUMNS = {"isDeleted"};
  protected static final String[] LEAD_DATE_COLUMNS = {"createdAt", "updatedAt"};

  @Mock protected LeadRepository leadRepository;

  @Mock protected AddressRepository addressRepository;

  @Mock protected UserLogService userLogService;

  @Mock protected LeadFilterQueryBuilder leadFilterQueryBuilder;

  @Mock protected MessageService messageService;

  @Mock protected HttpServletRequest request;

  @Mock protected ApplicationContext applicationContext;

  @InjectMocks protected LeadService leadService;

  @Mock LeadService leadServiceMock;

  // Use 1L to match the default behavior of BaseService.getClientId() in test
  // environment
  protected static final Long TEST_CLIENT_ID = 1L;

  protected Lead testLead;
  protected LeadRequestModel testLeadRequest;
  private final AtomicLong leadIdSequence = new AtomicLong(1L);

  @BeforeEach
  void setUp() {
    // Create standard test data using shared factory methods
    testLeadRequest = createValidLeadRequest(DEFAULT_LEAD_ID, TEST_CLIENT_ID);
    testLeadRequest.setStart(0);
    testLeadRequest.setEnd(10);

    testLead = createTestLead(testLeadRequest, DEFAULT_CREATED_USER);
    testLead.setClientId(TEST_CLIENT_ID); // Ensure entity has matching clientId

    // Set up RequestContextHolder so
    // BaseService.getClientId()/getUserId()/getUser() work
    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    mockRequest.addHeader("Authorization", "Bearer test-token");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

    // Default stubs that are commonly used (can be overridden in specific tests)
    stubUserLogServiceLogData(true);
    stubAddressRepositorySave(new Address(testLeadRequest.getAddress(), DEFAULT_CREATED_USER));

    // Configure mock to return default values for controller tests
    stubLeadServiceMockDefaults();

    // LeadService.createLead uses applicationContext.getBean(LeadService.class)
    lenient().when(applicationContext.getBean(LeadService.class)).thenReturn(leadService);
  }

  protected void stubLeadServiceMockDefaults() {
    lenient().when(leadServiceMock.getUserId()).thenReturn(DEFAULT_USER_ID);
    lenient().when(leadServiceMock.getUser()).thenReturn(DEFAULT_LOGIN_NAME);
    lenient().when(leadServiceMock.getClientId()).thenReturn(TEST_CLIENT_ID);
  }

  protected void stubLeadRepositoryFindLeadWithDetailsById(Long id, Long clientId, Lead lead) {
    lenient().when(leadRepository.findLeadWithDetailsById(id, clientId)).thenReturn(lead);
  }

  protected void stubLeadRepositoryFindLeadWithDetailsByEmail(
      String email, Long clientId, Lead lead) {
    lenient().when(leadRepository.findLeadWithDetailsByEmail(email, clientId)).thenReturn(lead);
  }

  protected void stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(
      Long id, Long clientId, Lead lead) {
    lenient()
        .when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(id, clientId))
        .thenReturn(lead);
  }

  protected void stubLeadRepositorySave(Lead lead) {
    lenient().when(leadRepository.save(any(Lead.class))).thenReturn(lead);
  }

  protected void stubLeadRepositorySaveAssignsId() {
    lenient()
        .when(leadRepository.save(any(Lead.class)))
        .thenAnswer(
            inv -> {
              Lead lead = inv.getArgument(0);
              if (lead.getLeadId() == null || lead.getLeadId() == 0) {
                lead.setLeadId(leadIdSequence.getAndIncrement());
              }
              return lead;
            });
  }

  protected void stubAddressRepositorySave(Address address) {
    Address savedAddress = new Address();
    if (address != null) {
      savedAddress.setAddressId(DEFAULT_ADDRESS_ID);
    }
    lenient().when(addressRepository.save(any(Address.class))).thenReturn(savedAddress);
  }

  protected void stubUserLogServiceLogData(boolean result) {
    lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(result);
  }

  protected void stubLeadFilterQueryBuilderFindPaginatedEntities(Page<Lead> page) {
    lenient()
        .when(
            leadFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(),
                anyString(),
                org.mockito.ArgumentMatchers
                    .<List<
                            com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel
                                .FilterCondition>>
                        any(),
                anyBoolean(),
                any(Pageable.class)))
        .thenReturn(page);
  }

  protected void stubLeadFilterQueryBuilderGetColumnType(String[] columns, String type) {
    lenient()
        .when(
            leadFilterQueryBuilder.getColumnType(
                org.mockito.ArgumentMatchers.argThat(
                    arg -> java.util.Arrays.asList(columns).contains(arg))))
        .thenReturn(type);
  }

  protected void stubMessageServiceCreateDetailedBulkInsertResultMessage() {
    // No-op for void methods unless we need to verify them later
  }

  // New stubs for additional test coverage

  protected void stubValidateLeadMissingEmail() {
    // Stub for validation that ensures email is present
    // This would be called by the service during validation
  }

  protected void stubLeadRepositoryFindByIdSuccess(Long id, Lead lead) {
    lenient().when(leadRepository.findLeadWithDetailsById(id, TEST_CLIENT_ID)).thenReturn(lead);
  }

  protected void stubLeadRepositoryFindByIdNotFound(Long id) {
    lenient().when(leadRepository.findLeadWithDetailsById(id, TEST_CLIENT_ID)).thenReturn(null);
  }

  protected void stubLeadRepositoryFindByIdIncludingDeletedAny(Lead lead) {
    lenient()
        .when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(anyLong(), anyLong()))
        .thenReturn(lead);
  }

  protected void stubLeadRepositoryFindByIdSuccessActive(Long id) {
    lenient()
        .when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(id, TEST_CLIENT_ID))
        .thenReturn(testLead);
  }

  protected void stubLeadRepositoryFindByIdReturnsSoftDeleted(Long id) {
    Lead softDeletedLead = new Lead();
    softDeletedLead.setLeadId(id);
    softDeletedLead.setIsDeleted(true);
    lenient()
        .when(leadRepository.findLeadWithDetailsById(id, TEST_CLIENT_ID))
        .thenReturn(softDeletedLead);
  }

  protected void stubBulkSaveThrowsOnItem(int index) {
    // Stub for scenario where bulk save throws on specific item
    AtomicInteger callCount = new AtomicInteger(0);
    lenient()
        .when(leadRepository.save(any(Lead.class)))
        .thenAnswer(
            inv -> {
              if (callCount.getAndIncrement() == index) {
                throw new RuntimeException("Simulated bulk save failure at index " + index);
              }
              return inv.getArgument(0);
            });
  }

  protected void stubBulkSaveReturnsPartialResult(List<Lead> results) {
    AtomicInteger callCount = new AtomicInteger(0);
    lenient()
        .when(leadRepository.save(any(Lead.class)))
        .thenAnswer(
            inv -> {
              int currentIndex = callCount.getAndIncrement();
              if (results != null
                  && currentIndex < results.size()
                  && results.get(currentIndex) != null) {
                return results.get(currentIndex);
              }
              Lead lead = inv.getArgument(0);
              lead.setLeadId(ThreadLocalRandom.current().nextLong(1, 1000));
              return lead;
            });
  }

  protected void stubLeadRepositoryFindLeadWithDetailsByEmailReturnsLead() {
    lenient()
        .when(leadRepository.findLeadWithDetailsByEmail(anyString(), anyLong()))
        .thenAnswer(
            inv -> {
              Lead lead = new Lead();
              lead.setLeadId(leadIdSequence.getAndIncrement());
              return lead;
            });
  }

  protected void stubLeadRepositoryFindPageReturnsEmpty() {
    Page<Lead> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(999, 10), 0);
    lenient()
        .when(
            leadFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), anyString(), anyList(), anyBoolean(), any(Pageable.class)))
        .thenReturn(emptyPage);
  }

  /** Stub controller service call for getLeadDetailsByEmail. */
  protected void stubLeadServiceGetLeadDetailsByEmail(
      String email, com.example.SpringApi.Models.ResponseModels.LeadResponseModel response) {
    lenient().when(leadServiceMock.getLeadDetailsByEmail(email)).thenReturn(response);
  }

  /** Stub controller service call for getLeadDetailsById. */
  protected void stubLeadServiceGetLeadDetailsById(
      Long id, com.example.SpringApi.Models.ResponseModels.LeadResponseModel response) {
    lenient().when(leadServiceMock.getLeadDetailsById(id)).thenReturn(response);
  }

  /** Stub controller service call for getLeadsInBatches. */
  protected void stubLeadServiceGetLeadsInBatches(
      com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel<
              com.example.SpringApi.Models.ResponseModels.LeadResponseModel>
          response) {
    lenient()
        .when(leadServiceMock.getLeadsInBatches(any(LeadRequestModel.class)))
        .thenReturn(response);
  }

  // ==================== UNAUTHORIZED STUBS ====================

  protected void stubLeadServiceGetLeadsInBatchesThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                ErrorMessages.ERROR_UNAUTHORIZED))
        .when(leadServiceMock)
        .getLeadsInBatches(any(LeadRequestModel.class));
  }

  protected void stubLeadServiceCreateLeadThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                ErrorMessages.ERROR_UNAUTHORIZED))
        .when(leadServiceMock)
        .createLead(any(LeadRequestModel.class));
  }

  protected void stubLeadServiceUpdateLeadThrowsUnauthorized(Long leadId) {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                ErrorMessages.ERROR_UNAUTHORIZED))
        .when(leadServiceMock)
        .updateLead(eq(leadId), any());
  }

  protected void stubLeadServiceUpdateLeadThrowsForbidden(Long leadId) {
    lenient()
        .doThrow(new com.example.SpringApi.Exceptions.PermissionException("Forbidden"))
        .when(leadServiceMock)
        .updateLead(eq(leadId), any());
  }

  protected void stubLeadServiceToggleLeadThrowsUnauthorized(Long leadId) {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                ErrorMessages.ERROR_UNAUTHORIZED))
        .when(leadServiceMock)
        .toggleLead(leadId);
  }

  protected void stubLeadServiceToggleLeadThrowsForbidden(Long leadId) {
    lenient()
        .doThrow(new com.example.SpringApi.Exceptions.PermissionException("Forbidden"))
        .when(leadServiceMock)
        .toggleLead(leadId);
  }

  protected void stubLeadServiceBulkCreateLeadsAsyncThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                ErrorMessages.ERROR_UNAUTHORIZED))
        .when(leadServiceMock)
        .bulkCreateLeadsAsync(anyList(), anyLong(), anyString(), anyLong());
  }

  // ==================== FACTORY METHODS ====================

  protected LeadRequestModel createValidLeadRequest(Long leadId, Long clientId) {
    LeadRequestModel leadRequest = new LeadRequestModel();
    leadRequest.setLeadId(leadId);
    leadRequest.setClientId(clientId);
    leadRequest.setFirstName(DEFAULT_FIRST_NAME);
    leadRequest.setLastName(DEFAULT_LAST_NAME);
    leadRequest.setEmail(DEFAULT_EMAIL);
    leadRequest.setPhone(DEFAULT_PHONE);
    leadRequest.setCompany(DEFAULT_COMPANY);
    leadRequest.setCompanySize(DEFAULT_COMPANY_SIZE);
    leadRequest.setLeadStatus(DEFAULT_LEAD_STATUS);
    leadRequest.setCreatedById(DEFAULT_CREATED_BY_ID);
    leadRequest.setAssignedAgentId(DEFAULT_ASSIGNED_AGENT_ID);
    leadRequest.setAddress(createValidAddressRequest());
    leadRequest.setIsDeleted(false);
    return leadRequest;
  }

  protected AddressRequestModel createValidAddressRequest() {
    return createValidAddressRequest(DEFAULT_ADDRESS_ID, DEFAULT_USER_ID, DEFAULT_CLIENT_ID);
  }

  protected AddressRequestModel createValidAddressRequest(
      Long addressId, Long userId, Long clientId) {
    AddressRequestModel addressRequest = new AddressRequestModel();
    addressRequest.setId(addressId);
    addressRequest.setUserId(userId);
    addressRequest.setClientId(clientId);
    addressRequest.setAddressType(DEFAULT_ADDRESS_TYPE);
    addressRequest.setStreetAddress(DEFAULT_STREET_ADDRESS);
    addressRequest.setCity(DEFAULT_CITY);
    addressRequest.setState(DEFAULT_STATE);
    addressRequest.setPostalCode(DEFAULT_POSTAL_CODE);
    addressRequest.setCountry(DEFAULT_COUNTRY);
    addressRequest.setIsPrimary(true);
    addressRequest.setIsDeleted(false);
    return addressRequest;
  }

  protected Lead createTestLead(LeadRequestModel request, String createdUser) {
    Lead lead = new Lead(request, createdUser);
    lead.setLeadId(request.getLeadId());
    lead.setAddressId(DEFAULT_ADDRESS_ID);

    User user = createTestUser(request.getCreatedById());
    Address address = createTestAddress(request.getAddress(), createdUser);

    lead.setCreatedByUser(user);
    lead.setAssignedAgent(user);
    lead.setAddress(address);

    lead.setIsDeleted(false);
    lead.setCreatedAt(LocalDateTime.now());
    lead.setUpdatedAt(LocalDateTime.now());
    return lead;
  }

  protected User createTestUser(Long userId) {
    User user = new User();
    user.setUserId(userId);
    user.setLoginName(DEFAULT_LOGIN_NAME);
    user.setFirstName(DEFAULT_FIRST_NAME);
    user.setLastName(DEFAULT_LAST_NAME);
    user.setEmail(DEFAULT_EMAIL);
    user.setIsDeleted(false);
    user.setCreatedUser(DEFAULT_CREATED_USER);
    user.setModifiedUser(DEFAULT_CREATED_USER);
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    return user;
  }

  protected Address createTestAddress(AddressRequestModel request, String createdUser) {
    Address address = new Address(request, createdUser);
    address.setAddressId(request.getId());
    address.setIsDeleted(false);
    address.setCreatedAt(LocalDateTime.now());
    address.setUpdatedAt(LocalDateTime.now());
    return address;
  }

  /** Stub controller service updateLead call. */
  protected void stubLeadServiceUpdateLeadDoNothing(Long leadId) {
    lenient().doNothing().when(leadServiceMock).updateLead(eq(leadId), any(LeadRequestModel.class));
  }

  /** Stub controller service createLead call. */
  protected void stubLeadServiceCreateLeadDoNothing() {
    lenient().doNothing().when(leadServiceMock).createLead(any(LeadRequestModel.class));
  }

  /** Stub controller service toggleLead call. */
  protected void stubLeadServiceToggleLeadDoNothing(Long leadId) {
    lenient().doNothing().when(leadServiceMock).toggleLead(leadId);
  }
}

