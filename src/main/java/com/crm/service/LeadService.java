package com.crm.service;

import com.crm.dto.LeadDto;
import com.crm.entity.Lead;
import com.crm.entity.Member;
import com.crm.entity.Organization;
import com.crm.repository.LeadRepository;
import com.crm.repository.MemberRepository;
import com.crm.repository.OrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LeadService {

    private final LeadRepository leadRepository;
    private final OrganizationRepository organizationRepository;
    private final MemberRepository memberRepository;

    public LeadService(LeadRepository leadRepository, OrganizationRepository organizationRepository,
                       MemberRepository memberRepository) {
        this.leadRepository = leadRepository;
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
    }
    
    @Transactional
    public LeadDto createLead(LeadDto leadDto) {
        // Validate that orgId and memberId are provided (auto-populated by controller)
        if (leadDto.getOrgId() == null) {
            throw new RuntimeException("Organization ID is required");
        }
        if (leadDto.getMemberId() == null) {
            throw new RuntimeException("Member ID is required");
        }
        
        Organization organization = organizationRepository.findById(leadDto.getOrgId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        
        Member member = memberRepository.findById(leadDto.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));
        
        Lead lead = new Lead();
        lead.setLeadName(leadDto.getLeadName());
        lead.setLeadEmail(leadDto.getLeadEmail());
        lead.setPhone(leadDto.getPhone());
        lead.setIsVerified(leadDto.getIsVerified() != null ? leadDto.getIsVerified() : false);
        lead.setOrganization(organization);
        lead.setMember(member);
        
        Lead savedLead = leadRepository.save(lead);
        return convertToDto(savedLead);
    }
    
    @Transactional(readOnly = true)
    public List<LeadDto> getLeadsByOrganization(Long orgId) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        return leadRepository.findByOrganizationWithRelations(organization)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<LeadDto> getLeadsByOrganizationPaginated(Long orgId, Boolean isVerified,
                                                        String search, Pageable pageable) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        org.springframework.data.domain.Page<Lead> page;

        if (search != null && !search.isBlank()) {
            if (isVerified != null) {
                page = leadRepository.findByOrganizationAndIsVerifiedWithSearch(organization, isVerified, search, pageable);
            } else {
                page = leadRepository.findByOrganizationSearchOnly(organization, search, pageable);
            }
        } else {
            if (isVerified != null) {
                page = leadRepository.findByOrganizationAndIsVerified(organization, isVerified, pageable);
            } else {
                page = leadRepository.findByOrganizationWithRelations(organization, pageable);
            }
        }

        return page.map(this::convertToDto);
    }
    
    @Transactional(readOnly = true)
    public List<LeadDto> getLeadsByMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        
        return leadRepository.findByMemberWithRelations(member)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public LeadDto getLeadById(Long leadId) {
        Lead lead = leadRepository.findByIdWithRelations(leadId);
        if (lead == null) {
            throw new RuntimeException("Lead not found");
        }
        return convertToDto(lead);
    }
    
    @Transactional
    public LeadDto updateLead(Long leadId, LeadDto leadDto) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        
        lead.setLeadName(leadDto.getLeadName());
        lead.setLeadEmail(leadDto.getLeadEmail());
        lead.setPhone(leadDto.getPhone());
        
        if (leadDto.getIsVerified() != null) {
            lead.setIsVerified(leadDto.getIsVerified());
        }
        
        Lead savedLead = leadRepository.save(lead);
        return convertToDto(savedLead);
    }
    
    @Transactional
    public void deleteLead(Long leadId) {
        if (!leadRepository.existsById(leadId)) {
            throw new RuntimeException("Lead not found");
        }
        leadRepository.deleteById(leadId);
    }
    
    @Transactional
    public LeadDto updateLeadStatus(Long leadId, Boolean isVerified) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found"));
        
        lead.setIsVerified(isVerified);
        Lead savedLead = leadRepository.save(lead);
        return convertToDto(savedLead);
    }

      @Transactional(readOnly = true)
    public List<Map<String, Object>> getMonthlySummary(Long orgId) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        List<Object[]> rows = leadRepository.findMonthlyLeadSummaryByOrganization(organization);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> row = new HashMap<>();
            row.put("year", ((Number) r[0]).longValue());
            row.put("month", ((Number) r[1]).longValue());
            row.put("leadCount", ((Number) r[2]).longValue());
            result.add(row);
        }
        return result;
    }
    
    private LeadDto convertToDto(Lead lead) {
        return new LeadDto(
            lead.getLeadId(),
            lead.getLeadName(),
            lead.getLeadEmail(),
            lead.getPhone(),
            lead.getIsVerified(),
            lead.getOrganization().getOrgId(),
            lead.getMember().getMemberId(),
            lead.getMember().getName(),
            lead.getCreatedAt(),
            lead.getUpdatedAt()
        );
    }
}
