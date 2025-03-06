package ec.grace.loginqr.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "usuarios")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {

    @Id
    private String id;
    private String cedula;
    private String nombre;
    private String apellido;
    private String correo;
    private Boolean estado;
    private Integer intentos;

}
