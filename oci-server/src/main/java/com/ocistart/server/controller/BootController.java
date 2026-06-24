package com.ocistart.server.controller;

import com.ocistart.common.param.ApiResponse;
import com.ocistart.dao.entity.BootInstance;
import com.ocistart.dao.entity.Tenant;
import com.ocistart.server.service.BootInstanceService;
import com.ocistart.server.service.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/boot")
public class BootController extends BaseController {

    @Resource
    private BootInstanceService bootInstanceService;

    @Resource
    private TenantService tenantService;

    @GetMapping({"/fullBootList", "/list"})
    public String fullBootList(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) Long tenantId,
                               @RequestParam(required = false) String architecture,
                               Model model) {
        Page<BootInstance> taskPage = bootInstanceService.listBootTasks(
                tenantId,
                architecture,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        );
        List<Tenant> tenants = tenantService.getAllTenants();

        model.addAttribute("tasks", taskPage.getContent());
        model.addAttribute("tenants", tenants);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", taskPage.getTotalPages());
        model.addAttribute("totalElements", taskPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("selectedTenantId", tenantId);
        model.addAttribute("selectedArchitecture", architecture);
        model.addAttribute("pendingCount", bootInstanceService.getPendingCount());
        model.addAttribute("runningCount", bootInstanceService.getRunningCount());
        model.addAttribute("activePage", "boot-list");
        return "boot_list";
    }

    @PostMapping("/save")
    @ResponseBody
    public ApiResponse save(@ModelAttribute BootInstance bootInstance) {
        BootInstance saved = bootInstanceService.updateBootTask(bootInstance);
        return ApiResponse.success("抢机配置已保存", saved);
    }

    @PostMapping("/start")
    @ResponseBody
    public ApiResponse start(@RequestParam Long id) {
        return bootInstanceService.startBootTask(id);
    }

    @PostMapping("/manualBoot")
    @ResponseBody
    public ApiResponse manualBoot(@RequestParam Long id) {
        return bootInstanceService.manualBoot(id);
    }

    @PostMapping("/clone")
    @ResponseBody
    public ApiResponse cloneTask(@RequestParam Long id) {
        return bootInstanceService.cloneBootTask(id);
    }

    @PostMapping("/stop")
    @ResponseBody
    public ApiResponse stop(@RequestParam Long id) {
        return bootInstanceService.stopBootTask(id);
    }

    @PostMapping("/batchStart")
    @ResponseBody
    public ApiResponse batchStart() {
        return bootInstanceService.batchStartPendingTasks();
    }

    @PostMapping("/batchStop")
    @ResponseBody
    public ApiResponse batchStop() {
        return bootInstanceService.batchStopRunningTasks();
    }

    @PostMapping("/resetRetry")
    @ResponseBody
    public ApiResponse resetRetry() {
        return bootInstanceService.resetRetryCount();
    }

    @PostMapping("/delete")
    @ResponseBody
    public ApiResponse delete(@RequestParam Long id) {
        return bootInstanceService.deleteBootTask(id);
    }

    @PostMapping("/deleteGroup")
    @ResponseBody
    public ApiResponse deleteGroup(@RequestParam Long tenantId, @RequestParam String architecture) {
        return bootInstanceService.deleteByTenantAndArchitecture(tenantId, architecture);
    }

    @PostMapping("/toggleStatus")
    @ResponseBody
    public ApiResponse toggleStatus(@RequestParam Long id) {
        return bootInstanceService.toggleStatus(id);
    }

    @GetMapping("/counts")
    @ResponseBody
    public ApiResponse<Map<String, Long>> counts() {
        Map<String, Long> counts = new HashMap<>();
        counts.put("pending", bootInstanceService.getPendingCount());
        counts.put("running", bootInstanceService.getRunningCount());
        return ApiResponse.success(counts);
    }
}
