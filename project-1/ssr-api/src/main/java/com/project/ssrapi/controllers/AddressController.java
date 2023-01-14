package com.project.ssrapi.controllers;

import com.example.shinsiri.dtos.res.address.AddressDTO;
import com.example.shinsiri.dtos.res.address.DistrictDTO;
import com.example.shinsiri.dtos.res.address.ProvinceDTO;
import com.example.shinsiri.services.AddressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {

    private static final Logger logger = LoggerFactory.getLogger(AddressController.class);

    @Autowired
    private AddressService addressService;

    @GetMapping("/searchAll")
    public AddressDTO searchAll(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        logger.info("[GET] /address/searchAll - search: {}", search);
        return addressService.searchAll(search, page, size);
    }

    @GetMapping("/searchDistrict")
    public AddressDTO searchDistrict(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        logger.info("[GET] /address/searchDistrict - search: {}", search);
        return addressService.searchDistrict(search, page, size);
    }

    @GetMapping("/provinces")
    public List<ProvinceDTO> province(){
        logger.info("[GET] /address/provinces");
        return addressService.getProvince();
    }

    @GetMapping("/districts")
    public List<DistrictDTO> district(@RequestParam int provinceId){
        logger.info("[GET] /address/districts");
        return addressService.getDistrict(provinceId);
    }

}
