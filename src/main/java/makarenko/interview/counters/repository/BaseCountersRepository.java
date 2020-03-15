package makarenko.interview.counters.repository;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public abstract class BaseCountersRepository implements CountersRepository {

    protected final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();

    @Override
    public OperationResult create(String id) {
        final AtomicLong previous = counters.putIfAbsent(id, new AtomicLong(0L));
        return previous == null ? OperationResult.Success : OperationResult.DuplicateCreation;
    }

    @Override
    public Set<String> getAllNames() {
        return counters.keySet();
    }

    @Override
    public Long findValue(String id) {
        final AtomicLong counter = counters.get(id);
        if (counter == null) {
            return null;
        }
        return counter.get();
    }

    @Override
    public Long increment(String id, int increment) {
        final AtomicLong counter = counters.get(id);
        if (counter == null || increment <= 0) {
            return null;
        }
        return counter.addAndGet(increment);
    }

    @Override
    public OperationResult delete(String id) {
        return counters.remove(id) != null ? OperationResult.Success : OperationResult.CounterNotFound;
    }

    @Override
    public void clear() {
        counters.clear();
    }
}

