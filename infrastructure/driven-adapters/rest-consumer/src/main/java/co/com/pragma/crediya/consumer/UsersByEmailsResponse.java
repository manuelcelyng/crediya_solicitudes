package co.com.pragma.crediya.consumer;

import co.com.pragma.crediya.model.page.usuarios.SolicitudUsersFieldsPage;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UsersByEmailsResponse {
    List<SolicitudUsersFieldsPage> results;
}
