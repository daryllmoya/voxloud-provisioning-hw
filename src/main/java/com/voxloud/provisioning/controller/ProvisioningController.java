package com.voxloud.provisioning.controller;

import com.voxloud.provisioning.service.ProvisioningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ProvisioningController {

    @Autowired
    private ProvisioningService provisioningService;

    @GetMapping("/provisioning/{macAddress}")
    public ResponseEntity<String> getProvisioningFile(@PathVariable String macAddress) {
        String config = provisioningService.getProvisioningFile(macAddress);

        if (config == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Device not found");
        }

        MediaType contentType = config.trim().startsWith("{")
                ? MediaType.APPLICATION_JSON
                : MediaType.valueOf("text/plain");

        return ResponseEntity.ok()
                .contentType(contentType)
                .body(config);
    }
}
