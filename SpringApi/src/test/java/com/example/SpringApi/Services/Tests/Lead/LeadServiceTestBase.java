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

        // Mock Authorization header for BaseService authentication behavior
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");

        // Set up RequestContextHolder so
        // BaseService.getClientId()/getUserId()/getUser() work
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("Authorization", "Bearer test-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

        // Mock generic logData to avoid NPEs
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Mock AddressRepository save to return a valid address with ID to avoid NPEs
        // in service
        lenient().when(addressRepository.save(any(Address.class))).thenAnswer(i -> {
            Address a = i.getArgument(0);
            a.setAddressId(DEFAULT_ADDRESS_ID);
            return a;
        });

        // Mock LeadRepository saveAll to return the input list with ids set to simulate successful saves
        lenient().when(leadRepository.saveAll(anyList())).thenAnswer(i -> {
            List<Lead> list = i.getArgument(0);
            long id = DEFAULT_LEAD_ID;
            for (Lead lead : list) {
                lead.setLeadId(id++);
            }
            return list;
        });
    }
}
