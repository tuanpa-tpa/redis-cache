package com.example.demoredis.service;

import com.example.demoredis.model.Customer;

import java.util.List;

public interface CustomerService {

    public List<Customer> getAll();

    public Customer add(Customer customer);

    public Customer update(Customer customer);

    public void delete(long id);

    public Customer getCustomerById(long id);
}
