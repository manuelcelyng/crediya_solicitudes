package co.com.pragma.crediya.shared.mappers;

import co.com.pragma.crediya.model.solicitud.Email;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface EmailMapper {

    @Named("toEmail")
    default Email toEmail(String email) {return new Email(email);}

    @Named("fromEmail")
    default String fromEmail(Email email) { return email == null ? null : email.email(); }

}
