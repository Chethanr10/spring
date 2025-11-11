package com.example.webhookapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@SpringBootApplication
public class WebhookappApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(WebhookappApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		RestTemplate restTemplate = new RestTemplate();

		// Step 1️⃣: Generate webhook and token
		String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

		String jsonBody = """
                {
                    "name": "John Doe",
                    "regNo": "REG12347",
                    "email": "john@example.com"
                }
                """;

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

		System.out.println("Response: " + response.getBody());

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> responseMap = mapper.readValue(response.getBody(), Map.class);

		String webhookUrl = (String) responseMap.get("webhook");
		String token = (String) responseMap.get("accessToken");

		System.out.println("Webhook URL: " + webhookUrl);
		System.out.println("Access Token: " + token);

		// Step 2️⃣: Your final SQL query
		String finalQuery = """
                SELECT 
                    p.AMOUNT AS SALARY,
                    CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME,
                    FLOOR(DATEDIFF(CURDATE(), e.DOB) / 365) AS AGE,
                    d.DEPARTMENT_NAME
                FROM PAYMENTS p
                JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID
                JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID
                WHERE EXTRACT(DAY FROM p.PAYMENT_TIME) <> 1
                AND p.AMOUNT = (
                    SELECT MAX(AMOUNT)
                    FROM PAYMENTS
                    WHERE EXTRACT(DAY FROM PAYMENT_TIME) <> 1
                );
                """;

		// Step 3️⃣: Prepare JSON body with space after key name
		String queryJson = """
                {
                    "finalQuery ": "%s"
                }
                """.formatted(finalQuery);

		// Step 4️⃣: Authorization header with Bearer token
		HttpHeaders authHeaders = new HttpHeaders();
		authHeaders.setContentType(MediaType.APPLICATION_JSON);
		authHeaders.set("Authorization", "Bearer " + token);

		HttpEntity<String> finalRequest = new HttpEntity<>(queryJson, authHeaders);
		ResponseEntity<String> finalResponse = restTemplate.postForEntity(webhookUrl, finalRequest, String.class);

		System.out.println("Submission Response: " + finalResponse.getBody());
	}
}
