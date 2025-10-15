package com.application.javai;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsuarioController {
    private static final List<Usuario> usuarios = new ArrayList<>();

    static {
        // Exemplo de usuários para teste
        usuarios.add(new Usuario(1, "João", "joao@email.com"));
        usuarios.add(new Usuario(2, "Maria", "maria@email.com"));
    }
    //*"Burlando" o Spring Security */
    //Para rodar no cmd prompt do windows.
    //ao rodar o .\gradlew bootRun vai vir gerada uma senha padrão.
    //pesquise por Using generated security password:
    //pois aparece no terminal como Using generated security password: <senha-gerada>
    // rode com curl -u user:<senha-gerada> http://localhost:8080/users 

    @GetMapping("/users")
    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    @GetMapping("/user/{uid}")
    public Usuario getUsuario(@PathVariable int uid) {
        return usuarios.stream()
                .filter(u -> u.getId() == uid)
                .findFirst()
                .orElse(null);
    }
}
