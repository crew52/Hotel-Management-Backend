package codegym.c10.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class HotelManagementBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelManagementBackendApplication.class, args);
	}

} 