package com.ocistart.server.controller;

import com.ocistart.server.service.oracle.OracleInstanceService;
import com.ocistart.server.pojo.response.InstanceDetailsRes;
import com.ocistart.common.param.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@Controller
@RequestMapping("/vps/instances")
public class VpsController extends BaseController {

    @Resource
    private OracleInstanceService oracleInstanceService;

    @GetMapping("/list")
    public String listInstances(@RequestParam(defaultValue = "1000") int size,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(required = false) String tenantId,
                                Model model) {
        Page<InstanceDetailsRes> instancePage = oracleInstanceService.getAllInstances(page, size, tenantId);
        model.addAttribute("instances", instancePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", instancePage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("activePage", "vps-list");
        return "vps_list";
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

    @PostMapping("/restart")
    @ResponseBody
    public ApiResponse restartInstance(@RequestParam String instanceId) {
        oracleInstanceService.stopInstance(instanceId);
        return oracleInstanceService.startInstance(instanceId);
    }
}
