package com.company.attendance.crm.service;

import com.company.attendance.crm.entity.Invoice;

public interface InvoicePdfService {
    byte[] generateInvoicePdf(Invoice invoice);
}
