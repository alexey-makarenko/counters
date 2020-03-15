package makarenko.interview.counters.api;

import makarenko.interview.counters.repository.CountersRepository;
import makarenko.interview.counters.repository.OperationResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.Set;

@RestController
@RequestMapping("/v1/counters")
public class CountersController {

    private final CountersRepository countersRepository;

    public CountersController(CountersRepository countersRepository) {
        this.countersRepository = countersRepository;
    }

    @PostMapping("/create")
    public Object createCounter(@RequestBody String id) {
        final OperationResult result = countersRepository.create(id);
        if (result.isSuccess())
            return ResponseEntity.status(HttpStatus.CREATED).build();
        return response(result);
    }

    @GetMapping("/{id}/value")
    public Object findValue(@PathVariable String id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        final Long value = countersRepository.findValue(id);
        if (value == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(value.toString());
        }
    }

    @PostMapping("/{id}/increment")
    public Object increment(@PathVariable String id,
                            @RequestBody Integer increment) {
        if (id == null || increment == null || increment <= 0) {
            return ResponseEntity.badRequest().build();
        }
        final Long result = countersRepository.increment(id, increment);
        if (result == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(result);
        }
    }

    @DeleteMapping("/{id}")
    public Object delete(@PathVariable String id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        final OperationResult result = countersRepository.delete(id);
        return response(result);
    }

    @GetMapping("/names")
    public Set<String> getAllNames() {
        return countersRepository.getAllNames();
    }

    @GetMapping("/total")
    public BigInteger getTotal() {
        return countersRepository.getTotal();
    }

    private Object response(OperationResult result) {
        if (result.isSuccess()) {
            return ResponseEntity.ok().build();
        }
        if (result == OperationResult.CounterNotFound) {
            return ResponseEntity.notFound().build();
        }
        if (result == OperationResult.DuplicateCreation) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Counter is already created");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
