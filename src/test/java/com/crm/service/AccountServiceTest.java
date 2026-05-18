package com.crm.service;

import com.crm.dto.AccountDto;
import com.crm.entity.Account;
import com.crm.entity.Member;
import com.crm.entity.Organization;
import com.crm.entity.Role;
import com.crm.repository.AccountRepository;
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
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AccountService accountService;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    private Organization organization;
    private Member member;
    private Account account;

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
        account.setAccountName("Acme Corp");
        account.setEmail("acme@test.com");
        account.setPhone("1234567890");
        account.setWebsite("https://acme.com");
        account.setIndustry("Technology");
        account.setOrganization(organization);
        account.setMember(member);
        account.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        account.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
    }

    @Test
    void createAccount_ShouldCreateAndReturnAccount() {
        AccountDto dto = new AccountDto();
        dto.setAccountName("Acme Corp");
        dto.setEmail("acme@test.com");
        dto.setPhone("1234567890");
        dto.setOrgId(1L);
        dto.setMemberId(1L);

        when(accountRepository.existsByEmail("acme@test.com")).thenReturn(false);
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountDto result = accountService.createAccount(dto);

        assertNotNull(result);
        assertEquals("Acme Corp", result.getAccountName());
        assertEquals("acme@test.com", result.getEmail());

        verify(accountRepository).save(accountCaptor.capture());
        Account savedAccount = accountCaptor.getValue();
        assertEquals("Acme Corp", savedAccount.getAccountName());
        assertEquals(organization, savedAccount.getOrganization());
        assertEquals(member, savedAccount.getMember());
    }

    @Test
    void createAccount_ShouldThrowException_WhenOrgIdIsNull() {
        AccountDto dto = new AccountDto();
        dto.setMemberId(1L);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> accountService.createAccount(dto));
        assertEquals("Organization ID is required", exception.getMessage());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void createAccount_ShouldThrowException_WhenMemberIdIsNull() {
        AccountDto dto = new AccountDto();
        dto.setOrgId(1L);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> accountService.createAccount(dto));
        assertEquals("Member ID is required", exception.getMessage());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void createAccount_ShouldThrowException_WhenEmailExists() {
        AccountDto dto = new AccountDto();
        dto.setEmail("existing@test.com");
        dto.setOrgId(1L);
        dto.setMemberId(1L);

        when(accountRepository.existsByEmail("existing@test.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> accountService.createAccount(dto));
        assertEquals("Account with this email already exists", exception.getMessage());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void createAccount_ShouldSkipEmailCheck_WhenEmailIsNull() {
        AccountDto dto = new AccountDto();
        dto.setOrgId(1L);
        dto.setMemberId(1L);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountService.createAccount(dto);

        verify(accountRepository, never()).existsByEmail(any());
    }

    @Test
    void getAccountsByOrganization_ShouldReturnAccounts() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(organization));
        when(accountRepository.findByOrganizationWithRelations(organization)).thenReturn(List.of(account));

        List<AccountDto> result = accountService.getAccountsByOrganization(1L);

        assertEquals(1, result.size());
        assertEquals("Acme Corp", result.get(0).getAccountName());
    }

    @Test
    void getAccountsByOrganization_ShouldThrowException_WhenOrgNotFound() {
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> accountService.getAccountsByOrganization(1L));
        assertEquals("Organization not found", exception.getMessage());
    }

    @Test
    void getAccountById_ShouldReturnAccount() {
        when(accountRepository.findByIdWithRelations(1L)).thenReturn(account);

        AccountDto result = accountService.getAccountById(1L);

        assertNotNull(result);
        assertEquals("Acme Corp", result.getAccountName());
        assertEquals("acme@test.com", result.getEmail());
    }

    @Test
    void getAccountById_ShouldThrowException_WhenNotFound() {
        when(accountRepository.findByIdWithRelations(1L)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> accountService.getAccountById(1L));
        assertEquals("Account not found", exception.getMessage());
    }

    @Test
    void updateAccount_ShouldUpdateAndReturnAccount() {
        AccountDto updateDto = new AccountDto();
        updateDto.setAccountName("Acme Updated");
        updateDto.setEmail("acme@test.com");
        updateDto.setPhone("9876543210");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountService.updateAccount(1L, updateDto);

        verify(accountRepository).save(accountCaptor.capture());
        Account savedAccount = accountCaptor.getValue();
        assertEquals("Acme Updated", savedAccount.getAccountName());
        assertEquals("9876543210", savedAccount.getPhone());
    }

    @Test
    void updateAccount_ShouldThrowException_WhenEmailAlreadyUsedByAnotherAccount() {
        AccountDto updateDto = new AccountDto();
        updateDto.setEmail("other@test.com");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.existsByEmail("other@test.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> accountService.updateAccount(1L, updateDto));
        assertEquals("Account with this email already exists", exception.getMessage());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void updateAccount_ShouldAllowSameEmail_WhenEmailUnchanged() {
        AccountDto updateDto = new AccountDto();
        updateDto.setAccountName("Acme Corp");
        updateDto.setEmail("acme@test.com");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountService.updateAccount(1L, updateDto);

        verify(accountRepository, never()).existsByEmail(any());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void updateAccount_ShouldThrowException_WhenAccountNotFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> accountService.updateAccount(1L, new AccountDto()));
        assertEquals("Account not found", exception.getMessage());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void deleteAccount_ShouldDeleteAccount() {
        when(accountRepository.existsById(1L)).thenReturn(true);

        accountService.deleteAccount(1L);

        verify(accountRepository).deleteById(1L);
    }

    @Test
    void deleteAccount_ShouldThrowException_WhenNotFound() {
        when(accountRepository.existsById(1L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> accountService.deleteAccount(1L));
        assertEquals("Account not found", exception.getMessage());
        verify(accountRepository, never()).deleteById(any());
    }
}
