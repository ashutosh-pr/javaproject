package com.demo.crud_demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class EmployeeService {
    @Autowired
    EmployeeRepository repo;

    public Employee createEmp(Employee emp){
        return repo.save(emp);
    }    

    public List<Employee> getAll(){
        return repo.findAll();
    }

    public Employee findById(int id){
        return repo.findById(id).orElse(null);
    }
}
