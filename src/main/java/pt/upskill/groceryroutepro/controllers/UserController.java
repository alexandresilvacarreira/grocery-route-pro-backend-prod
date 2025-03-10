package pt.upskill.groceryroutepro.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import pt.upskill.groceryroutepro.exceptions.ValidationException;
import pt.upskill.groceryroutepro.entities.User;
import pt.upskill.groceryroutepro.models.ChangePasswordRequestModel;
import pt.upskill.groceryroutepro.models.EmailVerificationToken;
import pt.upskill.groceryroutepro.models.Emailmodel;
import pt.upskill.groceryroutepro.models.SignUp;
import pt.upskill.groceryroutepro.services.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@Component
public class UserController {

    @Autowired
    UserService userService;


    @GetMapping("/users/get-authenticated-user")
    public ResponseEntity getAuthenticatedUser() {

        User authenticatedUser = this.userService.getAuthenticatedUser();
        if (authenticatedUser != null)
            return ResponseEntity.ok(authenticatedUser);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signUp(@RequestBody SignUp signUp) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.createAccount(signUp);
            response.put("success", true);
            response.put("message", "Conta criada com sucesso");
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(response);
        }
    }

    @PostMapping("/verify-account/")
    public ResponseEntity<Map<String, Object>> verifyAccount(@RequestBody EmailVerificationToken emailVerificationToken) {
        Map<String, Object> response = new HashMap<>();

        try {
            userService.verifyEmail(emailVerificationToken);
            response.put("success", true);
            response.put("message", "Conta verificada com sucesso");
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(response);
        }
    }

    @PostMapping("/users/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Emailmodel emailmodel) {
        Map<String, Object> response = new HashMap<>();

        try {
            userService.getPasswordLinkFromEmail(emailmodel.getEmail());
            response.put("success", true);
            response.put("message", "Pedido de mudança de password efetuado com sucesso");
            return ResponseEntity.ok(response);

        } catch (
                ValidationException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(response);
        }

    }


    @PostMapping("/users/change-password/")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody ChangePasswordRequestModel changePasswordRequest) {

        Map<String, Object> response = new HashMap<>();
        try {
            userService.changePassword(changePasswordRequest.getEmail(),
                    changePasswordRequest.getToken(), changePasswordRequest.getPassword());
            response.put("success", true);
            response.put("message", "Password Alterada com sucesso");
            return ResponseEntity.ok(response);
        } catch (ValidationException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body(response);
        }
    }


}
