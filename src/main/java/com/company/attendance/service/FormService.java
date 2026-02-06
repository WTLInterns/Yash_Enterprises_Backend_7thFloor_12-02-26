package com.company.attendance.service;

import com.company.attendance.dto.FormDto;
import com.company.attendance.entity.Form;
import com.company.attendance.notification.NotificationService;
import com.company.attendance.repository.FormRepository;
import com.company.attendance.repository.ClientRepository;
import com.company.attendance.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FormService {

    private final FormRepository formRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    public List<FormDto> findAll() {
        return formRepository.findAllActiveOrderByCreatedAtDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<FormDto> findByClientId(Long clientId) {
        return formRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<FormDto> findByCreatedBy(Long createdBy) {
        return formRepository.findByCreatedByOrderByCreatedAtDesc(createdBy).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public FormDto findById(Long id) {
        return formRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    public FormDto create(FormDto formDto, Long createdBy) {
        Form form = Form.builder()
                .name(formDto.getName())
                .description(formDto.getDescription())
                .schema(formDto.getSchema())
                .clientId(formDto.getClientId())
                .createdBy(createdBy != null ? createdBy : formDto.getCreatedBy())
                .updatedBy(createdBy != null ? createdBy : formDto.getUpdatedBy())
                .isActive(true)
                .build();

        Form savedForm = formRepository.save(form);

        notifyAdmins("Form created", savedForm.getName() != null ? savedForm.getName() : "A form was created", "FORM_CREATED", savedForm.getId());
        return convertToDto(savedForm);
    }

    public FormDto update(Long id, FormDto formDto, Long updatedBy) {
        Form existingForm = formRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Form not found with id: " + id));

        existingForm.setName(formDto.getName());
        existingForm.setDescription(formDto.getDescription());
        existingForm.setSchema(formDto.getSchema());
        existingForm.setClientId(formDto.getClientId());
        existingForm.setUpdatedBy(updatedBy != null ? updatedBy : formDto.getUpdatedBy());

        Form updatedForm = formRepository.save(existingForm);

        notifyAdmins("Form updated", updatedForm.getName() != null ? updatedForm.getName() : "A form was updated", "FORM_UPDATED", updatedForm.getId());
        return convertToDto(updatedForm);
    }

    private void notifyAdmins(String title, String body, String type, Long formId) {
        Map<String, String> data = Map.of(
                "type", type,
                "formId", String.valueOf(formId)
        );

        employeeRepository.findByRole_NameIgnoreCase("ADMIN").forEach(emp -> {
            notificationService.notifyEmployeeWeb(emp.getId(), title, body, type, "FORM", formId, data);
        });

        employeeRepository.findByRole_NameIgnoreCase("MANAGER").forEach(emp -> {
            notificationService.notifyEmployeeWeb(emp.getId(), title, body, type, "FORM", formId, data);
        });
    }

    public void delete(Long id) {
        Form form = formRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Form not found with id: " + id));
        form.setIsActive(false);
        formRepository.save(form);
    }

    private FormDto convertToDto(Form form) {
        String createdByName = "-";
        String updatedByName = "-";
        String clientName = "-";

        // Get created by name
        if (form.getCreatedBy() != null) {
            // Debug: Check what employee exists for this ID
            final Long createdBy = form.getCreatedBy();
            System.out.println("DEBUG: Looking for employee with ID: " + createdBy);
            createdByName = employeeRepository.findById(createdBy)
                    .map(emp -> {
                        String empName = (emp.getFirstName() + " " + (emp.getLastName() != null ? emp.getLastName() : "")).trim();
                        System.out.println("DEBUG: Found employee=" + emp);
                        System.out.println("DEBUG: firstName=" + emp.getFirstName() + " lastName=" + emp.getLastName() + " -> name=" + empName);
                        return empName;
                    })
                    .orElse("-");
        }

        // Get updated by name
        if (form.getUpdatedBy() != null) {
            final Long updatedBy = form.getUpdatedBy();
            updatedByName = employeeRepository.findById(updatedBy)
                    .map(emp -> {
                        String empName = (emp.getFirstName() + " " + (emp.getLastName() != null ? emp.getLastName() : "")).trim();
                        System.out.println("DEBUG: updatedBy=" + updatedBy + " -> firstName=" + emp.getFirstName() + " lastName=" + emp.getLastName() + " -> name=" + empName);
                        return empName;
                    })
                    .orElse("-");
        }

        // Get client name
        if (form.getClientId() != null) {
            final Long clientId = form.getClientId();
            clientName = clientRepository.findById(clientId)
                    .map(client -> {
                        System.out.println("DEBUG: clientId=" + clientId + " -> clientName=" + client.getName());
                        return client.getName();
                    })
                    .orElse("-");
        }

        return FormDto.builder()
                .id(form.getId())
                .name(form.getName())
                .description(form.getDescription())
                .schema(form.getSchema())
                .clientId(form.getClientId())
                .clientName(clientName)
                .createdBy(form.getCreatedBy())
                .updatedBy(form.getUpdatedBy())
                .createdByName(createdByName)
                .updatedByName(updatedByName)
                .createdAt(form.getCreatedAt())
                .updatedAt(form.getUpdatedAt())
                .isActive(form.getIsActive())
                .build();
    }
}

