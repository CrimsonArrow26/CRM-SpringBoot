package com.crm.repository;

import com.crm.entity.Lead;
import com.crm.entity.Member;
import com.crm.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    List<Lead> findByOrganization(Organization organization);
    Page<Lead> findByOrganization(Organization organization, Pageable pageable);
    List<Lead> findByMember(Member member);
    List<Lead> findByOrganizationAndMember(Organization organization, Member member);
    List<Lead> findByOrganizationAndIsVerified(Organization organization, Boolean isVerified);

    @Query("SELECT l FROM Lead l LEFT JOIN FETCH l.organization LEFT JOIN FETCH l.member WHERE l.organization = :organization")
    List<Lead> findByOrganizationWithRelations(@Param("organization") Organization organization);

    @Query("SELECT l FROM Lead l LEFT JOIN FETCH l.organization LEFT JOIN FETCH l.member WHERE l.member = :member")
    List<Lead> findByMemberWithRelations(@Param("member") Member member);

    @Query("SELECT l FROM Lead l LEFT JOIN FETCH l.organization LEFT JOIN FETCH l.member WHERE l.leadId = :leadId")
    Lead findByIdWithRelations(@Param("leadId") Long leadId);

    @Query("SELECT l FROM Lead l LEFT JOIN FETCH l.organization LEFT JOIN FETCH l.member WHERE l.organization = :organization")
    Page<Lead> findByOrganizationWithRelations(@Param("organization") Organization organization, Pageable pageable);

    @Query(value = """
    SELECT l FROM Lead l LEFT JOIN FETCH l.organization LEFT JOIN FETCH l.member
    WHERE l.organization = :organization
    AND (LOWER(l.leadName) LIKE LOWER(CONCAT('%', :search, '%'))
         OR LOWER(l.leadEmail) LIKE LOWER(CONCAT('%', :search, '%'))
         OR l.phone LIKE CONCAT('%', :search, '%'))
    """)
    Page<Lead> findByOrganizationWithSearch(@Param("organization") Organization organization,
                                              @Param("search") String search, Pageable pageable);

    @Query(value = """
    SELECT l FROM Lead l LEFT JOIN FETCH l.organization LEFT JOIN FETCH l.member
    WHERE l.organization = :organization
    AND l.isVerified = :isVerified
    AND (LOWER(l.leadName) LIKE LOWER(CONCAT('%', :search, '%'))
         OR LOWER(l.leadEmail) LIKE LOWER(CONCAT('%', :search, '%'))
         OR l.phone LIKE CONCAT('%', :search, '%'))
    """)
    Page<Lead> findByOrganizationAndIsVerifiedWithSearch(@Param("organization") Organization organization,
                                                          @Param("isVerified") Boolean isVerified,
                                                          @Param("search") String search, Pageable pageable);

    @Query(value = """
    SELECT l FROM Lead l LEFT JOIN FETCH l.organization LEFT JOIN FETCH l.member
    WHERE l.organization = :organization
    AND l.isVerified = :isVerified
    """)
    Page<Lead> findByOrganizationAndIsVerified(@Param("organization") Organization organization,
                                               @Param("isVerified") Boolean isVerified, Pageable pageable);

    @Query(value = """
    SELECT l FROM Lead l
    WHERE l.organization = :organization
    AND (LOWER(l.leadName) LIKE LOWER(CONCAT('%', :search, '%'))
         OR LOWER(l.leadEmail) LIKE LOWER(CONCAT('%', :search, '%'))
         OR l.phone LIKE CONCAT('%', :search, '%'))
    """, countQuery = """
    SELECT COUNT(l) FROM Lead l
    WHERE l.organization = :organization
    AND (LOWER(l.leadName) LIKE LOWER(CONCAT('%', :search, '%'))
         OR LOWER(l.leadEmail) LIKE LOWER(CONCAT('%', :search, '%'))
         OR l.phone LIKE CONCAT('%', :search, '%'))
    """)
    Page<Lead> findByOrganizationSearchOnly(@Param("organization") Organization organization,
                                            @Param("search") String search, Pageable pageable);

    @Query(value = """
    SELECT YEAR(l.createdAt), MONTH(l.createdAt), COUNT(l)
    FROM Lead l
    WHERE l.organization = :organization
    GROUP BY YEAR(l.createdAt), MONTH(l.createdAt)
    ORDER BY YEAR(l.createdAt), MONTH(l.createdAt)
    """)
    List<Object[]> findMonthlyLeadSummaryByOrganization(@Param("organization") Organization organization);

}
