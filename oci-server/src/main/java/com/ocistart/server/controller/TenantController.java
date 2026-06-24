package com.ocistart.server.controller;

import com.ocistart.dao.entity.Tenant;
import com.ocistart.server.service.TenantService;
import com.ocistart.common.param.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Controller
@RequestMapping("/tenant")
public class TenantController extends BaseController {

    @Resource
    private TenantService tenantService;

    @GetMapping("/list")
    public String listTenants(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) String cloudType,
                              @RequestParam(required = false) Boolean emailServiceEnabled,
                              Model model) {
        Page<Tenant> tenantPage = tenantService.searchTenants(
                keyword,
                cloudType,
                emailServiceEnabled,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        );
        model.addAttribute("tenants", tenantPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tenantPage.getTotalPages());
        model.addAttribute("totalElements", tenantPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("cloudType", cloudType);
        model.addAttribute("emailServiceEnabled", emailServiceEnabled);
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

    @PostMapping("/saveWithKey")
    @ResponseBody
    public ApiResponse saveTenantWithKey(@ModelAttribute Tenant tenant,
                                         @RequestParam(value = "privateKeyFile", required = false) MultipartFile privateKeyFile) throws IOException {
        if (privateKeyFile != null && !privateKeyFile.isEmpty()) {
            tenant.setKeyFile(new String(privateKeyFile.getBytes(), StandardCharsets.UTF_8));
        }
        if (tenant.getId() != null && tenant.getId() > 0) {
            return tenantService.updateTenant(tenant);
        }
        return tenantService.addTenant(tenant);
    }

    @GetMapping("/options")
    @ResponseBody
    public ApiResponse<List<Tenant>> tenantOptions() {
        return ApiResponse.success(tenantService.getAllTenants());
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

    @PostMapping("/updateCustomName")
    @ResponseBody
    public ApiResponse updateCustomName(@RequestParam Long id, @RequestParam(required = false) String customName) {
        return tenantService.updateCustomName(id, customName);
    }

    @PostMapping("/updateCost")
    @ResponseBody
    public ApiResponse updateCost(@RequestParam Long id, @RequestParam(required = false) BigDecimal cost) {
        return tenantService.updateCost(id, cost);
    }

    @PostMapping("/sync/{id}")
    @ResponseBody
    public ApiResponse syncTenant(@PathVariable Long id) {
        return tenantService.syncTenant(id);
    }

    @PostMapping("/transfer")
    @ResponseBody
    public ApiResponse transferTenant(@RequestParam Long id, @RequestParam(required = false) Long parentId) {
        return tenantService.transferTenant(id, parentId);
    }

    @GetMapping("/checkAllStream")
    public SseEmitter checkAllStream() {
        SseEmitter emitter = new SseEmitter(120000L);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                List<Tenant> tenants = tenantService.getAllTenants();
                for (Tenant tenant : tenants) {
                    ApiResponse response = tenantService.checkAccountStatus(tenant.getId());
                    emitter.send(SseEmitter.event()
                            .name("tenant-status")
                            .data("[" + tenant.getId() + "] " + tenant.getTenancyName() + "：" + response.getMessage()));
                }
                emitter.send(SseEmitter.event().name("complete").data("账号状态检测完成，共 " + tenants.size() + " 个"));
                emitter.complete();
            } catch (Exception e) {
                log.error("批量检测账号状态失败", e);
                emitter.completeWithError(e);
            } finally {
                executor.shutdown();
            }
        });
        return emitter;
    }

    @GetMapping("/auditStream")
    public SseEmitter auditStream() {
        SseEmitter emitter = new SseEmitter(120000L);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                List<Tenant> tenants = tenantService.getAllTenants();
                emitter.send(SseEmitter.event().name("audit").data("开始审计租户配置，共 " + tenants.size() + " 个账号"));
                for (Tenant tenant : tenants) {
                    emitter.send(SseEmitter.event()
                            .name("audit")
                            .data("租户 " + tenant.getId()
                                    + "，云类型=" + tenant.getCloudType()
                                    + "，区域=" + tenant.getRegion()
                                    + "，邮箱 MFA/服务=" + (Boolean.TRUE.equals(tenant.getEmailServiceEnabled()) ? "已启用" : "未启用")
                                    + "，API 同步=" + (Boolean.TRUE.equals(tenant.getApiSynced()) ? "已同步" : "未同步")));
                }
                emitter.send(SseEmitter.event().name("complete").data("审计报告生成完成"));
                emitter.complete();
            } catch (Exception e) {
                log.error("租户审计失败", e);
                emitter.completeWithError(e);
            } finally {
                executor.shutdown();
            }
        });
        return emitter;
    }
}
