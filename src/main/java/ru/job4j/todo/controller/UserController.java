package ru.job4j.todo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.job4j.todo.model.User;
import ru.job4j.todo.service.UserService;
import ru.job4j.todo.util.TimeZoneUtils;

@Controller()
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserService simpleUserService;

    @GetMapping("/register")
    public String getRegistrationPage(Model model) {
        model.addAttribute("timezones", TimeZoneUtils.getAvailableTimeZoneIds());
        return "users/register";
    }

    @PostMapping("/register")
    public String register(Model model, @ModelAttribute User user) {
        var savedUser = simpleUserService.save(user);
        if (savedUser.isEmpty()) {
            model.addAttribute("error", "Пользователь с таким логином уже зарегистрирован");
            return "users/register";
        }
        return "redirect:/index";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "users/login";
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute User user, Model model, HttpServletRequest request) {
        var userOptional = simpleUserService.findByLoginAndPassword(user.getLogin(), user.getPassword());
        if (userOptional.isEmpty()) {
            model.addAttribute("error", "Логин или пароль введены неверно");
            return "users/login";
        }
        var session = request.getSession();
        session.setAttribute("user", userOptional.get());
        return "redirect:/index";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/users/login";
    }

}
