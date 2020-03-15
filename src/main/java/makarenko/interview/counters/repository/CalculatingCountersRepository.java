package makarenko.interview.counters.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;

@Profile("another")
@Repository
public class CalculatingCountersRepository extends BaseCountersRepository {

    @Override
    public BigInteger getTotal() {
        return counters.values().stream().map(c -> BigInteger.valueOf(c.get()))
                .reduce(BigInteger::add).orElse(BigInteger.ZERO);
    }
}

