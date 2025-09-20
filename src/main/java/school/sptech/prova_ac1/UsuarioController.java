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

    // injeção de dependência via construtor
    public UsuarioController(UsuarioRepository repository) {
        this.repository = repository;
    }

    // 1. Criar usuário
    @PostMapping
    public ResponseEntity<Usuario> criar(@RequestBody Usuario usuario) {
        if (repository.findByEmail(usuario.getEmail()).isPresent() ||
                repository.findByCpf(usuario.getCpf()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409
        }
        Usuario salvo = repository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo); // 201
    }

    // 2. Buscar todos
    @GetMapping
    public ResponseEntity<List<Usuario>> buscarTodos() {
        List<Usuario> usuarios = repository.findAll();
        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }
        return ResponseEntity.ok(usuarios); // 200
    }

    // 3. Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Integer id) {
        return repository.findById(id)
                .map(ResponseEntity::ok) // 200
                .orElse(ResponseEntity.notFound().build()); // 404
    }

    // 4. Deletar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build(); // 404
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build(); // 204
    }

    // 5. Buscar por data de nascimento maior que
    @GetMapping("/filtro-data")
    public ResponseEntity<List<Usuario>> buscarPorDataNascimento(
            @RequestParam("nascimento") String nascimento) {

        LocalDate dataFiltro = LocalDate.parse(nascimento);
        List<Usuario> usuarios = repository.findAll().stream()
                .filter(u -> u.getDataNascimento().isAfter(dataFiltro))
                .collect(Collectors.toList());

        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }
        return ResponseEntity.ok(usuarios); // 200
    }

    // 6. Atualizar
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizar(
            @PathVariable Integer id,
            @RequestBody Usuario usuario
    ) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build(); // 404
        }

        // valida se email/cpf já existem em outro usuário
        Optional<Usuario> emailExistente = repository.findByEmail(usuario.getEmail());
        if (emailExistente.isPresent() && !emailExistente.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409
        }

        Optional<Usuario> cpfExistente = repository.findByCpf(usuario.getCpf());
        if (cpfExistente.isPresent() && !cpfExistente.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409
        }

        usuario.setId(id); // garante que o ID seja o da URL
        Usuario atualizado = repository.save(usuario);
        return ResponseEntity.ok(atualizado); // 200
    }
}
