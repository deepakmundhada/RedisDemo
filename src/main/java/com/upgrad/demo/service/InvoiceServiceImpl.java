package com.upgrad.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import com.upgrad.demo.configuration.RedisConfig;
import com.upgrad.demo.entity.Invoice;
import com.upgrad.demo.exception.InvoiceNotFoundException;
import com.upgrad.demo.repository.InvoiceRepository;

@Service
public class InvoiceServiceImpl implements InvoiceService {

	@Autowired
    private InvoiceRepository invoiceRepo;
	
	@Autowired
	private RedisConfig redisConfig;

    @Override
    public Invoice saveInvoice(Invoice inv) {

        return invoiceRepo.save(inv);
    }

    @Override
    @CachePut(value="Invoice", key="#invId")
    public Invoice updateInvoice(Invoice inv, Integer invId) {
       Invoice invoice = invoiceRepo.findById(invId)
            .orElseThrow(() -> new InvoiceNotFoundException("Invoice Not Found"));
       invoice.setInvAmount(inv.getInvAmount());
       invoice.setInvName(inv.getInvName());
       return invoiceRepo.save(invoice);
    }

    @Override
    @CacheEvict(value="Invoice", key="#invId")
    // @CacheEvict(value="Invoice", allEntries=true) //in case there are multiple records to delete
    public void deleteInvoice(Integer invId) {
       Invoice invoice = invoiceRepo.findById(invId)
           .orElseThrow(() -> new InvoiceNotFoundException("Invoice Not Found"));
       invoiceRepo.delete(invoice);
    }

    @Override
    @Cacheable(value="Invoice", key="#invId")
    public Invoice getOneInvoice(Integer invId) {
       Invoice invoice = invoiceRepo.findById(invId)
         .orElseThrow(() -> new InvoiceNotFoundException("Invoice Not Found"));
       return invoice;
    }

    @Override
    @Cacheable(value="Invoice")
    public List<Invoice> getAllInvoices() {
       return invoiceRepo.findAll();
    }
    
	public String stringCommand(String key) {
		System.out.println("@@@@@@@@@@@@@@@ key is " + key);
		byte[] b = redisConfig.redisConnection().stringCommands().get(key.getBytes());
    	System.out.println(new String(b));

    	
    	List<Object> txResults = redisConfig.redisTemplate().execute(new SessionCallback<List<Object>>() {
    	    @SuppressWarnings("unchecked")
			public List<Object> execute(RedisOperations operations) throws DataAccessException {
    	        operations.multi();
    	        operations.opsForSet().add("keySet", "value1", "value2", "value3");
    	        operations.opsForSet().randomMember("keySet");
    	        operations.boundSetOps("keySet").members();
    	        
    	        operations.opsForList().leftPushAll("keyList", 10, 20, 30);
    	        operations.opsForList().range("keyList", 0, 1);
    	        operations.boundListOps("keyList").size();
    	        
    	        operations.opsForValue().set("50", new Invoice(50, "codetest", (double) 500));
    	        operations.opsForValue().get("50");
    	        operations.boundValueOps("50").get();
    	        return operations.exec();
    	    }
    	});
    	
    	
    	List<Object> invoice = redisConfig.stringRedisTemplate().execute(new SessionCallback<List<Object>>() {
    	    @SuppressWarnings("unchecked")
			public List<Object> execute(RedisOperations operations) throws DataAccessException {
    	        operations.multi();
    	        operations.boundValueOps(key).get();
    	        return operations.exec();
    	    }
    	});
    	
    	return (String) invoice.get(0);
    }
}
