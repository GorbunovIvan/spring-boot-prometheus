package com.example.employee;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class EmployeeService {

    private final List<Employee> employees;

    private final static AtomicInteger EMPLOYEES_REQUESTED = new AtomicInteger();

    // Counter metric
    private static final Counter REQUESTS_TOTAL = Metrics.counter("employee_service_requests_total");

    // Gauge metric
    private static final Gauge EMPLOYEES_REQUESTED_BY_ID = Gauge.builder("employees_requests_by_id", () -> EMPLOYEES_REQUESTED)
            .description("IDs of employees that were requested")
            .tags("tag1", "value1", "tag2", "value2")
            .register(Metrics.globalRegistry);

    public EmployeeService() {

        this.employees = new ArrayList<>();

        create(new Employee("Bob", 29));
        create(new Employee("Steve", 38));
        create(new Employee("Maria", 31));
        create(new Employee("Mike", 22));
    }

    public List<Employee> getAll() {
        REQUESTS_TOTAL.increment();
        return new ArrayList<>(employees);
    }

    public Employee getById(Long id) {
        EMPLOYEES_REQUESTED.incrementAndGet();
        return employees.stream()
                .filter(e -> e.getId().equals(id))
                .findAny()
                .orElse(null);
    }

    public Employee create(@Nonnull Employee employee) {
        var nextId = generateNextId();
        employee.setId(nextId);
        employees.add(employee);
        return employee;
    }

    public void deleteById(Long id) {
        var employee = getById(id);
        if (employee != null) {
            employees.remove(employee);
        }
    }

    private long generateNextId() {

        if (employees.isEmpty()) {
            return 1;
        }

        var maxExistingId = employees.stream()
                .mapToLong(Employee::getId)
                .max()
                .getAsLong();

        return maxExistingId + 1;
    }
}
