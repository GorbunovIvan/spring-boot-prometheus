package com.example.employee;

import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeService {

    private final List<Employee> employees;

    public EmployeeService() {

        this.employees = new ArrayList<>();

        create(new Employee("Bob", 29));
        create(new Employee("Steve", 38));
        create(new Employee("Maria", 31));
        create(new Employee("Mike", 22));
    }

    public List<Employee> getAll() {
        return new ArrayList<>(employees);
    }

    public Employee getById(Long id) {
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
