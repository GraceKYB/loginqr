package ec.grace.loginqr.controller;

import ec.grace.loginqr.entity.Usuario;
import ec.grace.loginqr.service.UsuarioService;
import ec.grace.loginqr.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UsuarioController {

    public String ipWS = Constant.ipWS;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarUsuarios());
        model.addAttribute("usuario", new Usuario());
        return "usuarios";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario) {
        if(usuario.getId()!=null && usuario.getId().isEmpty()){
            usuario.setId(null);
            usuario.setEstado(true);
            usuario.setIntentos(0);
        }

        if(usuario.getEstado() &&usuario.getIntentos()!=null &&  usuario.getIntentos()>=3){
            usuario.setIntentos(0);
        }

        usuarioService.guardarUsuario(usuario);
        return "redirect:/user/usuarios";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable String id) {
        usuarioService.eliminarUsuario(id);
        return "redirect:/user/usuarios";
    }

    @GetMapping("/editar/{id}")
    @ResponseBody
    public Usuario editarUsuario(@PathVariable String id) {
        Optional<Usuario> usuario = usuarioService.buscarUsuarioPorId(id);
        return usuario.orElse(null);
    }


}
