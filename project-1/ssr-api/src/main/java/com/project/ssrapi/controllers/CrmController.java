package com.project.ssrapi.controllers;

import com.example.shinsiri.dtos.req.crm.ReqCreateCrmDTO;
import com.example.shinsiri.dtos.req.crm.ReqSearchCrmDTO;
import com.example.shinsiri.dtos.res.crm.ResSearchCrmDTO;
import com.example.shinsiri.entities.Crm;
import com.example.shinsiri.entities.User;
import com.example.shinsiri.repositories.CrmRepository;
import com.example.shinsiri.services.AuthenticationService;
import com.example.shinsiri.services.CrmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/crms")
public class CrmController {

    private static final Logger logger = LoggerFactory.getLogger(CrmController.class);

    @Autowired
    private CrmRepository crmRepository;

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private CrmService crmService;


    @GetMapping("")
    public List<Crm> getCrms() {
        logger.info("[GET] /crms");
        return crmRepository.findAll();
    }

    @GetMapping("/{id}")
    public Crm getCrmById(
            @PathVariable int id
    ) {
        logger.info("[GET] /crms/{}", id);
        return crmRepository.findById(id).orElse(null);
    }

    @PostMapping("/search")
    public ResSearchCrmDTO searchCrm(
            @RequestBody ReqSearchCrmDTO dto
    ) {
        logger.info("[POST] /crms/search with dto: {}", dto);
        return crmService.searchCrm(dto);
    }

    @PostMapping("")
    public Crm createCrm(
            Authentication authentication,
            @Valid @RequestBody ReqCreateCrmDTO dto
    ) {
        logger.info("[POST] /crms with dto: {}", dto);
        User user = authenticationService.checkAuthentication(authentication);
        return crmService.createCrm(dto, user.getId());
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteCrmById(
            @PathVariable int id
    ) {
        logger.info("[DELETE] /crms/{}", id);
        return crmService.deleteCrm(id);
    }

}
