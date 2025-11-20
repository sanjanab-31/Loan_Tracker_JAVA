package com.example.emi.controller;
import com.example.emi.model.Loan;
import com.example.emi.model.User;
import com.example.emi.service.LoanService;
import com.example.emi.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
public class PageController {
    private final UserService userService;
    private final LoanService loanService;
    public PageController(UserService userService, LoanService loanService) {
        this.userService = userService;
        this.loanService = loanService;
    }
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "index";
    }
    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        return "user-form";
    }
    @PostMapping("/users")
    public String createUser(@ModelAttribute User user) {
        userService.createUser(user);
        // After creating a user, redirect to main users page
        return "redirect:/";
    }
    @GetMapping("/loans")
    public String loansByUser(@RequestParam Long userId, Model model) {
        model.addAttribute("loans", loanService.getLoansByUserId(userId));
        return "loans";
    }
    @GetMapping("/loans/new")
    public String newLoanForm(@RequestParam Long userId, Model model) {
        Loan loan = new Loan();
        loan.setUser(userService.getUserById(userId));
        model.addAttribute("loan", loan);
        return "loan-form";
    }
    @PostMapping("/loans")
    public String createLoan(@ModelAttribute Loan loan) {
        loanService.createLoan(loan);
        return "redirect:/";
    }
}
