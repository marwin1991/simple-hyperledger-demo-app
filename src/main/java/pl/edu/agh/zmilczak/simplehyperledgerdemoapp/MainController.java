package pl.edu.agh.zmilczak.simplehyperledgerdemoapp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Controller
public class MainController {

    private AuthorizationService authorizationService;
    private LedgerServices ledgerServices;

    @Autowired
    public MainController(AuthorizationService authorizationService, LedgerServices ledgerServices) {
        this.authorizationService = authorizationService;
        this.ledgerServices = ledgerServices;
    }

    @GetMapping("/")
    public String index(Model model) {
        init(model);
        return "index";
    }

    @PostMapping("/enrollAdmin")
    public String enrollAdmin(Model model) {
        init(model);
        try {
            model.addAttribute("resultEnrollAdmin", authorizationService.enrollAdmin());
        } catch (Exception e) {
            model.addAttribute("resultEnrollAdmin", e.getMessage());
        }
        return "index";
    }

    @PostMapping("/registerUser")
    public String registerUser(Model model) {
        init(model);
        try {
            model.addAttribute("resultRegisterUser", authorizationService.registerUser());
        } catch (Exception e) {
            model.addAttribute("resultRegisterUser", e.getMessage());
        }
        return "index";
    }

    @PostMapping("/getAllCars")
    public String getAllCars(Model model) {
        init(model);
        try {
            List<Result> result = ledgerServices.getAllCars();

            result.sort((o1, o2) -> {
                if (o1.getKey().length() > o2.getKey().length()) {
                    return 1;
                } else if (o1.getKey().length() < o2.getKey().length()) {
                    return -1;
                }
                return o1.getKey().compareTo(o2.getKey());
            });


            model.addAttribute("resultCars", result);
        } catch (Exception e) {
            model.addAttribute("resultCars", Collections.emptyList());
            model.addAttribute("resultCarsError", e.getMessage());
        }
        return "index";
    }

    @PostMapping("/createCar")
    public String createCar(Model model, @ModelAttribute("result") Result result) {
        init(model);
        try {
            model.addAttribute("resultCreateCar", ledgerServices.createCar(result));
        } catch (Exception e) {
            model.addAttribute("resultCreateCar", e.getMessage());
        }
        return "index";
    }


    private void init(Model model){
        model.addAttribute("result", new Result());
    }
}
