package com.demo.crud_demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService empService;

    @PostMapping()
    public Employee createEmployee(@RequestBody Employee emp){
        return empService.createEmp(emp);
    }
    
    @GetMapping()
    public List<Employee> getEmployees(){
        return empService.getAll();
    }

    @GetMapping("/{id}")
    public Employee getById(@PathVariable int id){
        return empService.findById(id);
    }

    
    
}
