package com.example.demoredis.service.impl;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import com.example.demoredis.model.Customer;
import com.example.demoredis.repository.CustomerRepository;
import com.example.demoredis.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;


@Service
@CacheConfig(cacheNames = "customerCache")
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    private static final String QUEUE_KEY = "customer_queue";

    @Autowired
    private final RedisTemplate<String, Serializable> redisTemplate;

    public CustomerServiceImpl(RedisTemplate<String, Serializable> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Cacheable(cacheNames = "customers")
    @Override
    public List<Customer> getAll() {
        waitSomeTime();
        return this.customerRepository.findAll();
    }

    @CacheEvict(cacheNames = "customers", allEntries = true)
    @Override
    public Customer add(Customer customer) {
        return this.customerRepository.save(customer);
    }

    @Override
    public Customer addQueue(Customer customer) {
        pushCustomerToQueue(customer);
        return null;
    }

    @CacheEvict(cacheNames = "customers", allEntries = true)
    @Override
    public Customer update(Customer customer) {
        Optional<Customer> optCustomer = this.customerRepository.findById(customer.getId());
        if (!optCustomer.isPresent())
            return null;
        Customer repCustomer = optCustomer.get();
        repCustomer.setName(customer.getName());
        return this.customerRepository.save(repCustomer);
    }

    @Caching(evict = { @CacheEvict(cacheNames = "customer", key = "#id"),
            @CacheEvict(cacheNames = "customers", allEntries = true) })
    @Override
    public void delete(long id) {
        this.customerRepository.deleteById(id);
    }

    @Cacheable(cacheNames = "customer", key = "#id", unless = "#result == null")
    @Override
    public Customer getCustomerById(long id) {
        waitSomeTime();
        return this.customerRepository.findById(id).orElse(null);
    }

    private void waitSomeTime() {
        System.out.println("Long Wait Begin");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Long Wait End");
    }

    public void pushCustomerToQueue(Customer customer) {
        redisTemplate.opsForList().rightPush(QUEUE_KEY, customer);
    }

    public Customer popCustomerFromQueue() {
        return (Customer) redisTemplate.opsForList().leftPop(QUEUE_KEY);
    }
    public void startProcessing() {
        new Thread(() -> {
            while (true) {
                Customer customer = popCustomerFromQueue();
                if (customer != null) {
                    System.out.println("alo");
                }
            }
        }).start();
    }

}
