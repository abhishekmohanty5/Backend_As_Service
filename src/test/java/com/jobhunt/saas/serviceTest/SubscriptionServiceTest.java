package com.jobhunt.saas.serviceTest;

import com.jobhunt.saas.auth.AuthContext;
import com.jobhunt.saas.dto.SubscriptionResponse;
import com.jobhunt.saas.entity.Plan;
import com.jobhunt.saas.entity.Subscription;
import com.jobhunt.saas.entity.SubscriptionStatus;
import com.jobhunt.saas.repository.PlanRepo;
import com.jobhunt.saas.repository.SubscriptionRepo;
import com.jobhunt.saas.service.SubscriptionService;
import com.jobhunt.saas.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepo subscriptionRepo;

    @Mock
    private PlanRepo planRepo;

    @Mock
    private AuthContext authContext;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private static final Long USER_ID = 1L;
    private static final Long TENANT_ID = 100L;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    public void getExceptionForInactiveSubscription() {
        when(subscriptionRepo.findByUserIdAndTenantIdAndStatus(USER_ID, TENANT_ID, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> subscriptionService.getActiveSubscriptionForUser(USER_ID));
    }

    @Test
    public void cancelSubscriptionTest() {
        when(authContext.getCurrentUserId()).thenReturn(USER_ID);

        when(subscriptionRepo.findByUserIdAndTenantIdAndStatus(USER_ID, TENANT_ID, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> subscriptionService.cancelSubscription()
        );
    }

    @Test
    public void subscribe_shouldThrowException_whenUserAlreadyActive() {
        Long planId = 10L;

        Subscription subscription = new Subscription();
        subscription.setUserId(USER_ID);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        SubscriptionService spyService = spy(subscriptionService);
        doReturn(USER_ID).when(spyService).getCurrentUserId();
        doReturn(false).when(spyService).isExpired(subscription);

        when(subscriptionRepo.findByUserIdAndTenantIdAndStatus(USER_ID, TENANT_ID, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(subscription));

        assertThrows(IllegalStateException.class, () -> spyService.subscribe(planId));
        verify(subscriptionRepo, never()).save(any());
    }

    @Test
    public void subscribe_shouldThrowException_whenPlanNotFound() {
        Long planId = 10L;

        SubscriptionService spyService = spy(subscriptionService);
        doReturn(USER_ID).when(spyService).getCurrentUserId();

        when(subscriptionRepo.findByUserIdAndTenantIdAndStatus(USER_ID, TENANT_ID, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        when(planRepo.findById(planId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> spyService.subscribe(planId));
        verify(subscriptionRepo, never()).save(any());
    }

    @Test
    public void subscribe_shouldThrowException_whenPlanNotActive() {
        Long planId = 10L;

        Plan plan = new Plan();
        plan.setId(planId);
        plan.setActive(false);

        SubscriptionService spyService = spy(subscriptionService);
        doReturn(USER_ID).when(spyService).getCurrentUserId();

        when(subscriptionRepo.findByUserIdAndTenantIdAndStatus(USER_ID, TENANT_ID, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        when(planRepo.findById(planId)).thenReturn(Optional.of(plan));

        assertThrows(IllegalStateException.class, () -> spyService.subscribe(planId));
        verify(subscriptionRepo, never()).save(any());
    }

    @Test
    public void subscribe_shouldCreateSubscription_whenValid() {
        Long planId = 10L;

        Plan plan = new Plan();
        plan.setId(planId);
        plan.setActive(true);
        plan.setDurationInDays(30);
        plan.setName("PRO");

        SubscriptionService spyService = spy(subscriptionService);
        doReturn(USER_ID).when(spyService).getCurrentUserId();

        when(subscriptionRepo.findByUserIdAndTenantIdAndStatus(USER_ID, TENANT_ID, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        when(planRepo.findById(planId)).thenReturn(Optional.of(plan));

        SubscriptionResponse response = spyService.subscribe(planId);
        assertNotNull(response);
        assertEquals(planId, response.getPlanId());
        assertNotNull(response.getStartDate());
        assertNotNull(response.getEndDate());
        assertTrue(response.getEndDate().isAfter(response.getStartDate()));

        verify(subscriptionRepo).save(any(Subscription.class));
    }
}
