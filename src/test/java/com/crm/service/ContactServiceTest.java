package com.crm.service;

import com.crm.dto.ContactDto;
import com.crm.entity.*;
import com.crm.repository.AccountRepository;
import com.crm.repository.ContactRepository;
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
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private ContactService contactService;

    @Captor
    private ArgumentCaptor<Contact> contactCaptor;

    private Organization organization;
    private Member member;
    private Account account;
    private Contact contact;
    private ContactDto contactDto;

    @BeforeEach
    void setUp() {
        organization = new Organization("Test Org", "org@test.com");
        organization.setOrgId(1L);

        Role role = new Role();
        role.setRoleId(1L);
        role.setRoleName("Admin");

        member = new Member("John Doe", "john@test.com", "password", organization, role);
        member.setMemberId(1L);

        account = new Account();
        account.setAccountId(1L);
        account.setAccountName("Test Account");
        account.setOrganization(organization);
        account.setMember(member);

        contact = new Contact();
        contact.setContactId(1L);
        contact.setContactName("Bob");
        contact.setContactEmail("bob@test.com");
        contact.setPhone("1234567890");
        contact.setOrganization(organization);
        contact.setMember(member);
        contact.setAccount(account);
        contact.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        contact.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));

        contactDto = new ContactDto();
        contactDto.setContactName("Bob");
        contactDto.setContactEmail("bob@test.com");
        contactDto.setPhone("1234567890");
        contactDto.setOrgId(1L);
        contactDto.setMemberId(1L);
        contactDto.setAccountId(1L);
    }

    @Test
    void createContact_ShouldCreateAndReturnContact() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(contactRepository.save(any(Contact.class))).thenReturn(contact);

        ContactDto result = contactService.createContact(contactDto);

        assertNotNull(result);
        assertEquals("Bob", result.getContactName());
        assertEquals("bob@test.com", result.getContactEmail());
        assertEquals(1L, result.getOrgId());
        assertEquals(1L, result.getAccountId());

        verify(contactRepository).save(contactCaptor.capture());
        Contact savedContact = contactCaptor.getValue();
        assertEquals("Bob", savedContact.getContactName());
        assertEquals(organization, savedContact.getOrganization());
        assertEquals(member, savedContact.getMember());
        assertEquals(account, savedContact.getAccount());
    }

    @Test
    void createContact_ShouldThrowException_WhenOrgIdIsNull() {
        contactDto.setOrgId(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> contactService.createContact(contactDto));
        assertEquals("Organization ID is required", exception.getMessage());
        verify(contactRepository, never()).save(any());
    }

    @Test
    void createContact_ShouldThrowException_WhenMemberIdIsNull() {
        contactDto.setMemberId(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> contactService.createContact(contactDto));
        assertEquals("Member ID is required", exception.getMessage());
        verify(contactRepository, never()).save(any());
    }

    @Test
    void createContact_ShouldCreateWithoutAccount_WhenAccountIdIsNull() {
        contactDto.setAccountId(null);
        Contact contactWithoutAccount = new Contact();
        contactWithoutAccount.setContactId(1L);
        contactWithoutAccount.setContactName("Bob");
        contactWithoutAccount.setContactEmail("bob@test.com");
        contactWithoutAccount.setOrganization(organization);
        contactWithoutAccount.setMember(member);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(contactRepository.save(any(Contact.class))).thenReturn(contactWithoutAccount);

        ContactDto result = contactService.createContact(contactDto);

        assertNotNull(result);
        assertEquals("Bob", result.getContactName());
        verify(accountRepository, never()).findById(any());
    }

    @Test
    void createContact_ShouldThrowException_WhenAccountNotFound() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> contactService.createContact(contactDto));
        assertEquals("Account not found", exception.getMessage());
        verify(contactRepository, never()).save(any());
    }

    @Test
    void getContactsByOrganization_ShouldReturnContacts() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(contactRepository.findByOrganizationWithRelations(organization)).thenReturn(List.of(contact));

        List<ContactDto> result = contactService.getContactsByOrganization(1L);

        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).getContactName());
    }

    @Test
    void getContactsByOrganization_ShouldThrowException_WhenOrgNotFound() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> contactService.getContactsByOrganization(1L));
        assertEquals("Organization not found", exception.getMessage());
    }

    @Test
    void getContactsByMember_ShouldReturnContacts() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(contactRepository.findByMemberWithRelations(member)).thenReturn(List.of(contact));

        List<ContactDto> result = contactService.getContactsByMember(1L);

        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).getContactName());
    }

    @Test
    void getContactsByAccount_ShouldReturnContacts() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(contactRepository.findByAccount(account)).thenReturn(List.of(contact));

        List<ContactDto> result = contactService.getContactsByAccount(1L);

        assertEquals(1, result.size());
        assertEquals("Bob", result.get(0).getContactName());
    }

    @Test
    void getContactById_ShouldReturnContact() {
        when(contactRepository.findByIdWithRelations(1L)).thenReturn(contact);

        ContactDto result = contactService.getContactById(1L);

        assertNotNull(result);
        assertEquals("Bob", result.getContactName());
        assertEquals("bob@test.com", result.getContactEmail());
    }

    @Test
    void getContactById_ShouldThrowException_WhenNotFound() {
        when(contactRepository.findByIdWithRelations(1L)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> contactService.getContactById(1L));
        assertEquals("Contact not found", exception.getMessage());
    }

    @Test
    void updateContact_ShouldUpdateAndReturnContact() {
        ContactDto updateDto = new ContactDto();
        updateDto.setContactName("Bob Updated");
        updateDto.setContactEmail("bob.updated@test.com");
        updateDto.setPhone("9876543210");

        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));
        when(contactRepository.save(any(Contact.class))).thenReturn(contact);

        contactService.updateContact(1L, updateDto);

        verify(contactRepository).save(contactCaptor.capture());
        Contact savedContact = contactCaptor.getValue();
        assertEquals("Bob Updated", savedContact.getContactName());
        assertEquals("bob.updated@test.com", savedContact.getContactEmail());
        assertEquals("9876543210", savedContact.getPhone());
    }

    @Test
    void updateContact_ShouldThrowException_WhenContactNotFound() {
        when(contactRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> contactService.updateContact(1L, new ContactDto()));
        assertEquals("Contact not found", exception.getMessage());
        verify(contactRepository, never()).save(any());
    }

    @Test
    void deleteContact_ShouldDeleteContact() {
        when(contactRepository.existsById(1L)).thenReturn(true);

        contactService.deleteContact(1L);

        verify(contactRepository).deleteById(1L);
    }

    @Test
    void deleteContact_ShouldThrowException_WhenNotFound() {
        when(contactRepository.existsById(1L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> contactService.deleteContact(1L));
        assertEquals("Contact not found", exception.getMessage());
        verify(contactRepository, never()).deleteById(any());
    }
}
