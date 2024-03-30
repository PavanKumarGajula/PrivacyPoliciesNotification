package com.privacypolicies.PrivacyPoliciesNotification.Controllers;

import com.privacypolicies.PrivacyPoliciesNotification.Service.WebScrapingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
public class HomeController {


    @RequestMapping(value = {"","/","/home"})
    public String homePage(){
        return "home.html";
    }


}
