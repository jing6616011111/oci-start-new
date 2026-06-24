package com.ocistart.server.controller;

import com.ocistart.dao.entity.InstanceDetails;
import com.ocistart.dao.entity.Tenant;
import com.ocistart.dao.repository.TenantRepository;
import com.ocistart.server.service.oracle.OracleInstanceService;
import com.ocistart.server.service.oracle.OracleCloudService;
import com.ocistart.server.pojo.response.InstanceDetailsRes;
import com.ocistart.server.pojo.request.UpdateRemarkRequest;
import com.ocistart.common.param.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/oci")
public class OciController extends BaseController {

    @Resource
    private OracleInstanceService oracleInstanceService;

    @Resource
    private OracleCloudService oracleCloudService;

    @Resource
    private TenantRepository tenantRepository;

    @GetMapping("/list")
    public String listInstances(@RequestParam(defaultValue = "10") int size,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(required = false) String tenantId,
                                Model model) {
        Page<InstanceDetailsRes> instancePage = oracleInstanceService.getAllInstances(page, size, tenantId);
        List<Tenant> tenants = tenantRepository.findAll();

        model.addAttribute("instances", instancePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", instancePage.getTotalPages());
        model.addAttribute("totalElements", instancePage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("activePage", "oci-list");
        model.addAttribute("tenants", tenants);
        model.addAttribute("selectedTenantId", tenantId);
        return "oci_list";
    }

    @PostMapping("/start")
    @ResponseBody
    public ApiResponse startInstance(@RequestParam String instanceId) {
        return oracleInstanceService.startInstance(instanceId);
    }

    @PostMapping("/stop")
    @ResponseBody
    public ApiResponse stopInstance(@RequestParam String instanceId) {
        return oracleInstanceService.stopInstance(instanceId);
    }

    @PostMapping("/kill")
    @ResponseBody
    public ApiResponse killInstance(@RequestParam Long id) {
        return oracleInstanceService.killInstance(id);
    }

    @PostMapping("/changeIp")
    @ResponseBody
    public ApiResponse changeIp(@RequestParam Long instanceId) {
        return oracleInstanceService.changePublicIp(instanceId);
    }

    @PostMapping("/updateRemark")
    @ResponseBody
    public ApiResponse updateRemark(@RequestBody UpdateRemarkRequest request) {
        oracleInstanceService.updateRemark(request.getInstanceId(), request.getRemark());
        return ApiResponse.success("备注已更新");
    }

    @PostMapping("/delete")
    @ResponseBody
    public ApiResponse deleteInstance(@RequestParam Long id) {
        return oracleInstanceService.deleteInstanceRecord(id);
    }

    @GetMapping("/detail/{id}")
    public String instanceDetail(@PathVariable Long id, Model model) {
        InstanceDetails inst = oracleInstanceService.getInstanceById(id);
        model.addAttribute("instance", inst);
        model.addAttribute("activePage", "oci-list");
        return "oci_detail";
    }
}
