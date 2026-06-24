package com.ocistart.server.utils;

import com.oracle.bmc.core.VirtualNetworkClient;
import com.oracle.bmc.core.model.*;
import com.oracle.bmc.core.requests.*;
import com.oracle.bmc.core.responses.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class OciCliUtils {

    /**
     * 在指定区间中创建或查找 VCN。
     */
    public static List<Vcn> createVcn(VirtualNetworkClient client, String compartmentId) throws Exception {
        ListVcnsRequest listRequest = ListVcnsRequest.builder()
                .compartmentId(compartmentId)
                .build();
        ListVcnsResponse listResponse = client.listVcns(listRequest);
        List<Vcn> existingVcns = listResponse.getItems();

        if (existingVcns != null && !existingVcns.isEmpty()) {
            log.info("找到 {} 个已有 VCN", existingVcns.size());
            return existingVcns;
        }

        CreateVcnDetails details = CreateVcnDetails.builder()
                .cidrBlock("10.0.0.0/16")
                .compartmentId(compartmentId)
                .displayName("oci-start-vcn")
                .dnsLabel("ocistart")
                .build();
        CreateVcnRequest createRequest = CreateVcnRequest.builder()
                .createVcnDetails(details)
                .build();
        CreateVcnResponse createResponse = client.createVcn(createRequest);
        Vcn vcn = client.getWaiters()
                .forVcn(GetVcnRequest.builder().vcnId(createResponse.getVcn().getId()).build(),
                        Vcn.LifecycleState.Available)
                .execute().getVcn();
        log.info("已创建 VCN：{}", vcn.getId());
        return Collections.singletonList(vcn);
    }

    /**
     * 在 VCN 和可用性域中创建子网。
     */
    public static Subnet createSubnet(VirtualNetworkClient client, String compartmentId,
                                       String availabilityDomain, String cidrBlock, Vcn vcn) throws Exception {
        ListSubnetsRequest listRequest = ListSubnetsRequest.builder()
                .compartmentId(compartmentId)
                .vcnId(vcn.getId())
                .build();
        ListSubnetsResponse listResponse = client.listSubnets(listRequest);
        List<Subnet> existingSubnets = listResponse.getItems();

        if (existingSubnets != null && !existingSubnets.isEmpty()) {
            log.info("找到 {} 个已有子网", existingSubnets.size());
            return existingSubnets.get(0);
        }

        CreateSubnetDetails details = CreateSubnetDetails.builder()
                .availabilityDomain(availabilityDomain)
                .compartmentId(compartmentId)
                .vcnId(vcn.getId())
                .cidrBlock("10.0.1.0/24")
                .displayName("oci-start-subnet")
                .dnsLabel("ocisubnet")
                .build();
        CreateSubnetRequest createRequest = CreateSubnetRequest.builder()
                .createSubnetDetails(details)
                .build();
        CreateSubnetResponse createResponse = client.createSubnet(createRequest);
        Subnet subnet = client.getWaiters()
                .forSubnet(GetSubnetRequest.builder().subnetId(createResponse.getSubnet().getId()).build(),
                        Subnet.LifecycleState.Available)
                .execute().getSubnet();
        log.info("已创建子网：{}", subnet.getId());
        return subnet;
    }
}
