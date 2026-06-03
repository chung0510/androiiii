package com.smartbox.backend.controller;

import com.smartbox.backend.model.*;
import com.smartbox.backend.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class UserController {

    private final UserRepository repository;

    public UserController(UserRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/register")
    public RegisterResponse register(
            @RequestBody RegisterRequest request
    ) {

        User exist =
                repository.findByUsername(
                        request.getUsername()
                );

        if(exist != null){
            return new RegisterResponse(
                    false,
                    "Tên đăng nhập đã tồn tại"
            );
        }
        if(request.getUsername() == null ||
                request.getUsername().trim().isEmpty())
        {
            return new RegisterResponse(
                    false,
                    "Tên đăng nhập không hợp lệ"
            );
        }

        if(request.getUsername().length() < 4)
        {
            return new RegisterResponse(
                    false,
                    "Tên đăng nhập tối thiểu 4 ký tự"
            );
        }

        if(request.getPassword() == null ||
                request.getPassword().length() < 8)
        {
            return new RegisterResponse(
                    false,
                    "Mật khẩu tối thiểu 8 ký tự"
            );
        }

        User user = new User();

        user.setUsername(
                request.getUsername()
        );

        user.setPassword(
                request.getPassword()
        );

        repository.save(user);

        return new RegisterResponse(
                true,
                "Đăng ký thành công"
        );
    }

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest request
    ) {

        User user =
                repository.findByUsername(
                        request.getUsername()
                );

        if(user == null){
            return new LoginResponse(
                    false,
                    null,
                    null
            );
        }

        if(!user.getPassword()
                .equals(request.getPassword()))
        {
            return new LoginResponse(
                    false,
                    null,
                    null
            );
        }

        return new LoginResponse(
                true,
                user.getId(),
                user.getUsername()
        );
    }

    @GetMapping("/user/{id}")
    public User getUser(
            @PathVariable String id
    ){
        return repository.findById(id)
                .orElse(null);
    }
}