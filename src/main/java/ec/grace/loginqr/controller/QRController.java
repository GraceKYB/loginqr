package ec.grace.loginqr.controller;

import ec.grace.loginqr.entity.Usuario;
import ec.grace.loginqr.repository.UsuarioRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;

@Controller
@RequestMapping("/login")
public class QRController {

    // Mapa para almacenar tokens generados y su estado
    private static final ConcurrentHashMap<String, Boolean> tokenStatus = new ConcurrentHashMap<>();

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/")
    public String showQRCodePage(Model model) {
        String token = UUID.randomUUID().toString();
        tokenStatus.put(token, false);
        String qrText = "auth?token=" + token;
        model.addAttribute("qrText", qrText);
        model.addAttribute("token", token);
        return "qrpage";
    }


    @GetMapping("/generateQR")
    public ResponseEntity<ByteArrayResource> generateQRCode(@RequestParam String url) throws IOException, WriterException {
        int width = 300, height = 300;

        BitMatrix bitMatrix = new MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, width, height);
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "png", baos);
        ByteArrayResource resource = new ByteArrayResource(baos.toByteArray());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @PostMapping("/scan")
    public ResponseEntity<String> procesarEscaneo(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        String email = payload.get("email");

        // Buscar el usuario en la base de datos usando el email
        Usuario usuario = usuarioRepository.findByCorreo(email);

        if (usuario == null) {
            // Si el usuario no existe, retorna un mensaje de "Usuario no registrado"
            return ResponseEntity.badRequest().body("Usuario no registrado");
        }
        // Verificar si el usuario está bloqueado
        if (!usuario.getEstado()) {
            return ResponseEntity.badRequest().body( "Usuario bloqueado por intentos fallidos");
        }

        if (token == null || tokenStatus.containsKey(token)) {
            boolean tokeUsado = tokenStatus.get(token);
            if(tokeUsado){
                // Si el token no es válido o ha expirado, verificar los intentos
                return validarIntentosToken(usuario);
            }

        }

        if (!tokenStatus.containsKey(token)) {
            return validarIntentosToken(usuario);
        }

        assert token != null;
        tokenStatus.put(token, true);

        // Actualizar el número de intentos a 0 y marcar al usuario como autenticado
        usuario.setIntentos(0);
        usuarioRepository.save(usuario); // Reiniciar intentos en la base de datos

        // Redirigir a la página de bienvenida
        return ResponseEntity.ok("INICIANDO SESIÓN........");
    }


    private ResponseEntity<String> validarIntentosToken(Usuario usuario) {
        int intentos = usuario.getIntentos()==null?0: usuario.getIntentos();

        if (intentos >= 3) {
            // Si los intentos son 3 o más, bloquear al usuario
            usuario.setEstado(false);
            usuarioRepository.save(usuario); // Actualizar estado en la base de datos
            return ResponseEntity.badRequest().body("Usuario bloqueado por 3 intentos fallidos");
        } else {
            // Si los intentos son menores a 3, incrementar el contador de intentos
            usuario.setIntentos(intentos + 1);
            usuarioRepository.save(usuario); // Actualizar intentos en la base de datos
            return ResponseEntity.badRequest().body("Intentos fallidos: " + (intentos + 1) + "/3");
        }
    }


    @GetMapping("/check-token-redirect")
    public RedirectView checkTokenRedirect(@RequestParam String token) {
        Boolean isScanned = tokenStatus.getOrDefault(token, false);

        if (Boolean.TRUE.equals(isScanned)) {
            return new RedirectView("/loginqr/login/bienvenido");
        }

        return new RedirectView("/loginqr/login/");
    }

    @GetMapping("/bienvenido")
    public String welcomePage() {
        return "bienvenido";
    }


}