package com.company.attendance.crm.mapper;

import com.company.attendance.crm.entity.Invoice;
import com.company.attendance.crm.entity.InvoiceItem;
import com.company.attendance.crm.dto.InvoiceDto;
import com.company.attendance.crm.dto.InvoiceItemDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class InvoiceMapper {
    
    public InvoiceDto toInvoiceDto(Invoice invoice) {
        if (invoice == null) return null;
        
        return InvoiceDto.builder()
                .id(invoice.getId())
                .invoiceNo(invoice.getInvoiceNo())
                .invoiceDate(invoice.getInvoiceDate())
                .dueDate(invoice.getDueDate())
                .isProForma(invoice.getIsProForma())
                .includeGst(invoice.getIncludeGst())
                .billedByName(invoice.getBilledByName())
                .billedByAddress(invoice.getBilledByAddress())
                .billedByEmail(invoice.getBilledByEmail())
                .gstin(invoice.getGstin())
                .pan(invoice.getPan())
                .billedToName(invoice.getBilledToName())
                .billedToAddress(invoice.getBilledToAddress())
                .billedToGstin(invoice.getBilledToGstin())
                .billedToMobile(invoice.getBilledToMobile())
                .billedToEmail(invoice.getBilledToEmail())
                .accountName(invoice.getAccountName())
                .accountNumber(invoice.getAccountNumber())
                .ifsc(invoice.getIfsc())
                .accountType(invoice.getAccountType())
                .bank(invoice.getBank())
                .upiId(invoice.getUpiId())
                .terms(invoice.getTerms())
                .companyLogo(invoice.getCompanyLogo())
                .signature(invoice.getSignature())
                .subtotal(invoice.getSubtotal())
                .cgst(invoice.getCgst())
                .sgst(invoice.getSgst())
                .grandTotal(invoice.getGrandTotal())
                .amountInWords(invoice.getAmountInWords())
                .status(invoice.getStatus())
                .items(invoice.getItems() != null ? 
                    invoice.getItems().stream()
                        .map(this::toInvoiceItemDto)
                        .collect(Collectors.toList()) : null)
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .createdBy(invoice.getCreatedBy())
                .updatedBy(invoice.getUpdatedBy())
                .build();
    }
    
    public InvoiceItemDto toInvoiceItemDto(InvoiceItem item) {
        if (item == null) return null;
        
        return InvoiceItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .rate(item.getRate())
                .qty(item.getQty())
                .amount(item.getAmount())
                .cgst(item.getCgst())
                .sgst(item.getSgst())
                .total(item.getTotal())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
    
    public InvoiceItem toInvoiceItemEntity(InvoiceItemDto dto) {
        if (dto == null) return null;
        
        InvoiceItem item = new InvoiceItem();
        item.setId(dto.getId());
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setRate(dto.getRate());
        item.setQty(dto.getQty());
        item.setAmount(dto.getAmount());
        item.setCgst(dto.getCgst());
        item.setSgst(dto.getSgst());
        item.setTotal(dto.getTotal());
        item.setCreatedAt(dto.getCreatedAt());
        item.setUpdatedAt(dto.getUpdatedAt());
        
        return item;
    }
    
    public Invoice toInvoiceEntity(InvoiceDto dto) {
        if (dto == null) return null;
        
        Invoice invoice = new Invoice();
        invoice.setId(dto.getId());
        invoice.setInvoiceNo(dto.getInvoiceNo());
        invoice.setInvoiceDate(dto.getInvoiceDate());
        invoice.setDueDate(dto.getDueDate());
        invoice.setIsProForma(dto.getIsProForma());
        invoice.setIncludeGst(dto.getIncludeGst());
        invoice.setBilledByName(dto.getBilledByName());
        invoice.setBilledByAddress(dto.getBilledByAddress());
        invoice.setBilledByEmail(dto.getBilledByEmail());
        invoice.setGstin(dto.getGstin());
        invoice.setPan(dto.getPan());
        invoice.setBilledToName(dto.getBilledToName());
        invoice.setBilledToAddress(dto.getBilledToAddress());
        invoice.setBilledToGstin(dto.getBilledToGstin());
        invoice.setBilledToMobile(dto.getBilledToMobile());
        invoice.setBilledToEmail(dto.getBilledToEmail());
        invoice.setAccountName(dto.getAccountName());
        invoice.setAccountNumber(dto.getAccountNumber());
        invoice.setIfsc(dto.getIfsc());
        invoice.setAccountType(dto.getAccountType());
        invoice.setBank(dto.getBank());
        invoice.setUpiId(dto.getUpiId());
        invoice.setTerms(dto.getTerms());
        invoice.setCompanyLogo(dto.getCompanyLogo());
        invoice.setSignature(dto.getSignature());
        invoice.setSubtotal(dto.getSubtotal());
        invoice.setCgst(dto.getCgst());
        invoice.setSgst(dto.getSgst());
        invoice.setGrandTotal(dto.getGrandTotal());
        invoice.setAmountInWords(dto.getAmountInWords());
        invoice.setStatus(dto.getStatus());
        invoice.setCreatedBy(dto.getCreatedBy());
        invoice.setUpdatedBy(dto.getUpdatedBy());
        
        return invoice;
    }
}
