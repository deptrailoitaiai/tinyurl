package tinyurl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.BeanFactory;

@SpringBootApplication(
        exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
public class App {
    public static void main( String[] args ) {
        ApplicationContext context = SpringApplication.run(App.class, args);

        // In ra class của context để xác định nó là ApplicationContext hay gì khác
        System.out.println("Context class: " + context.getClass());

        // Kiểm tra xem context có phải là BeanFactory không
        if (context instanceof BeanFactory) {
            System.out.println(">> This context also implements BeanFactory");
        }

        // Kiểm tra có phải là ApplicationContext không
        if (context instanceof ApplicationContext) {
            System.out.println(">> This is an ApplicationContext");
        }
    }
}
