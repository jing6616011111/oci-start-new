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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/pageData")
    @ResponseBody
    public ApiResponse<Page<InstanceDetailsRes>> pageData(@RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(required = false) String tenantId) {
        return ApiResponse.success(oracleInstanceService.getAllInstances(page, size, tenantId));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportInstances(@RequestParam(required = false) String tenantId) {
        Page<InstanceDetailsRes> instancePage = oracleInstanceService.getAllInstances(0, Integer.MAX_VALUE, tenantId);
        String header = "ID,实例ID,名称,状态,规格,CPU,内存GB,磁盘GB,公网IP,内网IP,SSH用户,端口,Root密码,备注\n";
        String rows = instancePage.getContent().stream()
                .map(item -> String.join(",",
                        csv(item.getId()),
                        csv(item.getInstanceId()),
                        csv(item.getDisplayName()),
                        csv(item.getState()),
                        csv(item.getShape()),
                        csv(item.getOcpus()),
                        csv(item.getMemoryInGBs()),
                        csv(item.getBootVolumeSizeInGBs()),
                        csv(item.getPublicIps()),
                        csv(item.getPrivateIps()),
                        csv(item.getUsername()),
                        csv("22"),
                        csv(item.getPassword()),
                        csv(item.getRemark())
                ))
                .collect(Collectors.joining("\n"));
        byte[] body = ("\uFEFF" + header + rows).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=oci-instances.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @PostMapping({"/start", "/startInstance"})
    @ResponseBody
    public ApiResponse startInstance(@RequestParam String instanceId) {
        return oracleInstanceService.startInstance(instanceId);
    }

    @PostMapping({"/stop", "/stopInstance"})
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

    private String csv(Object value) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + text + "\"";
    }
}
