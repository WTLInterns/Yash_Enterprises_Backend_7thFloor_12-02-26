package com.company.attendance.mapper;

import com.company.attendance.dto.ExpenseDto;
import com.company.attendance.entity.Expense;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:04+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class ExpenseMapperImpl implements ExpenseMapper {

    @Override
    public ExpenseDto toDto(Expense o) {
        if ( o == null ) {
            return null;
        }

        ExpenseDto expenseDto = new ExpenseDto();

        expenseDto.setAmount( o.getAmount() );
        expenseDto.setApprovedBy( o.getApprovedBy() );
        expenseDto.setCategory( o.getCategory() );
        expenseDto.setEmployeeId( o.getEmployeeId() );
        expenseDto.setId( o.getId() );
        expenseDto.setReceiptUrl( o.getReceiptUrl() );
        expenseDto.setStatus( o.getStatus() );

        return expenseDto;
    }

    @Override
    public Expense toEntity(ExpenseDto dto) {
        if ( dto == null ) {
            return null;
        }

        Expense.ExpenseBuilder expense = Expense.builder();

        expense.amount( dto.getAmount() );
        expense.approvedBy( dto.getApprovedBy() );
        expense.category( dto.getCategory() );
        expense.employeeId( dto.getEmployeeId() );
        expense.id( dto.getId() );
        expense.receiptUrl( dto.getReceiptUrl() );
        expense.status( dto.getStatus() );

        return expense.build();
    }
}
