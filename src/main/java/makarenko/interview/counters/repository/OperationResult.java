package makarenko.interview.counters.repository;

public enum OperationResult {
    Success, CounterNotFound, DuplicateCreation;

    public boolean isSuccess() {
        return this == Success;
    }
}
