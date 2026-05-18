package com.crm.repository;

import com.crm.entity.Contact;
import com.crm.entity.Member;
import com.crm.entity.Organization;
import com.crm.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByOrganization(Organization organization);
    Page<Contact> findByOrganization(Organization organization, Pageable pageable);
    List<Contact> findByMember(Member member);
    List<Contact> findByOrganizationAndMember(Organization organization, Member member);
    List<Contact> findByAccount(Account account);
    List<Contact> findByOrganizationAndAccount(Organization organization, Account account);

    @Query("SELECT c FROM Contact c LEFT JOIN FETCH c.organization LEFT JOIN FETCH c.member LEFT JOIN FETCH c.account WHERE c.organization = :organization")
    List<Contact> findByOrganizationWithRelations(@Param("organization") Organization organization);

    @Query("SELECT c FROM Contact c LEFT JOIN FETCH c.organization LEFT JOIN FETCH c.member LEFT JOIN FETCH c.account WHERE c.member = :member")
    List<Contact> findByMemberWithRelations(@Param("member") Member member);

    @Query("SELECT c FROM Contact c LEFT JOIN FETCH c.organization LEFT JOIN FETCH c.member LEFT JOIN FETCH c.account WHERE c.contactId = :contactId")
    Contact findByIdWithRelations(@Param("contactId") Long contactId);

    @Query("SELECT c FROM Contact c LEFT JOIN FETCH c.organization LEFT JOIN FETCH c.member LEFT JOIN FETCH c.account WHERE c.organization = :organization")
    Page<Contact> findByOrganizationWithRelations(@Param("organization") Organization organization, Pageable pageable);

    @Query(value = """
    SELECT c FROM Contact c
    LEFT JOIN FETCH c.organization
    LEFT JOIN FETCH c.member
    LEFT JOIN FETCH c.account
    WHERE c.organization = :organization
    AND (LOWER(c.contactName) LIKE LOWER(CONCAT('%', :search, '%'))
         OR LOWER(c.contactEmail) LIKE LOWER(CONCAT('%', :search, '%'))
         OR c.phone LIKE CONCAT('%', :search, '%'))
    """, countQuery = """
    SELECT COUNT(c) FROM Contact c
    WHERE c.organization = :organization
    AND (LOWER(c.contactName) LIKE LOWER(CONCAT('%', :search, '%'))
         OR LOWER(c.contactEmail) LIKE LOWER(CONCAT('%', :search, '%'))
         OR c.phone LIKE CONCAT('%', :search, '%'))
    """)
    Page<Contact> findByOrganizationWithSearch(@Param("organization") Organization organization,
                                               @Param("search") String search, Pageable pageable);
}
