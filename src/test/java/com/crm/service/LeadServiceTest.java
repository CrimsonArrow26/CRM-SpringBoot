package com.crm.service;

import com.crm.dto.LeadDto;
import com.crm.entity.Lead;
import com.crm.entity.Member;
import com.crm.entity.Organization;
import com.crm.entity.Role;
import com.crm.repository.LeadRepository;
import com.crm.repository.MemberRepository;
import com.crm.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private LeadService leadService;

    @Captor
    private ArgumentCaptor<Lead> leadCaptor;

    private Organization organization;
    private Member member;
    private Lead lead;
    private LeadDto leadDto;

    @BeforeEach
    void setUp() {
        organization = new Organization("Test Org", "org@test.com");
        organization.setOrgId(1L);

        Role role = new Role();
        role.setRoleId(1L);
        role.setRoleName("Admin");

        member = new Member("John Doe", "john@test.com", "password", organization, role);
        member.setMemberId(1L);

        lead = new Lead("Alice", "alice@test.com", "1234567890", organization, member);
        lead.setLeadId(1L);
        lead.setIsVerified(false);
        lead.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        lead.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        leadDto = new LeadDto();
        leadDto.setLeadName("Alice");
        leadDto.setLeadEmail("alice@test.com");
        leadDto.setPhone("1234567890");
        leadDto.setOrgId(1L);
        leadDto.setMemberId(1L);
    }

    @Test
    void createLead_ShouldCreateAndReturnLead() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(leadRepository.save(any(Lead.class))).thenReturn(lead);

        LeadDto result = leadService.createLead(leadDto);

        assertNotNull(result);
        assertEquals("Alice", result.getLeadName());
        assertEquals("alice@test.com", result.getLeadEmail());
        assertEquals("1234567890", result.getPhone());
        assertEquals(1L, result.getOrgId());
        assertEquals(1L, result.getMemberId());

        verify(leadRepository).save(leadCaptor.capture());
        Lead savedLead = leadCaptor.getValue();
        assertEquals("Alice", savedLead.getLeadName());
        assertEquals(organization, savedLead.getOrganization());
        assertEquals(member, savedLead.getMember());
        assertFalse(savedLead.getIsVerified());
    }

    @Test
    void createLead_ShouldThrowException_WhenOrgIdIsNull() {
        leadDto.setOrgId(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> leadService.createLead(leadDto));
        assertEquals("Organization ID is required", exception.getMessage());
        verify(leadRepository, never()).save(any());
    }

    @Test
    void createLead_ShouldThrowException_WhenMemberIdIsNull() {
        leadDto.setMemberId(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> leadService.createLead(leadDto));
        assertEquals("Member ID is required", exception.getMessage());
        verify(leadRepository, never()).save(any());
    }

    @Test
    void createLead_ShouldThrowException_WhenOrganizationNotFound() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> leadService.createLead(leadDto));
        assertEquals("Organization not found", exception.getMessage());
        verify(leadRepository, never()).save(any());
    }

    @Test
    void createLead_ShouldThrowException_WhenMemberNotFound() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> leadService.createLead(leadDto));
        assertEquals("Member not found", exception.getMessage());
        verify(leadRepository, never()).save(any());
    }

    @Test
    void createLead_ShouldDefaultIsVerifiedToFalse_WhenNull() {
        leadDto.setIsVerified(null);
        Lead newLead = new Lead("Alice", "alice@test.com", "1234567890", organization, member);
        newLead.setLeadId(1L);
        newLead.setIsVerified(false);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(leadRepository.save(any(Lead.class))).thenReturn(newLead);

        LeadDto result = leadService.createLead(leadDto);

        assertNotNull(result);
        assertFalse(result.getIsVerified());
        verify(leadRepository).save(leadCaptor.capture());
        assertFalse(leadCaptor.getValue().getIsVerified());
    }

    @Test
    void getLeadsByOrganization_ShouldReturnLeads() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(leadRepository.findByOrganizationWithRelations(organization)).thenReturn(List.of(lead));

        List<LeadDto> result = leadService.getLeadsByOrganization(1L);

        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getLeadName());
    }

    @Test
    void getLeadsByOrganization_ShouldThrowException_WhenOrganizationNotFound() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> leadService.getLeadsByOrganization(1L));
        assertEquals("Organization not found", exception.getMessage());
        verify(leadRepository, never()).findByOrganizationWithRelations(any());
    }

    @Test
    void getLeadsByOrganization_ShouldReturnEmptyList_WhenNoLeads() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(leadRepository.findByOrganizationWithRelations(organization)).thenReturn(List.of());

        List<LeadDto> result = leadService.getLeadsByOrganization(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getLeadsByMember_ShouldReturnLeads() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(leadRepository.findByMemberWithRelations(member)).thenReturn(List.of(lead));

        List<LeadDto> result = leadService.getLeadsByMember(1L);

        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getLeadName());
    }

    @Test
    void getLeadsByMember_ShouldThrowException_WhenMemberNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> leadService.getLeadsByMember(1L));
        assertEquals("Member not found", exception.getMessage());
    }

    @Test
    void getLeadById_ShouldReturnLead() {
        when(leadRepository.findByIdWithRelations(1L)).thenReturn(lead);

        LeadDto result = leadService.getLeadById(1L);

        assertNotNull(result);
        assertEquals("Alice", result.getLeadName());
        assertEquals("alice@test.com", result.getLeadEmail());
    }

    @Test
    void getLeadById_ShouldThrowException_WhenNotFound() {
        when(leadRepository.findByIdWithRelations(1L)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> leadService.getLeadById(1L));
        assertEquals("Lead not found", exception.getMessage());
    }

    @Test
    void updateLead_ShouldUpdateAndReturnLead() {
        LeadDto updateDto = new LeadDto();
        updateDto.setLeadName("Alice Updated");
        updateDto.setLeadEmail("alice.updated@test.com");
        updateDto.setPhone("9876543210");

        when(leadRepository.findById(1L)).thenReturn(Optional.of(lead));
        when(leadRepository.save(any(Lead.class))).thenReturn(lead);

        leadService.updateLead(1L, updateDto);

        verify(leadRepository).save(leadCaptor.capture());
        Lead savedLead = leadCaptor.getValue();
        assertEquals("Alice Updated", savedLead.getLeadName());
        assertEquals("alice.updated@test.com", savedLead.getLeadEmail());
        assertEquals("9876543210", savedLead.getPhone());
    }

    @Test
    void updateLead_ShouldThrowException_WhenLeadNotFound() {
        when(leadRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> leadService.updateLead(1L, new LeadDto()));
        assertEquals("Lead not found", exception.getMessage());
        verify(leadRepository, never()).save(any());
    }

    @Test
    void deleteLead_ShouldDeleteLead() {
        when(leadRepository.existsById(1L)).thenReturn(true);

        leadService.deleteLead(1L);

        verify(leadRepository).deleteById(1L);
    }

    @Test
    void deleteLead_ShouldThrowException_WhenLeadNotFound() {
        when(leadRepository.existsById(1L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> leadService.deleteLead(1L));
        assertEquals("Lead not found", exception.getMessage());
        verify(leadRepository, never()).deleteById(any());
    }

    @Test
    void updateLeadStatus_ShouldUpdateVerification() {
        when(leadRepository.findById(1L)).thenReturn(Optional.of(lead));
        when(leadRepository.save(any(Lead.class))).thenReturn(lead);

        leadService.updateLeadStatus(1L, true);

        verify(leadRepository).save(leadCaptor.capture());
        assertTrue(leadCaptor.getValue().getIsVerified());
    }

    @Test
    void updateLeadStatus_ShouldThrowException_WhenLeadNotFound() {
        when(leadRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> leadService.updateLeadStatus(1L, true));
        assertEquals("Lead not found", exception.getMessage());
        verify(leadRepository, never()).save(any());
    }

    @Test
    void getMonthlySummary_ShouldReturnSummary() {
        Object[] row = new Object[]{2024, 1, 5L};
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(leadRepository.findMonthlyLeadSummaryByOrganization(organization)).thenReturn(List.<Object[]>of(row));

        var result = leadService.getMonthlySummary(1L);

        assertEquals(1, result.size());
        assertEquals(2024, result.get(0).get("year"));
        assertEquals(1, result.get(0).get("month"));
        assertEquals(5, result.get(0).get("leadCount"));
    }

    @Test
    void getMonthlySummary_ShouldThrowException_WhenOrganizationNotFound() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> leadService.getMonthlySummary(1L));
        assertEquals("Organization not found", exception.getMessage());
    }
}
