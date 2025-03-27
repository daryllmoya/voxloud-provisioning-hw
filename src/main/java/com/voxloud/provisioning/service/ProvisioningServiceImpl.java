package com.voxloud.provisioning.service;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voxloud.provisioning.entity.Device;
import com.voxloud.provisioning.entity.DeviceModel;
import com.voxloud.provisioning.repository.DeviceRepository;

@Service
public class ProvisioningServiceImpl implements ProvisioningService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Value("${provisioning.domain}")
    private String defaultDomain;

    @Value("${provisioning.port}")
    private String defaultPort;

    @Value("${provisioning.codecs}")
    private String defaultCodecs;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getProvisioningFile(String macAddress) {
        Optional<Device> deviceOpt = deviceRepository.findById(macAddress);

        if (deviceOpt.isEmpty()) {
            return null;
        }

        Device device = deviceOpt.get();

        Map<String, Object> config = buildDefaultConfig(device);

        // Apply override fragment if present
        if (device.getOverrideFragment() != null && !device.getOverrideFragment().isBlank()) {
            Map<String, Object> overrideMap = parseOverrideFragment(device.getOverrideFragment(), device.getModel());
            config.putAll(overrideMap);
        }

        return formatConfig(config, device.getModel());
    }

    private Map<String, Object> buildDefaultConfig(Device device) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("username", device.getUsername());
        config.put("password", device.getPassword());
        config.put("domain", defaultDomain);
        config.put("port", defaultPort);

        // Store codecs differently depending on model
        // (List for JSON, CSV for properties)
        if (device.getModel() == DeviceModel.CONFERENCE) {
            config.put("codecs", Arrays.asList(defaultCodecs.split(",")));
        } else {
            config.put("codecs", defaultCodecs);
        }

        return config;
    }

    private Map<String, Object> parseOverrideFragment(String fragment, DeviceModel model) {
        Map<String, Object> overrideMap = new HashMap<>();

        try {
            if (model == DeviceModel.CONFERENCE) {
                // JSON override
                overrideMap = objectMapper.readValue(fragment,
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                        });
            } else {
                // .properties style override
                Properties props = new Properties();
                props.load(new StringReader(fragment));
                for (String key : props.stringPropertyNames()) {
                    overrideMap.put(key, props.getProperty(key));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse override fragment", e);
        }

        return overrideMap;
    }

    private String formatConfig(Map<String, Object> config, DeviceModel model) {
        if (model == DeviceModel.CONFERENCE) {
            try {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
            } catch (IOException e) {
                throw new RuntimeException("Failed to format JSON config", e);
            }
        } else {
            return config.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("\n"));
        }
    }
}
