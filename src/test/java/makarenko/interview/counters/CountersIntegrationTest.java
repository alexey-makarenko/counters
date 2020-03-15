package makarenko.interview.counters;

import makarenko.interview.counters.repository.CountersRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = CountersApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CountersIntegrationTest {
    private static final String LOCAL_HOST = "http://localhost:";
    private static final String REST_API_PATH = "/v1/counters";
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private CountersRepository countersRepository;
    @LocalServerPort
    private int port;

    private static <T> T sure(T val) {
        assertNotNull(val);
        return val;
    }

    @BeforeEach
    public void prepare() {
        countersRepository.clear();
    }

    private String getBaseUrl() {
        return LOCAL_HOST + port + REST_API_PATH;
    }

    private String getCounterUrl(String id) {
        return LOCAL_HOST + port + REST_API_PATH + "/" + id;
    }

    @Test
    public void successCreateCounter() {
        for (int i = 1; i <= 10; i++) {
            createNewCounter(Integer.toString(i));
            assertEquals(i, countersRepository.getAllNames().size());
        }
        assertEquals(BigInteger.ZERO, getTotalValue());
    }

    @Test
    public void failDuplicateCreateCounter() {
        final String id = "My Counter !";
        createNewCounter(id);
        successIncrement(id, 10);
        final ResponseEntity<String> response = restCreateCounter(id);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(10, getValue(id));
        assertEquals(BigInteger.valueOf(10), getTotalValue());
    }

    @Test
    public void invalidRequestApi() {
        final String id = "valid_id";
        createNewCounter(id);
        final ResponseEntity<String> result = restIncrement(id, "12.12.12");
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void failNotFoundCounterValue() {
        ResponseEntity<Long> result = restValue("invalid_id");
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void failNotFoundCounterIncrement() {
        final ResponseEntity<String> response = restIncrement("not_existing_counter", "1");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void failNotFoundCounterDelete() {
        final ResponseEntity<String> response = restDeleteCounter("not_existing_counter");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void successIncrementSingle() {
        final String counter1 = createNewCounter("counter_1");
        successIncrement(counter1, 1);
        assertEquals(1, getValue(counter1));
        successIncrement(counter1, 9);
        assertEquals(10, getValue(counter1));

        assertEquals(BigInteger.valueOf(10), getTotalValue());
    }

    @Test
    public void successTotalOfTwo() {
        final String counter1 = createNewCounter("counter_1");
        successIncrement(counter1, 2);
        assertEquals(2, getValue(counter1));

        final String counter2 = createNewCounter("counter_2");
        successIncrement(counter2, 8);
        assertEquals(8, getValue(counter2));

        assertEquals(BigInteger.valueOf(10), getTotalValue());
    }

    @Test
    public void successTotalOnDelete() {
        final String counter1 = createNewCounter("counter_1");
        final String counter2 = createNewCounter("counter_2");
        successIncrement(counter1, 2);
        successIncrement(counter2, 8);
        assertEquals(BigInteger.valueOf(10), getTotalValue());

        successDelete(counter1);

        assertEquals(BigInteger.valueOf(8), getTotalValue());
    }

    @Test
    public void successAllNames() {
        final String counter1 = createNewCounter("counter_1");
        final String counter2 = createNewCounter("counter_2");

        ResponseEntity<String> response = restAllNames();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        final String[] result = sure(response.getBody())
                .replace("[", "")
                .replace("]", "")
                .split(",");
        final String[] expected = {counter1, counter2};
        Matchers.arrayContainingInAnyOrder(expected, result);
    }

    private Long getValue(String id) {
        final ResponseEntity<Long> result = restValue(id);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        return result.getBody();
    }

    private void successIncrement(String id, int increment) {
        final ResponseEntity<String> response = restIncrement(id, Integer.toString(increment));
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    private void successDelete(String id) {
        final ResponseEntity<String> response = restDeleteCounter(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    private String createNewCounter(String id) {
        ResponseEntity<String> response = restCreateCounter(id);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNull(response.getBody());
        return id;
    }

    private BigInteger getTotalValue() {
        final String url = getBaseUrl() + "/total";
        final ResponseEntity<String> result = sure(restTemplate.exchange(
                url, HttpMethod.GET,
                null, String.class));
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        return new BigInteger(result.getBody());
    }

    private ResponseEntity<String> restCreateCounter(String id) {
        final String url = getBaseUrl() + "/create";
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<String> httpEntity = new HttpEntity<>(id, headers);
        return sure(restTemplate.exchange(
                url, HttpMethod.POST,
                httpEntity, String.class));
    }

    private ResponseEntity<String> restDeleteCounter(String id) {
        final String url = getCounterUrl(id);
        return sure(restTemplate.exchange(
                url, HttpMethod.DELETE,
                null, String.class));
    }

    private ResponseEntity<Long> restValue(String id) {
        final String url = getCounterUrl(id) + "/value";
        return sure(restTemplate.exchange(
                url, HttpMethod.GET,
                null, Long.class));
    }

    private ResponseEntity<String> restAllNames() {
        final String url = getBaseUrl() + "/names";
        return sure(restTemplate.exchange(
                url, HttpMethod.GET,
                null, String.class));
    }

    private ResponseEntity<String> restIncrement(String id, String delta) {
        final String url = getCounterUrl(id) + "/increment";
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<String> httpEntity = new HttpEntity<>(delta, headers);
        return sure(restTemplate.exchange(
                url, HttpMethod.POST,
                httpEntity, String.class));
    }
}

