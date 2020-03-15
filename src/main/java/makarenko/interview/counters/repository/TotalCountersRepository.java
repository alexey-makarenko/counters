package makarenko.interview.counters.repository;

import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Repository
public class TotalCountersRepository extends BaseCountersRepository {

    private final AtomicReference<BigInteger> total = new AtomicReference<>(BigInteger.valueOf(0L));

    @Override
    public BigInteger getTotal() {
        return total.get();
    }

    @Override
    public Long increment(String id, int increment) {
        final AtomicLong counter = counters.get(id);
        if (counter == null || increment <= 0) {
            return null;
        }
        total.updateAndGet(value -> value.add(BigInteger.valueOf(increment)));
        return counter.addAndGet(increment);
    }

    @Override
    public OperationResult delete(String id) {
        final AtomicLong removed = counters.remove(id);
        if (removed == null) {
            return OperationResult.CounterNotFound;
        }
        total.updateAndGet(value -> value.subtract(BigInteger.valueOf(removed.get())));
        return OperationResult.Success;
    }

    @Override
    public void clear() {
        super.clear();
        total.set(BigInteger.valueOf(0L));
    }
}

