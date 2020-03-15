package makarenko.interview.counters.repository;

import java.math.BigInteger;
import java.util.Set;

public interface CountersRepository {
    OperationResult create(String id);

    Long findValue(String id);

    Set<String> getAllNames();

    BigInteger getTotal();

    Long increment(String id, int increment);

    OperationResult delete(String id);

    void clear();
}
