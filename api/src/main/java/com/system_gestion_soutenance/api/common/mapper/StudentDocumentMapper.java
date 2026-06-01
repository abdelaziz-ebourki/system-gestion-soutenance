package com.system_gestion_soutenance.api.common.mapper;

import com.system_gestion_soutenance.api.student.document.entity.StudentDocument;
import com.system_gestion_soutenance.api.student.document.dto.StudentDocumentDto;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public interface StudentDocumentMapper {
	StudentDocumentDto toDto(StudentDocument document);
}
