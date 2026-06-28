package net.javaguides.ems.service;

import net.javaguides.ems.dto.EmployeeDto;
import org.springframework.data.domain.Page;

public interface EmployeeService {
    EmployeeDto createEmployee(EmployeeDto employeeDto);

    EmployeeDto getEmployeeById(Long employeeId);

    Page<EmployeeDto> getAllEmployees(int page, int size, String sortBy, String sortDir);

    EmployeeDto updateEmployee(Long employeeId, EmployeeDto updatedEmployee);

    void deleteEmployee(Long employeeId);
}
