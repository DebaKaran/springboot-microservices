package com.dk.accounts.services.impl;

import com.dk.accounts.constants.AccountsConstants;
import com.dk.accounts.dtos.AccountsDto;
import com.dk.accounts.dtos.CustomerAccountResponseDto;
import com.dk.accounts.dtos.CustomerDto;
import com.dk.accounts.entity.Accounts;
import com.dk.accounts.entity.Customer;
import com.dk.accounts.exceptions.CustomerAlreadyExistsException;
import com.dk.accounts.exceptions.ResourceNotFoundException;
import com.dk.accounts.mapper.AccountsMapper;
import com.dk.accounts.mapper.CustomerMapper;
import com.dk.accounts.repository.AccountsRepository;
import com.dk.accounts.repository.CustomerRepository;
import com.dk.accounts.services.IAccountService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements IAccountService {

    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;

    @Override
    public void createAccount(CustomerDto customerDto) {
        Customer customer = CustomerMapper.mapToCustomer(customerDto);
        Optional<Customer> optionalCustomer = customerRepository.findByMobileNumber(customerDto.getMobileNumber());
        if(optionalCustomer.isPresent()) {
            throw new CustomerAlreadyExistsException("Customer already registered with given mobileNumber "+customerDto.getMobileNumber());
        }
        Customer savedCustomer =  customerRepository.save(customer);
        accountsRepository.save(createNewAccount(savedCustomer));

    }

    @Override
    public CustomerAccountResponseDto fetchAccount(final String mobileNum) {
        Customer customer = customerRepository.findByMobileNumber(mobileNum)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNum));

        Long customerId = customer.getCustomerId();
        Accounts accounts = accountsRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "customerId", customerId.toString()));

        CustomerDto customerDto = CustomerMapper.mapToCustomerDto(customer);
        AccountsDto accountsDto = AccountsMapper.mapToAccountsDto(accounts);
        return new CustomerAccountResponseDto(customerDto, accountsDto);
    }

    /**
     * @param customerAccountResponseDto - CustomerAccountResponseDto Object
     * @return boolean indicating if the update of Account details is successful or not
     */
    @Override
    public boolean updateAccount(final CustomerAccountResponseDto customerAccountResponseDto) {
        boolean isUpdated = false;
        AccountsDto accountsDto = customerAccountResponseDto.getAccountsDto();
        if(accountsDto !=null ){
            Accounts accounts = accountsRepository.findById(accountsDto.getAccountNumber()).orElseThrow(
                    () -> new ResourceNotFoundException("Account", "AccountNumber", accountsDto.getAccountNumber().toString())
            );
            AccountsMapper.mapToAccounts(accountsDto, accounts);
            accounts = accountsRepository.save(accounts);

            Long customerId = accounts.getCustomerId();
            Customer customer = customerRepository.findById(customerId).orElseThrow(
                    () -> new ResourceNotFoundException("Customer", "CustomerID", customerId.toString())
            );
            CustomerMapper.mapToCustomer(customerAccountResponseDto.getCustomerDto(),customer);
            customerRepository.save(customer);
            isUpdated = true;
        }
        return  isUpdated;
    }

    /**
     * @param mobileNumber - Input Mobile Number
     * @return boolean indicating if the delete of Account details is successful or not
     */
    @Override
    public boolean deleteAccount(String mobileNumber) {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber)
        );
        accountsRepository.deleteByCustomerId(customer.getCustomerId());
        customerRepository.deleteById(customer.getCustomerId());
        return true;
    }


    /**
     * @param customer - Customer Object
     * @return the new account details
     */
    private Accounts createNewAccount(Customer customer) {
        Accounts newAccount = new Accounts();
        newAccount.setCustomerId(customer.getCustomerId());
        long randomAccNumber = 1000000000L + new Random().nextInt(900000000);

        newAccount.setAccountNumber(randomAccNumber);
        newAccount.setAccountType(AccountsConstants.SAVINGS);
        newAccount.setBranchAddress(AccountsConstants.ADDRESS);

        // 🛠️ Fix for error
        // below two lines are not required as we are using AuditAwareImpl
//        newAccount.setCreatedAt(LocalDateTime.now());
//        newAccount.setCreatedBy("system");
        return newAccount;
    }
}
