package com.smartbox.backend.controller;

import com.smartbox.backend.model.LocationRequest;
import com.smartbox.backend.model.Locker;
import com.smartbox.backend.repository.LockerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@CrossOrigin
public class LockerController {

    @Autowired
    private LockerRepository lockerRepository;


    @PostMapping("/generate-lockers")
    public List<Locker> generate(
            @RequestBody LocationRequest request)
    {
        return lockerRepository.findAll();
    }

    @GetMapping("/lockers")
    public List<Locker> lockers()
    {
        return lockerRepository.findAll();
    }
}
