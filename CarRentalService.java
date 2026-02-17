package com.example.CarRental.service;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.CarRental.model.Car;
import com.example.CarRental.model.Customer;
import com.example.CarRental.model.Rental;

@Service
public class CarRentalService {

    private final List<Car> cars = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Rental> rentals = new ArrayList<>();

    // CarRental/cars.txt (same folder as pom.xml)
    private final Path filePath = Paths.get("cars.txt");
    private int nextId = 1;

    public CarRentalService() {
        loadCars(); // ✅ empty if file not present
    }

    public List<Car> getAllCars() {
        return cars;
    }

    public Car addCar(String brand, String model, double pricePerDay, String imageUrl) {
        String id = String.format("C%03d", nextId++);
        Car car = new Car(id, brand, model, pricePerDay, imageUrl);
        cars.add(car);
        saveCars();
        return car;
    }

    public void deleteCar(String carId) {
        Car car = findCar(carId).orElseThrow(() -> new IllegalArgumentException("Car not found"));

        if (!car.isAvailable()) {
            throw new IllegalStateException("Cannot delete: car is currently rented");
        }

        cars.removeIf(c -> c.getCarId().equalsIgnoreCase(carId));
        saveCars();
    }

    public Optional<Car> findCar(String carId) {
        return cars.stream().filter(c -> c.getCarId().equalsIgnoreCase(carId)).findFirst();
    }

    public double rentCar(String carId, String customerName, int days) {
        Car car = findCar(carId).orElseThrow(() -> new IllegalArgumentException("Car not found"));

        if (!car.isAvailable())
            throw new IllegalStateException("Car already rented");
        if (days <= 0)
            throw new IllegalArgumentException("Days must be > 0");
        if (customerName == null || customerName.trim().isEmpty())
            throw new IllegalArgumentException("Customer name required");

        Customer customer = new Customer("CUS" + (customers.size() + 1), customerName.trim());
        customers.add(customer);

        car.setAvailable(false);
        car.setRentedBy(customer.getName());
        rentals.add(new Rental(car, customer, days));

        saveCars();
        return days * car.getPricePerDay();
    }

    public void returnCar(String carId) {
        Car car = findCar(carId).orElseThrow(() -> new IllegalArgumentException("Car not found"));
        car.setAvailable(true);
        car.setRentedBy("");
        saveCars();
    }

    // -----------------------
    // SAVE / LOAD (Simple file)
    // Format per line:
    // carId|brand|model|pricePerDay|imageUrl|available|rentedBy
    // -----------------------
    private void saveCars() {
        List<String> lines = new ArrayList<>();
        for (Car c : cars) {
            lines.add(
                    safe(c.getCarId()) + "|" +
                            safe(c.getBrand()) + "|" +
                            safe(c.getModel()) + "|" +
                            c.getPricePerDay() + "|" +
                            safe(c.getImageUrl()) + "|" +
                            c.isAvailable() + "|" +
                            safe(c.getRentedBy()));
        }

        try {
            Files.write(filePath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save cars.txt");
        }
    }

    private void loadCars() {
        cars.clear();

        if (!Files.exists(filePath)) {
            nextId = 1; // ✅ empty startup
            return;
        }

        try {
            List<String> lines = Files.readAllLines(filePath);
            int max = 0;

            for (String line : lines) {
                if (line.trim().isEmpty())
                    continue;

                String[] p = line.split("\\|", -1);
                // p[0]=id p[1]=brand p[2]=model p[3]=price p[4]=img p[5]=available
                // p[6]=rentedBy
                Car car = new Car(p[0], p[1], p[2], Double.parseDouble(p[3]), p[4]);
                car.setAvailable(Boolean.parseBoolean(p[5]));
                car.setRentedBy(p.length > 6 ? p[6] : "");
                cars.add(car);

                if (p[0].matches("C\\d{3}")) {
                    int num = Integer.parseInt(p[0].substring(1));
                    max = Math.max(max, num);
                }
            }

            nextId = max + 1;

        } catch (Exception e) {
            cars.clear();
            nextId = 1;
        }
    }

    private String safe(String s) {
        if (s == null)
            return "";
        return s.replace("|", " ");
    }
}
