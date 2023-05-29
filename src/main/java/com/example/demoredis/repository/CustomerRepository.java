package com.example.demoredis.repository;

import com.example.demoredis.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CustomerRepository extends JpaRepository<Customer, Long>{

}
