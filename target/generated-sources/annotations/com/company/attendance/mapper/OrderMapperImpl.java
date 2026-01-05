package com.company.attendance.mapper;

import com.company.attendance.dto.OrderDto;
import com.company.attendance.entity.Order;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:04+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public OrderDto toDto(Order o) {
        if ( o == null ) {
            return null;
        }

        OrderDto orderDto = new OrderDto();

        orderDto.setAmount( o.getAmount() );
        orderDto.setClientId( o.getClientId() );
        orderDto.setCreatedBy( o.getCreatedBy() );
        orderDto.setId( o.getId() );
        orderDto.setItems( o.getItems() );
        orderDto.setOrderNumber( o.getOrderNumber() );
        orderDto.setStatus( o.getStatus() );

        return orderDto;
    }

    @Override
    public Order toEntity(OrderDto dto) {
        if ( dto == null ) {
            return null;
        }

        Order.OrderBuilder order = Order.builder();

        order.amount( dto.getAmount() );
        order.clientId( dto.getClientId() );
        order.createdBy( dto.getCreatedBy() );
        order.id( dto.getId() );
        order.items( dto.getItems() );
        order.orderNumber( dto.getOrderNumber() );
        order.status( dto.getStatus() );

        return order.build();
    }
}
