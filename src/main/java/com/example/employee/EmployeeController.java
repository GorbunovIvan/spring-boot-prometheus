package com.example.employee;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    private MeterRegistry meterRegistry;

    private static final AtomicInteger REQUESTS_TOTAL = new AtomicInteger();
    private Timer requestDurationTimer;

    @Autowired
    public void setMeterRegistry(MeterRegistry meterRegistry) {

        this.meterRegistry = meterRegistry;

        // Gauge metric
        this.meterRegistry.gauge("employee_controller_requests_total", REQUESTS_TOTAL);

        // Histogram metric
        this.requestDurationTimer = Timer.builder("employee_controller_request_duration_seconds")
                .description("Request duration in seconds for EmployeeController")
                .register(this.meterRegistry);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Employee> getAll() {
        REQUESTS_TOTAL.incrementAndGet();
        return employeeService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getById(@PathVariable long id) {
        Employee employee;
        Timer.Sample timerSample = Timer.start();
        try {
            employee = employeeService.getById(id);
        } finally {
            timerSample.stop(requestDurationTimer);
        }
        if (employee == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(employee, HttpStatus.OK);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Employee create(@RequestBody Employee employee) {
        return employeeService.create(employee);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable long id) {
        employeeService.deleteById(id);
    }
}
