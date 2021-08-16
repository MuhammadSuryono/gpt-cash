package com.gpt.product.gpcash.corporate.test;


import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.gpt.component.common.eai.EAIEngine;

@RestController
@ResponseBody
@Order(888)
public class TestEAIController2 {
    private static final String baseURL = "/corp/eai";

    @Autowired
    private EAIEngine serviceLocator;
    
    @RequestMapping(baseURL + "/{serviceName}")
    public Map<String, Object> genericHandler(HttpServletRequest request, @PathVariable String serviceName, @RequestBody Map<String, Object> param) throws Exception {
        return serviceLocator.invokeService(serviceName, param);
    }
    
}
