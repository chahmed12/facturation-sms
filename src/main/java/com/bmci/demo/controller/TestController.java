package com.bmci.demo.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bmci.demo.model.BanqueDigital;
import com.bmci.demo.repository.BanqueDigitalRepository;

@RestController
@RequestMapping("/test")
public class TestController {
  
    @Autowired
    private BanqueDigitalRepository banqueDigitalRepository;


    @GetMapping("/allclients")
    public List<BanqueDigital> getAllClients() {
        return banqueDigitalRepository.findAll(); 
    }

    @GetMapping("/ping")
    public String ping() {
        return "API fonctionnelle ";
    }
    @GetMapping("/count")
    public long countClients() {
        return banqueDigitalRepository.count();
    }
    @GetMapping("/clients")
    public Page<BanqueDigital> getClientsPagines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return banqueDigitalRepository.findAll(pageable);
}

}

    



