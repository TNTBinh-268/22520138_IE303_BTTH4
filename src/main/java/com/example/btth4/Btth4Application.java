package com.example.btth4;

import com.example.btth4.model.Product;
import com.example.btth4.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

@SpringBootApplication
public class Btth4Application {
    private static final Logger logger = LoggerFactory.getLogger(Btth4Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Btth4Application.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(ProductRepository productRepository) {
        return args -> {
            try {
                InputStream is = getClass().getClassLoader().getResourceAsStream("product-info.txt");
                if (is == null) {
                    logger.error("Could not find product-info.txt");
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String line;
                String name = null, brand = null, description = null, image = null;
                double price = 0;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("image ")) {
                        image = "img" + line.replace("image ", "").replace(":", "") + ".png";
                        logger.debug("Processing image: {}", image);
                    } else if (line.startsWith("- Name:")) {
                        name = line.replace("- Name:", "").trim();
                        logger.debug("Processing name: {}", name);
                    } else if (line.startsWith("- Price:")) {
                        String priceStr = line.replace("- Price:", "").trim();
                        try {
                            priceStr = priceStr.replace(",", "");
                            price = Double.parseDouble(priceStr);
                            logger.debug("Successfully parsed price: {}", price);
                        } catch (Exception e) {
                            logger.error("Error parsing price from string: {}", priceStr, e);
                            price = 0;
                        }
                    } else if (line.startsWith("- Brand:")) {
                        brand = line.replace("- Brand:", "").trim();
                        logger.debug("Processing brand: {}", brand);
                    } else if (line.startsWith("- Description:")) {
                        description = line.replace("- Description:", "").trim();
                        logger.debug("Processing description: {}", description);
                        if (name != null && brand != null && description != null && image != null) {
                            try {
                                Product product = new Product(null, name, price, brand, description, image);
                                productRepository.save(product);
                                logger.info("Successfully saved product: {}", product);
                            } catch (Exception e) {
                                logger.error("Error saving product: {}", name, e);
                            }

                            name = brand = description = image = null;
                            price = 0;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error processing product-info.txt", e);
                throw e;
            }
        };
    }
}
