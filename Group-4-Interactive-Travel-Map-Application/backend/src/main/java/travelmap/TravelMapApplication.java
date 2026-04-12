package travelmap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import travelmap.account.AccountFacade;
import travelmap.model.User;

@SpringBootApplication
public class TravelMapApplication {
    public static void main(String[] args) {
        SpringApplication.run(TravelMapApplication.class, args);
    }

    @Bean
    public CommandLineRunner testAuth(AccountFacade accountFacade) {
        return args -> {
            User user = new User();
            user.setUsername("wilson");
            user.setPassword("password123");

            System.out.println("=== AUTH TEST START ===");

            User registeredUser = accountFacade.register(user);
            System.out.println("Register worked: " + (registeredUser != null));

            boolean loginSuccess = accountFacade.login("wilson", "password123");
            System.out.println("Login with correct password: " + loginSuccess);

            boolean loggedIn = accountFacade.isLoggedIn();
            System.out.println("Is logged in: " + loggedIn);

            System.out.println("Current user: " +
                (accountFacade.getCurrentUser() != null
                    ? accountFacade.getCurrentUser().getUsername()
                    : "null"));

            accountFacade.logout();
            System.out.println("Logged out.");

            System.out.println("Is logged in after logout: " + accountFacade.isLoggedIn());

            boolean wrongLogin = accountFacade.login("wilson", "wrongpass");
            System.out.println("Login with wrong password: " + wrongLogin);

            System.out.println("=== AUTH TEST END ===");
        };
    }
}