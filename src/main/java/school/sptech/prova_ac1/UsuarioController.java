package school.sptech.prova_ac1;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository repository;

    public UsuarioController(UsuarioRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<Usuario> criar(@RequestBody Usuario usuario) {
        if (repository.findByEmail(usuario.getEmail()).isPresent() ||
                repository.findByCpf(usuario.getCpf()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Usuario salvo = repository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> buscarTodos() {
        List<Usuario> usuarios = repository.findAll();
        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Integer id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filtro-data")
    public ResponseEntity<List<Usuario>> buscarPorDataNascimento(
            @RequestParam("nascimento") String nascimento) {

        LocalDate dataFiltro = LocalDate.parse(nascimento);
        List<Usuario> usuarios = repository.findAll().stream()
                .filter(u -> u.getDataNascimento().isAfter(dataFiltro))
                .collect(Collectors.toList());

        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(usuarios);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizar(
            @PathVariable Integer id,
            @RequestBody Usuario usuario
    ) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        Optional<Usuario> emailExistente = repository.findByEmail(usuario.getEmail());
        if (emailExistente.isPresent() && !emailExistente.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Optional<Usuario> cpfExistente = repository.findByCpf(usuario.getCpf());
        if (cpfExistente.isPresent() && !cpfExistente.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        usuario.setId(id);
        Usuario atualizado = repository.save(usuario);
        return ResponseEntity.ok(atualizado);
    }
}
