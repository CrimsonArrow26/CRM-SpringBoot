package com.crm.repository;

import com.crm.entity.Account;
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
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByOrganization(Organization organization);
    Page<Account> findByOrganization(Organization organization, Pageable pageable);
    List<Account> findByMember(Member member);
    List<Account> findByOrganizationAndMember(Organization organization, Member member);

    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.organization LEFT JOIN FETCH a.member WHERE a.organization = :organization")
    List<Account> findByOrganizationWithRelations(@Param("organization") Organization organization);

    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.organization LEFT JOIN FETCH a.member WHERE a.organization = :organization")
    Page<Account> findByOrganizationWithRelations(@Param("organization") Organization organization, Pageable pageable);

    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.organization LEFT JOIN FETCH a.member WHERE a.member = :member")
    List<Account> findByMemberWithRelations(@Param("member") Member member);

    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.organization LEFT JOIN FETCH a.member WHERE a.accountId = :accountId")
    Account findByIdWithRelations(@Param("accountId") Long accountId);

    @Query(value = """
    SELECT a FROM Account a
    LEFT JOIN FETCH a.organization
    LEFT JOIN FETCH a.member
    WHERE a.organization = :organization
    AND (LOWER(a.accountName) LIKE LOWER(CONCAT('%', :search, '%'))
         OR LOWER(a.email) LIKE LOWER(CONCAT('%', :search, '%'))
         OR LOWER(a.industry) LIKE LOWER(CONCAT('%', :search, '%')))
    """, countQuery = """
    SELECT COUNT(a) FROM Account a
    WHERE a.organization = :organization
    AND (LOWER(a.accountName) LIKE LOWER(CONCAT('%', :search, '%'))
         OR LOWER(a.email) LIKE LOWER(CONCAT('%', :search, '%'))
         OR LOWER(a.industry) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<Account> findByOrganizationWithSearch(@Param("organization") Organization organization,
                                               @Param("search") String search, Pageable pageable);

    boolean existsByEmail(String email);
}
