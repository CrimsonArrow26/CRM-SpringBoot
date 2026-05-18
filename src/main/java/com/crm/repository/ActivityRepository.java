package com.crm.repository;

import com.crm.entity.Activity;
import com.crm.entity.Lead;
import com.crm.entity.Member;
import com.crm.entity.Organization;
import com.crm.entity.Account;
import com.crm.entity.Contact;
import com.crm.entity.Deal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByOrganization(Organization organization);
    Page<Activity> findByOrganization(Organization organization, Pageable pageable);
    List<Activity> findByMember(Member member);
    List<Activity> findByOrganizationAndMember(Organization organization, Member member);
    List<Activity> findByLead(Lead lead);
    List<Activity> findByOrganizationAndLead(Organization organization, Lead lead);

    @Query("SELECT a FROM Activity a LEFT JOIN FETCH a.organization LEFT JOIN FETCH a.member LEFT JOIN FETCH a.account LEFT JOIN FETCH a.contact LEFT JOIN FETCH a.deal LEFT JOIN FETCH a.lead WHERE a.organization = :organization")
    List<Activity> findByOrganizationWithRelations(@Param("organization") Organization organization);

    @Query("SELECT a FROM Activity a LEFT JOIN FETCH a.organization LEFT JOIN FETCH a.member LEFT JOIN FETCH a.account LEFT JOIN FETCH a.contact LEFT JOIN FETCH a.deal LEFT JOIN FETCH a.lead WHERE a.organization = :organization")
    Page<Activity> findByOrganizationWithRelations(@Param("organization") Organization organization, Pageable pageable);

    @Query("SELECT a FROM Activity a LEFT JOIN FETCH a.organization LEFT JOIN FETCH a.member LEFT JOIN FETCH a.account LEFT JOIN FETCH a.contact LEFT JOIN FETCH a.deal LEFT JOIN FETCH a.lead WHERE a.member = :member")
    List<Activity> findByMemberWithRelations(@Param("member") Member member);

    @Query("SELECT a FROM Activity a LEFT JOIN FETCH a.organization LEFT JOIN FETCH a.member LEFT JOIN FETCH a.account LEFT JOIN FETCH a.contact LEFT JOIN FETCH a.deal LEFT JOIN FETCH a.lead WHERE a.activityId = :activityId")
    Activity findByIdWithRelations(@Param("activityId") Long activityId);

    @Query(value = """
    SELECT a FROM Activity a
    LEFT JOIN FETCH a.organization
    LEFT JOIN FETCH a.member
    LEFT JOIN FETCH a.account
    LEFT JOIN FETCH a.contact
    LEFT JOIN FETCH a.deal
    LEFT JOIN FETCH a.lead
    WHERE a.organization = :organization
    AND (LOWER(a.subject) LIKE LOWER(CONCAT('%', :search, '%')))
    """, countQuery = """
    SELECT COUNT(a) FROM Activity a
    WHERE a.organization = :organization
    AND (LOWER(a.subject) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<Activity> findByOrganizationWithSearch(@Param("organization") Organization organization,
                                                @Param("search") String search, Pageable pageable);

    @Query(value = """
    SELECT a FROM Activity a
    LEFT JOIN FETCH a.organization
    LEFT JOIN FETCH a.member
    LEFT JOIN FETCH a.account
    LEFT JOIN FETCH a.contact
    LEFT JOIN FETCH a.deal
    LEFT JOIN FETCH a.lead
    WHERE a.organization = :organization
AND a.activityType = :type
    """, countQuery = """
    SELECT COUNT(a) FROM Activity a
WHERE a.organization = :organization AND a.activityType = :type
     """)
    Page<Activity> findByOrganizationAndType(@Param("organization") Organization organization,
                                              @Param("type") String type, Pageable pageable);

    @Query(value = """
    SELECT a FROM Activity a
    LEFT JOIN FETCH a.organization
    LEFT JOIN FETCH a.member
    LEFT JOIN FETCH a.account
    LEFT JOIN FETCH a.contact
    LEFT JOIN FETCH a.deal
    LEFT JOIN FETCH a.lead
    WHERE a.organization = :organization
    AND a.status = :status
    """, countQuery = """
    SELECT COUNT(a) FROM Activity a
    WHERE a.organization = :organization AND a.status = :status
    """)
    Page<Activity> findByOrganizationAndStatus(@Param("organization") Organization organization,
                                                @Param("status") String status, Pageable pageable);
}
