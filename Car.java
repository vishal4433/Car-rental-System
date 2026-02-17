package com.example.CarRental.model;

public class Car {
   private String carId;
   private String brand;
   private String model;
   private double pricePerDay;
   private String imageUrl;
   private boolean available = true;
   private String rentedBy = "";

   public Car() {
   }

   public Car(String carId, String brand, String model, double pricePerDay, String imageUrl) {
      this.carId = carId;
      this.brand = brand;
      this.model = model;
      this.pricePerDay = pricePerDay;
      this.imageUrl = imageUrl;
      this.available = true;
      this.rentedBy = "";
   }

   public String getCarId() {
      return carId;
   }

   public void setCarId(String carId) {
      this.carId = carId;
   }

   public String getBrand() {
      return brand;
   }

   public void setBrand(String brand) {
      this.brand = brand;
   }

   public String getModel() {
      return model;
   }

   public void setModel(String model) {
      this.model = model;
   }

   public double getPricePerDay() {
      return pricePerDay;
   }

   public void setPricePerDay(double pricePerDay) {
      this.pricePerDay = pricePerDay;
   }

   public String getImageUrl() {
      return imageUrl;
   }

   public void setImageUrl(String imageUrl) {
      this.imageUrl = imageUrl;
   }

   public boolean isAvailable() {
      return available;
   }

   public void setAvailable(boolean available) {
      this.available = available;
   }

   public String getRentedBy() {
      return rentedBy;
   }

   public void setRentedBy(String rentedBy) {
      this.rentedBy = rentedBy;
   }
}
