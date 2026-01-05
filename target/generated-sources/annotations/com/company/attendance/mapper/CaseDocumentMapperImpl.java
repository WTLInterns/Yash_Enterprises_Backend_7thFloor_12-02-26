package com.company.attendance.mapper;

import com.company.attendance.dto.CaseDocumentDto;
import com.company.attendance.entity.CaseDocument;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-05T13:29:04+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class CaseDocumentMapperImpl implements CaseDocumentMapper {

    @Override
    public CaseDocumentDto toDto(CaseDocument document) {
        if ( document == null ) {
            return null;
        }

        CaseDocumentDto caseDocumentDto = new CaseDocumentDto();

        caseDocumentDto.setCreatedAt( document.getCreatedAt() );
        caseDocumentDto.setFilePath( document.getFilePath() );
        caseDocumentDto.setFileSize( document.getFileSize() );
        caseDocumentDto.setFileType( document.getFileType() );
        caseDocumentDto.setUpdatedAt( document.getUpdatedAt() );
        caseDocumentDto.setId( document.getId() );
        caseDocumentDto.setDocumentName( document.getDocumentName() );
        caseDocumentDto.setDescription( document.getDescription() );
        caseDocumentDto.setFileName( document.getFileName() );

        return caseDocumentDto;
    }

    @Override
    public CaseDocument toEntity(CaseDocumentDto documentDto) {
        if ( documentDto == null ) {
            return null;
        }

        CaseDocument.CaseDocumentBuilder caseDocument = CaseDocument.builder();

        caseDocument.createdAt( documentDto.getCreatedAt() );
        caseDocument.description( documentDto.getDescription() );
        caseDocument.documentName( documentDto.getDocumentName() );
        caseDocument.fileName( documentDto.getFileName() );
        caseDocument.filePath( documentDto.getFilePath() );
        caseDocument.fileSize( documentDto.getFileSize() );
        caseDocument.fileType( documentDto.getFileType() );
        caseDocument.id( documentDto.getId() );
        caseDocument.updatedAt( documentDto.getUpdatedAt() );

        return caseDocument.build();
    }

    @Override
    public void updateEntityFromDto(CaseDocumentDto documentDto, CaseDocument document) {
        if ( documentDto == null ) {
            return;
        }

        document.setCreatedAt( documentDto.getCreatedAt() );
        document.setDescription( documentDto.getDescription() );
        document.setDocumentName( documentDto.getDocumentName() );
        document.setId( documentDto.getId() );
        document.setUpdatedAt( documentDto.getUpdatedAt() );
        document.setFileName( documentDto.getFileName() );
        document.setFilePath( documentDto.getFilePath() );
        document.setFileType( documentDto.getFileType() );
        document.setFileSize( documentDto.getFileSize() );
    }

    @Override
    public List<CaseDocumentDto> toDtoList(List<CaseDocument> documents) {
        if ( documents == null ) {
            return null;
        }

        List<CaseDocumentDto> list = new ArrayList<CaseDocumentDto>( documents.size() );
        for ( CaseDocument caseDocument : documents ) {
            list.add( toDto( caseDocument ) );
        }

        return list;
    }

    @Override
    public List<CaseDocument> toEntityList(List<CaseDocumentDto> documentDtos) {
        if ( documentDtos == null ) {
            return null;
        }

        List<CaseDocument> list = new ArrayList<CaseDocument>( documentDtos.size() );
        for ( CaseDocumentDto caseDocumentDto : documentDtos ) {
            list.add( toEntity( caseDocumentDto ) );
        }

        return list;
    }
}
