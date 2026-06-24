package com.ocistart.server.controller;

import com.ocistart.dao.entity.Tenant;
import com.ocistart.server.service.TenantService;
import com.ocistart.common.param.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/tenant")
public class TenantController extends BaseController {

    @Resource
    private TenantService tenantService;

    @GetMapping("/list")
    public String listTenants(Model model) {
        List<Tenant> tenants = tenantService.getAllTenants();
        model.addAttribute("tenants", tenants);
        model.addAttribute("activePage", "tenant-list");
        return "tenant_list";
    }

    @GetMapping("/add")
    public String addTenantForm(Model model) {
        model.addAttribute("tenant", new Tenant());
        model.addAttribute("activePage", "tenant-list");
        return "tenant_form";
    }

    @PostMapping("/save")
    @ResponseBody
    public ApiResponse saveTenant(@RequestBody Tenant tenant) {
        if (tenant.getId() != null && tenant.getId() > 0) {
            return tenantService.updateTenant(tenant);
        }
        return tenantService.addTenant(tenant);
    }

    @PostMapping("/delete")
    @ResponseBody
    public ApiResponse deleteTenant(@RequestParam Long id) {
        return tenantService.deleteTenant(id);
    }

    @GetMapping("/check/{id}")
    @ResponseBody
    public ApiResponse checkAccount(@PathVariable Long id) {
        return tenantService.checkAccountStatus(id);
    }
}
