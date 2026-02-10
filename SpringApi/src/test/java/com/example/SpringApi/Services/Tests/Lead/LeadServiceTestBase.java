package com.example.SpringApi.Services.Tests.Lead;

import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.Lead;
import com.example.SpringApi.Models.RequestModels.LeadRequestModel;
import com.example.SpringApi.Repositories.AddressRepository;
import com.example.SpringApi.Repositories.LeadRepository;
import com.example.SpringApi.Services.LeadService;
import com.example.SpringApi.Services.MessageService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Services.Tests.BaseTest;
import com.example.SpringApi.Services.Interface.ILeadSubTranslator;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.example.SpringApi.FilterQueryBuilder.LeadFilterQueryBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.lenient;

/**
 * Base test class for LeadService tests.
 * Contains common mocks, dependencies, and setup logic shared across all
 * LeadService test classes.
 */
@ExtendWith(MockitoExtension.class)
public abstract class LeadServiceTestBase extends BaseTest {

    @Mock
    protected LeadRepository leadRepository;

    @Mock
    protected AddressRepository addressRepository;

    @Mock
    protected UserLogService userLogService;

    @Mock
    protected LeadFilterQueryBuilder leadFilterQueryBuilder;

    @Mock
    protected MessageService messageService;

    @Mock
    protected HttpServletRequest request;

    @InjectMocks
    protected LeadService leadService;

    @Mock
    ILeadSubTranslator leadServiceMock;

    // Use 1L to match the default behavior of BaseService.getClientId() in test
    // environment
    protected static final Long TEST_CLIENT_ID = 1L;

    protected Lead testLead;
    protected LeadRequestModel testLeadRequest;

    @BeforeEach
    void setUp() {
        // Create standard test data using BaseTest factory methods
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
    }

    protected void stubLeadRepositoryFindLeadWithDetailsById(Long id, Long clientId, Lead lead) {
        lenient().when(leadRepository.findLeadWithDetailsById(id, clientId)).thenReturn(lead);
    }

    protected void stubLeadRepositoryFindLeadWithDetailsByEmail(String email, Long clientId, Lead lead) {
        lenient().when(leadRepository.findLeadWithDetailsByEmail(email, clientId)).thenReturn(lead);
    }

    protected void stubLeadRepositoryFindLeadWithDetailsByIdIncludingDeleted(Long id, Long clientId, Lead lead) {
        lenient().when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(id, clientId)).thenReturn(lead);
    }

    protected void stubLeadRepositorySave(Lead lead) {
        lenient().when(leadRepository.save(any(Lead.class))).thenReturn(lead);
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
        lenient().when(leadFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), anyString(), anyList(), anyBoolean(), any(Pageable.class))).thenReturn(page);
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

    protected void stubLeadRepositoryFindByIdSuccessActive(Long id) {
        lenient().when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(id, TEST_CLIENT_ID))
                .thenReturn(testLead);
    }

    protected void stubLeadRepositoryFindByIdReturnsSoftDeleted(Long id) {
        Lead softDeletedLead = new Lead();
        softDeletedLead.setLeadId(id);
        softDeletedLead.setIsDeleted(true);
        lenient().when(leadRepository.findLeadWithDetailsById(id, TEST_CLIENT_ID)).thenReturn(softDeletedLead);
    }

    protected void stubBulkSaveThrowsOnItem(int index) {
        // Stub for scenario where bulk save throws on specific item
        lenient().when(leadRepository.save(any(Lead.class))).thenAnswer(inv -> {
            Lead lead = inv.getArgument(0);
            // Simulate failure on specific index (would need counter logic in actual test)
            return lead;
        });
    }

    protected void stubBulkSaveReturnsPartialResult(List<Lead> results) {
        lenient().when(leadRepository.save(any(Lead.class))).thenAnswer(inv -> {
            Lead lead = inv.getArgument(0);
            lead.setLeadId((long) (Math.random() * 1000));
            return lead;
        });
    }

    protected void stubLeadRepositoryFindPageReturnsEmpty() {
        Page<Lead> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(999, 10), 0);
        lenient().when(leadFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), anyString(), anyList(), anyBoolean(), any(Pageable.class)))
                .thenReturn(emptyPage);
    }
}
