package com.company.attendance.mapper;

import com.company.attendance.dto.TaskDto;
import com.company.attendance.entity.Task;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:05+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class TaskMapperImpl implements TaskMapper {

    @Override
    public TaskDto toDto(Task task) {
        if ( task == null ) {
            return null;
        }

        TaskDto taskDto = new TaskDto();

        taskDto.setAssignedBy( task.getAssignedBy() );
        taskDto.setAssignedTo( task.getAssignedTo() );
        taskDto.setCreatedAt( task.getCreatedAt() );
        taskDto.setDescription( task.getDescription() );
        taskDto.setDueDate( task.getDueDate() );
        taskDto.setId( task.getId() );
        taskDto.setPriority( task.getPriority() );
        taskDto.setStatus( task.getStatus() );
        taskDto.setTitle( task.getTitle() );

        return taskDto;
    }

    @Override
    public Task toEntity(TaskDto dto) {
        if ( dto == null ) {
            return null;
        }

        Task.TaskBuilder task = Task.builder();

        task.assignedBy( dto.getAssignedBy() );
        task.assignedTo( dto.getAssignedTo() );
        task.createdAt( dto.getCreatedAt() );
        task.description( dto.getDescription() );
        task.dueDate( dto.getDueDate() );
        task.id( dto.getId() );
        task.priority( dto.getPriority() );
        task.status( dto.getStatus() );
        task.title( dto.getTitle() );

        return task.build();
    }
}
