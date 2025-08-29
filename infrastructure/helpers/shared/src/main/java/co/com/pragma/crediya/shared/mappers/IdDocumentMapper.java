package co.com.pragma.crediya.shared.mappers;

import co.com.pragma.crediya.model.solicitud.IdDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface IdDocumentMapper {
    @Named("toIdDocument")
    default IdDocument toIdDocument(String document) {return new IdDocument(document);}

    @Named("fromIdDocument")
    default String fromEmail(IdDocument document) { return document == null ? null : document.documento(); }

}
