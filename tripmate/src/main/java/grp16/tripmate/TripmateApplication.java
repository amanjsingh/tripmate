package grp16.tripmate;

import grp16.tripmate.db.connection.DatabaseConnection;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Connection;

@SpringBootApplication
public class TripmateApplication {
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(@RequestParam(name = "name", required = false, defaultValue = "World") String name, Model model) {
        model.addAttribute("name", name);
        return "index";
    }

    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(TripmateApplication.class).profiles("devint", "production", "test").run(args);

        DatabaseConnection databaseConnection = new DatabaseConnection();
        Connection connection = databaseConnection.getDatabaseConnection();


    }
}