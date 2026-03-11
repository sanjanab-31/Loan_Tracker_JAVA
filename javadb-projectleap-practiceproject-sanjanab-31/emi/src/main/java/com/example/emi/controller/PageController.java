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
        model.addAttribute("stats", loanService.getGlobalSummary());
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
        return "redirect:/?success=User+added+successfully";
    }

    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        return "user-form";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/?success=User+deleted+successfully";
    }
    @GetMapping("/loans")
    public String loansByUser(@RequestParam(required = false) Long userId, Model model) {
        if (userId != null) {
            model.addAttribute("loans", loanService.getLoansByUserId(userId));
            return "loans";
        }
        model.addAttribute("loans", loanService.getAllLoans());
        return "loans-all";
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
        return "redirect:/?success=Loan+disbursed+successfully";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("report", loanService.getReportSummary());
        return "reports";
    }
}
