package com.crm.repository;

import com.crm.entity.Deal;
import com.crm.entity.Member;
import com.crm.entity.Organization;
import com.crm.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {
	List<Deal> findByOrganization(Organization organization);
	Page<Deal> findByOrganization(Organization organization, Pageable pageable);
	List<Deal> findByMember(Member member);
	List<Deal> findByOrganizationAndMember(Organization organization, Member member);
	List<Deal> findByContactsContaining(Contact contact);
	List<Deal> findByOrganizationAndContactsContaining(Organization organization, Contact contact);

	@Query("SELECT d FROM Deal d LEFT JOIN FETCH d.organization LEFT JOIN FETCH d.member LEFT JOIN FETCH d.account LEFT JOIN FETCH d.contact WHERE d.organization = :organization")
	List<Deal> findByOrganizationWithRelations(@Param("organization") Organization organization);

	@Query("SELECT d FROM Deal d LEFT JOIN FETCH d.organization LEFT JOIN FETCH d.member LEFT JOIN FETCH d.account LEFT JOIN FETCH d.contact WHERE d.organization = :organization")
	Page<Deal> findByOrganizationWithRelations(@Param("organization") Organization organization, Pageable pageable);

	@Query("SELECT d FROM Deal d LEFT JOIN FETCH d.organization LEFT JOIN FETCH d.member LEFT JOIN FETCH d.account LEFT JOIN FETCH d.contact WHERE d.member = :member")
	List<Deal> findByMemberWithRelations(@Param("member") Member member);

	@Query("SELECT d FROM Deal d LEFT JOIN FETCH d.organization LEFT JOIN FETCH d.member LEFT JOIN FETCH d.account LEFT JOIN FETCH d.contact WHERE d.dealId = :dealId")
	Deal findByIdWithRelations(@Param("dealId") Long dealId);

	@Query("SELECT d FROM Deal d " +
	       "LEFT JOIN FETCH d.organization o " +
	       "LEFT JOIN FETCH d.member m " +
	       "LEFT JOIN FETCH d.account a " +
	       "LEFT JOIN FETCH d.contact c " +
	       "WHERE o = :organization " +
	       "AND (" +
	       "  (c IS NOT NULL AND LOWER(c.contactEmail) = LOWER(:email)) " +
	       "   OR " +
	       "  (a IS NOT NULL AND LOWER(a.email) = LOWER(:email))" +
	       ")")
	List<Deal> findByOrganizationAndPartyEmail(@Param("organization") Organization organization,
	                                           @Param("email") String email);

	@Query(value = """
	SELECT d FROM Deal d
	LEFT JOIN FETCH d.organization
	LEFT JOIN FETCH d.member
	LEFT JOIN FETCH d.account
	LEFT JOIN FETCH d.contact
	WHERE d.organization = :organization
	AND (LOWER(d.dealName) LIKE LOWER(CONCAT('%', :search, '%'))
	     OR LOWER(d.dealStage) LIKE LOWER(CONCAT('%', :search, '%')))
	""", countQuery = """
	SELECT COUNT(d) FROM Deal d
	WHERE d.organization = :organization
	AND (LOWER(d.dealName) LIKE LOWER(CONCAT('%', :search, '%'))
	     OR LOWER(d.dealStage) LIKE LOWER(CONCAT('%', :search, '%')))
	""")
	Page<Deal> findByOrganizationWithSearch(@Param("organization") Organization organization,
	                                          @Param("search") String search, Pageable pageable);

	@Query(value = """
	SELECT d FROM Deal d
	LEFT JOIN FETCH d.organization
	LEFT JOIN FETCH d.member
	LEFT JOIN FETCH d.account
	LEFT JOIN FETCH d.contact
	WHERE d.organization = :organization
	AND d.dealStage = :stage
	AND (LOWER(d.dealName) LIKE LOWER(CONCAT('%', :search, '%')))
	""", countQuery = """
	SELECT COUNT(d) FROM Deal d
	WHERE d.organization = :organization
	AND d.dealStage = :stage
	AND (LOWER(d.dealName) LIKE LOWER(CONCAT('%', :search, '%')))
	""")
	Page<Deal> findByOrganizationAndStageWithSearch(@Param("organization") Organization organization,
	                                                  @Param("stage") String stage,
	                                                  @Param("search") String search, Pageable pageable);

	@Query(value = """
	SELECT d FROM Deal d
	LEFT JOIN FETCH d.organization
	LEFT JOIN FETCH d.member
	LEFT JOIN FETCH d.account
	LEFT JOIN FETCH d.contact
	WHERE d.organization = :organization
	AND d.dealStage = :stage
	""", countQuery = """
	SELECT COUNT(d) FROM Deal d
	WHERE d.organization = :organization AND d.dealStage = :stage
	""")
	Page<Deal> findByOrganizationAndStage(@Param("organization") Organization organization,
	                                       @Param("stage") String stage, Pageable pageable);

	@Query("SELECT YEAR(d.createdAt), MONTH(d.createdAt), COUNT(d) " +
	       "FROM Deal d WHERE d.organization = :organization " +
	       "GROUP BY YEAR(d.createdAt), MONTH(d.createdAt) ORDER BY YEAR(d.createdAt), MONTH(d.createdAt)")
	List<Object[]> findMonthlyDealSummaryByOrganization(@Param("organization") Organization organization);

	@Query("SELECT d.dealStage, COUNT(d) FROM Deal d WHERE d.organization = :organization GROUP BY d.dealStage")
	List<Object[]> findDealStageDistributionByOrganization(@Param("organization") Organization organization);
}
