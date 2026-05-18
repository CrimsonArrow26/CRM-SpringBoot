package com.crm.service;

import com.crm.entity.*;
import com.crm.repository.*;
import com.crm.dto.DealDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

    @Mock
    private DealRepository dealRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private DealService dealService;

    @Captor
    private ArgumentCaptor<Deal> dealCaptor;

    private Organization organization;
    private Organization otherOrg;
    private Member member;
    private Member userMember;
    private Account account;
    private Contact contact;
    private Deal deal;

    @BeforeEach
    void setUp() {
        organization = new Organization("Test Org", "org@test.com");
        organization.setOrgId(1L);

        otherOrg = new Organization("Other Org", "other@test.com");
        otherOrg.setOrgId(2L);

        Role adminRole = new Role();
        adminRole.setRoleId(1L);
        adminRole.setRoleName("Admin");

        Role userRole = new Role();
        userRole.setRoleId(2L);
        userRole.setRoleName("User");

        member = new Member("John Doe", "john@test.com", "password", organization, adminRole);
        member.setMemberId(1L);

        userMember = new Member("Jane User", "jane@test.com", "password", organization, userRole);
        userMember.setMemberId(2L);

        account = new Account();
        account.setAccountId(1L);
        account.setAccountName("Test Account");
        account.setEmail("account@test.com");
        account.setOrganization(organization);
        account.setMember(member);

        contact = new Contact();
        contact.setContactId(1L);
        contact.setContactName("Bob Contact");
        contact.setContactEmail("bob@test.com");
        contact.setOrganization(organization);
        contact.setMember(member);

        deal = new Deal();
        deal.setDealId(1L);
        deal.setDealName("Big Deal");
        deal.setDescription("A big sales deal");
        deal.setDealValue(new BigDecimal("50000.00"));
        deal.setDealStage("Negotiation");
        deal.setExpectedCloseDate(OffsetDateTime.now(ZoneOffset.UTC).plusMonths(1));
        deal.setProbability("60");
        deal.setOrganization(organization);
        deal.setMember(member);
        deal.setAccount(account);
        deal.setContact(contact);
        deal.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        deal.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
    }

    @Test
    void createDeal_ShouldCreateAndReturnDeal() {
        DealDto dto = new DealDto();
        dto.setDealName("Big Deal");
        dto.setDescription("A big sales deal");
        dto.setDealValue(new BigDecimal("50000.00"));
        dto.setDealStage("Negotiation");
        dto.setProbability("60");
        dto.setOrgId(1L);
        dto.setMemberId(1L);
        dto.setAccountId(1L);
        dto.setContactId(1L);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));
        when(dealRepository.save(any(Deal.class))).thenReturn(deal);

        DealDto result = dealService.createDeal(dto);

        assertNotNull(result);
        assertEquals("Big Deal", result.getDealName());
        assertEquals(new BigDecimal("50000.00"), result.getDealValue());
        assertEquals("Negotiation", result.getDealStage());
        assertEquals(1L, result.getOrgId());
        assertEquals(1L, result.getAccountId());
        assertEquals(1L, result.getContactId());

        verify(dealRepository).save(dealCaptor.capture());
        Deal savedDeal = dealCaptor.getValue();
        assertEquals("Big Deal", savedDeal.getDealName());
        assertEquals(organization, savedDeal.getOrganization());
        assertEquals(member, savedDeal.getMember());
        assertEquals(account, savedDeal.getAccount());
        assertEquals(contact, savedDeal.getContact());
    }

    @Test
    void createDeal_ShouldThrowException_WhenOrgIdIsNull() {
        DealDto dto = new DealDto();
        dto.setMemberId(1L);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> dealService.createDeal(dto));
        assertEquals("Organization ID is required", exception.getMessage());
        verify(dealRepository, never()).save(any());
    }

    @Test
    void createDeal_ShouldThrowException_WhenMemberIdIsNull() {
        DealDto dto = new DealDto();
        dto.setOrgId(1L);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> dealService.createDeal(dto));
        assertEquals("Member ID is required", exception.getMessage());
        verify(dealRepository, never()).save(any());
    }

    @Test
    void createDeal_ShouldCreateWithoutAccountAndContact_WhenNotProvided() {
        DealDto dto = new DealDto();
        dto.setDealName("Simple Deal");
        dto.setDealValue(new BigDecimal("10000.00"));
        dto.setDealStage("Prospecting");
        dto.setOrgId(1L);
        dto.setMemberId(1L);

        Deal simpleDeal = new Deal();
        simpleDeal.setDealId(2L);
        simpleDeal.setDealName("Simple Deal");
        simpleDeal.setDealValue(new BigDecimal("10000.00"));
        simpleDeal.setDealStage("Prospecting");
        simpleDeal.setOrganization(organization);
        simpleDeal.setMember(member);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(dealRepository.save(any(Deal.class))).thenReturn(simpleDeal);

        DealDto result = dealService.createDeal(dto);

        assertEquals("Simple Deal", result.getDealName());
        verify(accountRepository, never()).findById(any());
        verify(contactRepository, never()).findById(any());
    }

    @Test
    void getDealsByOrganization_ShouldReturnDeals() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(dealRepository.findByOrganizationWithRelations(organization)).thenReturn(List.of(deal));

        List<DealDto> result = dealService.getDealsByOrganization(1L);

        assertEquals(1, result.size());
        assertEquals("Big Deal", result.get(0).getDealName());
        assertEquals(new BigDecimal("50000.00"), result.get(0).getDealValue());
    }

    @Test
    void getDealsByOrganization_ShouldThrowException_WhenOrgNotFound() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> dealService.getDealsByOrganization(1L));
        assertEquals("Organization not found", exception.getMessage());
    }

    @Test
    void getDealsByMember_ShouldReturnDeals() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(dealRepository.findByMemberWithRelations(member)).thenReturn(List.of(deal));

        List<DealDto> result = dealService.getDealsByMember(1L, 1L);

        assertEquals(1, result.size());
        assertEquals("Big Deal", result.get(0).getDealName());
    }

    @Test
    void getDealsByMember_ShouldThrowException_WhenMemberDoesNotBelongToOrg() {
        when(organizationRepository.findById(2L)).thenReturn(Optional.of(otherOrg));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> dealService.getDealsByMember(2L, 1L));
        assertEquals("Member does not belong to this organization", exception.getMessage());
        verify(dealRepository, never()).findByMemberWithRelations(any());
    }

    @Test
    void getDealsForCurrentUser_ShouldUseMemberOwnedDeals_ForInternalRoles() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(dealRepository.findByMemberWithRelations(member)).thenReturn(List.of(deal));

        List<DealDto> result = dealService.getDealsForCurrentUser(1L, 1L);

        assertEquals(1, result.size());
        verify(dealRepository, never()).findByOrganizationAndPartyEmail(any(), any());
    }

    @Test
    void getDealsForCurrentUser_ShouldUseEmailMatch_ForUserRole() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(userMember));
        when(dealRepository.findByOrganizationAndPartyEmail(organization, "jane@test.com")).thenReturn(List.of(deal));

        List<DealDto> result = dealService.getDealsForCurrentUser(1L, 2L);

        assertEquals(1, result.size());
        verify(dealRepository, never()).findByMemberWithRelations(any());
    }

    @Test
    void getDealsForCurrentUser_ShouldThrowException_WhenMemberNotInOrg() {
        when(organizationRepository.findById(2L)).thenReturn(Optional.of(otherOrg));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> dealService.getDealsForCurrentUser(2L, 1L));
        assertEquals("Member does not belong to this organization", exception.getMessage());
    }

    @Test
    void getDealById_ShouldReturnDeal() {
        when(dealRepository.findByIdWithRelations(1L)).thenReturn(deal);

        DealDto result = dealService.getDealById(1L);

        assertNotNull(result);
        assertEquals("Big Deal", result.getDealName());
    }

    @Test
    void getDealById_ShouldThrowException_WhenNotFound() {
        when(dealRepository.findByIdWithRelations(1L)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> dealService.getDealById(1L));
        assertEquals("Deal not found", exception.getMessage());
    }

    @Test
    void updateDeal_ShouldUpdateAndReturnDeal() {
        DealDto updateDto = new DealDto();
        updateDto.setDealName("Bigger Deal");
        updateDto.setDealValue(new BigDecimal("75000.00"));
        updateDto.setDealStage("Closed Won");

        when(dealRepository.findById(1L)).thenReturn(Optional.of(deal));
        when(dealRepository.save(any(Deal.class))).thenReturn(deal);

        dealService.updateDeal(1L, updateDto);

        verify(dealRepository).save(dealCaptor.capture());
        Deal savedDeal = dealCaptor.getValue();
        assertEquals("Bigger Deal", savedDeal.getDealName());
        assertEquals(new BigDecimal("75000.00"), savedDeal.getDealValue());
        assertEquals("Closed Won", savedDeal.getDealStage());
    }

    @Test
    void updateDeal_ShouldThrowException_WhenDealNotFound() {
        when(dealRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> dealService.updateDeal(1L, new DealDto()));
        assertEquals("Deal not found", exception.getMessage());
        verify(dealRepository, never()).save(any());
    }

    @Test
    void deleteDeal_ShouldDeleteDeal() {
        when(dealRepository.existsById(1L)).thenReturn(true);

        dealService.deleteDeal(1L);

        verify(dealRepository).deleteById(1L);
    }

    @Test
    void deleteDeal_ShouldThrowException_WhenNotFound() {
        when(dealRepository.existsById(1L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> dealService.deleteDeal(1L));
        assertEquals("Deal not found", exception.getMessage());
        verify(dealRepository, never()).deleteById(any());
    }

    @Test
    void getMonthlySummary_ShouldReturnSummary() {
        Object[] row = new Object[]{2024, 1, 10L};
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(dealRepository.findMonthlyDealSummaryByOrganization(organization)).thenReturn(List.<Object[]>of(row));

        var result = dealService.getMonthlySummary(1L);

        assertEquals(1, result.size());
        assertEquals(2024, result.get(0).get("year"));
        assertEquals(1, result.get(0).get("month"));
        assertEquals(10, result.get(0).get("dealCount"));
    }

    @Test
    void getMonthlySummary_ShouldThrowException_WhenOrgNotFound() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> dealService.getMonthlySummary(1L));
        assertEquals("Organization not found", exception.getMessage());
    }

    @Test
    void getStageDistribution_ShouldReturnDistribution() {
        Object[] row = new Object[]{"Negotiation", 5L};
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(dealRepository.findDealStageDistributionByOrganization(organization)).thenReturn(List.<Object[]>of(row));

        var result = dealService.getStageDistribution(1L);

        assertEquals(1, result.size());
        assertEquals("Negotiation", result.get(0).get("stage"));
        assertEquals(5, result.get(0).get("count"));
    }

    @Test
    void getStageDistribution_ShouldThrowException_WhenOrgNotFound() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> dealService.getStageDistribution(1L));
        assertEquals("Organization not found", exception.getMessage());
    }
}
