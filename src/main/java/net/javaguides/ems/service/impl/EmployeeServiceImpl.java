package net.javaguides.ems.service.impl;

import lombok.AllArgsConstructor;
import net.javaguides.ems.dto.EmployeeDto;
import net.javaguides.ems.entity.Employee;
import net.javaguides.ems.exception.EmailAlreadyExistsException;
import net.javaguides.ems.exception.ResourceNotFoundException;
import net.javaguides.ems.mapper.EmployeeMapper;
import net.javaguides.ems.repository.EmployeeRepository;
import net.javaguides.ems.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@AllArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "firstName", "lastName", "email");

    private EmployeeRepository employeeRepository;

    @Override
    public EmployeeDto createEmployee(EmployeeDto employeeDto) {

        if(employeeRepository.findByEmail(employeeDto.getEmail()).isPresent()){
                    throw new EmailAlreadyExistsException("Employee already exists with email: " + employeeDto.getEmail());
        }

        Employee employee = EmployeeMapper.mapToEmployee(employeeDto);
        Employee savedEmployee = employeeRepository.save(employee);
        return EmployeeMapper.mapToEmployeeDto(savedEmployee);
    }

    @Override
    public EmployeeDto getEmployeeById(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Employee is not exist with given id : " + employeeId));

        return EmployeeMapper.mapToEmployeeDto(employee);
    }

    @Override
    public Page<EmployeeDto> getAllEmployees(int page, int size, String sortBy, String sortDir) {

        Pageable pageable = createPageable(page, size, sortBy, sortDir);

        Page<Employee> employeePage = employeeRepository.findAll(pageable);

        return employeePage.map(EmployeeMapper::mapToEmployeeDto);
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {

        if (page < 0) {
            throw new IllegalArgumentException("Page number must be zero or greater");
        }

        if (size < 1) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }

        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size must not be greater than " + MAX_PAGE_SIZE);
        }

        String sortProperty = sortBy == null ? "" : sortBy.trim();

        if (!SORTABLE_FIELDS.contains(sortProperty)) {
            throw new IllegalArgumentException("Sort field must be one of: id, firstName, lastName, email");
        }

        Sort.Direction direction = getSortDirection(sortDir);

        return PageRequest.of(page, size, Sort.by(direction, sortProperty));
    }

    private Sort.Direction getSortDirection(String sortDir) {

        String direction = sortDir == null ? "" : sortDir.trim();

        if ("asc".equalsIgnoreCase(direction)) {
            return Sort.Direction.ASC;
        }

        if ("desc".equalsIgnoreCase(direction)) {
            return Sort.Direction.DESC;
        }

        throw new IllegalArgumentException("Sort direction must be 'asc' or 'desc'");
    }

    @Override
    public EmployeeDto updateEmployee(Long employeeId, EmployeeDto updatedEmployee) {
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(
                () -> new ResourceNotFoundException("Employee is not exist with given Id : "+ employeeId)
        );

        employee.setFirstName(updatedEmployee.getFirstName());
        employee.setLastName(updatedEmployee.getLastName());
        employee.setEmail(updatedEmployee.getEmail());

        Employee updatedEmployeeObj =  employeeRepository.save(employee);
        return EmployeeMapper.mapToEmployeeDto(updatedEmployeeObj);
    }

    @Override
    public void deleteEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Employee is not exist with given id : " + employeeId)
                );

        employeeRepository.deleteById(employeeId);
    }

}
















