package com.company.attendance.mapper;
import com.company.attendance.dto.ExpenseDto;
import com.company.attendance.entity.Expense;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {
    
    ExpenseDto toDto(Expense expense);
    
    Expense toEntity(ExpenseDto dto);
}

