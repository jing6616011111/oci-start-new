# OCI Start Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first usable slice of the requested OCI Start feature set: enhanced tenant management, baseline boot-task management, instance API aliases, and deployable install assets.

**Architecture:** Keep the current Spring Boot MVC + Thymeleaf structure. Add only local state management and safe API facades in this phase; high-risk real OCI mutations such as deleting VNICs, terminating cloud resources with verification codes, SSH/VNC/DD, MFA, notifications, and load balancer rewiring remain separate phases.

**Tech Stack:** Java 8 target, Spring Boot 2.7.6, Spring MVC, Spring Data JPA, Thymeleaf, H2, Maven.

---

### Task 1: Tenant Management Baseline

**Files:**
- Modify: `oci-dao/src/main/java/com/ocistart/dao/entity/Tenant.java`
- Modify: `oci-dao/src/main/java/com/ocistart/dao/repository/TenantRepository.java`
- Modify: `oci-server/src/main/java/com/ocistart/server/service/TenantService.java`
- Modify: `oci-server/src/main/java/com/ocistart/server/service/impl/TenantServiceImpl.java`
- Modify: `oci-server/src/main/java/com/ocistart/server/controller/TenantController.java`
- Modify: `oci-server/src/main/resources/templates/tenant_list.html`
- Modify: `oci-server/src/main/resources/templates/tenant_form.html`
- Modify: `oci-server/src/main/resources/static/js/app.js`
- Test: `oci-server/src/test/java/com/ocistart/server/service/TenantServiceImplTest.java`

- [x] Add tenant fields for `cloudType`, `emailServiceEnabled`, `customName`, `cost`, `status`, `lastCheckTime`.
- [x] Add pageable/search/filter query support.
- [x] Support multipart private-key upload on tenant save.
- [x] Add tenant option JSON endpoint.
- [x] Add custom-name and cost update endpoints.
- [x] Add SSE endpoints for batch account check and audit report.

### Task 2: Boot Task Management Baseline

**Files:**
- Modify: `oci-dao/src/main/java/com/ocistart/dao/entity/BootInstance.java`
- Modify: `oci-dao/src/main/java/com/ocistart/dao/repository/BootInstanceRepository.java`
- Modify: `oci-server/src/main/java/com/ocistart/server/service/BootInstanceService.java`
- Modify: `oci-server/src/main/java/com/ocistart/server/service/impl/BootInstanceServiceImpl.java`
- Create: `oci-server/src/main/java/com/ocistart/server/controller/BootController.java`
- Create: `oci-server/src/main/resources/templates/boot_list.html`
- Modify: `oci-server/src/main/resources/templates/fragments/sidebar.html`
- Test: `oci-server/src/test/java/com/ocistart/server/service/BootInstanceServiceImplTest.java`

- [x] Add list page for boot configurations at `/boot/fullBootList`.
- [x] Add create/update, clone, manual boot, start, stop, batch start, batch stop, reset retry count, delete, and status count endpoints.
- [x] Keep actual OCI creation asynchronous placeholder behavior for now; real continuous OCI retry loop is a later phase.

### Task 3: Instance API Compatibility

**Files:**
- Modify: `oci-server/src/main/java/com/ocistart/server/controller/OciController.java`
- Modify: `oci-server/src/main/java/com/ocistart/server/service/oracle/impl/OracleInstanceServiceImpl.java`

- [x] Add aliases `/oci/startInstance`, `/oci/stopInstance`, `/oci/pageData`, and `/oci/export`.
- [x] Keep destructive cloud actions as local-state placeholders unless already implemented safely.

### Task 4: Verification and Release

**Files:**
- Modify: `README.md`
- Run: `mvn test`
- Run: `scripts/build-linux-package.sh`
- Run browser smoke checks against `http://localhost:9856`.
- Push to GitHub and publish a new release package.

- [ ] Run tests and compile.
- [ ] Validate main pages in browser.
- [ ] Push source and release package.
