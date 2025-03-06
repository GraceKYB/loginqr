package ec.grace.loginqr.repository;

import ec.grace.loginqr.entity.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Usuario findByCorreo(String email);
}
