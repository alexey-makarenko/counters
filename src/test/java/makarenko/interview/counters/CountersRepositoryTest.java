package makarenko.interview.counters;

import makarenko.interview.counters.repository.CalculatingCountersRepository;
import makarenko.interview.counters.repository.CountersRepository;
import makarenko.interview.counters.repository.TotalCountersRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CountersRepositoryTest {

    private static Stream<CountersRepository> generator() {
        return Stream.of(new TotalCountersRepository(), new CalculatingCountersRepository());
    }

    @ParameterizedTest
    @MethodSource("generator")
    public void successCreateCounter(CountersRepository countersRepository) {
        for (int i = 1; i <= 10; i++) {
            countersRepository.create(Integer.toString(i));
            assertEquals(i, countersRepository.getAllNames().size());
        }
        assertEquals(BigInteger.ZERO, countersRepository.getTotal());
    }

    @ParameterizedTest
    @MethodSource("generator")
    public void successTotalOfTwo(CountersRepository countersRepository) {
        countersRepository.create("counter_1");
        countersRepository.increment("counter_1", 2);
        assertEquals(2, countersRepository.findValue("counter_1"));

        countersRepository.create("counter_2");
        countersRepository.increment("counter_2", 8);
        assertEquals(8, countersRepository.findValue("counter_2"));

        assertEquals(BigInteger.valueOf(10), countersRepository.getTotal());
    }

}

