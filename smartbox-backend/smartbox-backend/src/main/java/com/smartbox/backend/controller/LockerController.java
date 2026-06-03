package com.smartbox.backend.controller;

import com.smartbox.backend.model.LocationRequest;
import com.smartbox.backend.model.Locker;
import com.smartbox.backend.repository.LockerRepository;
import com.smartbox.backend.service.LockerGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class LockerController {

    @Autowired
    private LockerRepository lockerRepository;

    @Autowired
    private LockerGeneratorService service;

    @PostMapping("/generate-lockers")
    public List<Locker> generate(
            @RequestBody LocationRequest request)
    {
        return service.generate(
                request.getLat(),
                request.getLng());
    }

    @GetMapping("/lockers")
    public List<Locker> lockers()
    {
        return lockerRepository.findAll();
    }
}
