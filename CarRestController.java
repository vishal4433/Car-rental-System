package com.example.CarRental.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.CarRental.model.Car;
import com.example.CarRental.service.CarRentalService;

@RestController
@RequestMapping("/api")
public class CarRestController {

    private final CarRentalService service;

    public CarRestController(CarRentalService service) {
        this.service = service;
    }

    @GetMapping("/cars")
    public List<Car> getCars() {
        return service.getAllCars();
    }

    @PostMapping("/cars")
    public ResponseEntity<?> addCar(@RequestBody Map<String, String> body) {
        String brand = body.getOrDefault("brand", "").trim();
        String model = body.getOrDefault("model", "").trim();
        String imageUrl = body.getOrDefault("imageUrl", "").trim();

        double price;
        try {
            price = Double.parseDouble(body.getOrDefault("pricePerDay", "0"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid price"));
        }

        if (brand.isEmpty() || model.isEmpty() || imageUrl.isEmpty() || price <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Enter valid car details"));
        }

        Car car = service.addCar(brand, model, price, imageUrl);
        return ResponseEntity.ok(car);
    }

    @DeleteMapping("/cars/{carId}")
    public ResponseEntity<?> deleteCar(@PathVariable String carId) {
        try {
            service.deleteCar(carId);
            return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/rent")
    public ResponseEntity<?> rent(@RequestBody Map<String, String> body) {
        String carId = body.getOrDefault("carId", "").trim();
        String customer = body.getOrDefault("customer", "").trim();

        int days;
        try {
            days = Integer.parseInt(body.getOrDefault("days", "0"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid days"));
        }

        try {
            double total = service.rentCar(carId, customer, days);
            return ResponseEntity.ok(Map.of("message", "Rented successfully", "total", total));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/return/{carId}")
    public ResponseEntity<?> returnCar(@PathVariable String carId) {
        try {
            service.returnCar(carId);
            return ResponseEntity.ok(Map.of("message", "Returned successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
