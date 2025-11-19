package com.demo.crud_demo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
        // save
        // find
        // delete
        // update - save
    
}