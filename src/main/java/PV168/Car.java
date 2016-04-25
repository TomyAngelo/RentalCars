package PV168;

import java.math.BigDecimal;

/**
 * Created by jima88 on 9. 3. 2016.
 */
public class Car {

    private Long id;
    private String licensePlate;
    private String model;
    private BigDecimal price;
    private BigDecimal numberOfKM;
    private boolean isBorrowed;

    public Car(String licensePlate, String model, BigDecimal price, BigDecimal numberOfKM) {
        this.licensePlate = licensePlate;
        this.model = model;
        this.price = price;
        this.numberOfKM = numberOfKM;
    }
    public Car(){

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getNumberOfKM() {
        return numberOfKM;
    }

    public void setNumberOfKM(BigDecimal numberOfKM) {
        this.numberOfKM = numberOfKM;
    }

    public boolean getIsBorrowed() {
        return isBorrowed;
    }

    public void setIsBorrowed(boolean borrowed) {
        isBorrowed = borrowed;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", licensePlate='" + licensePlate + '\'' +
                ", model='" + model + '\'' +
                ", price='" + price + '\'' +
                ", numberOfKM='" + numberOfKM + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Car car = (Car) o;

        if (id != null ? !id.equals(car.id) : car.id != null) return false;
        if (licensePlate != null ? !licensePlate.equals(car.licensePlate) : car.licensePlate != null) return false;
        if (model != null ? !model.equals(car.model) : car.model != null) return false;
        if (price != null ? !price.equals(car.price) : car.price != null) return false;
        return numberOfKM != null ? numberOfKM.equals(car.numberOfKM) : car.numberOfKM == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 43 * result + (licensePlate != null ? licensePlate.hashCode() : 0);
        result = 43 * result + (model != null ? model.hashCode() : 0);
        result = 43 * result + (price != null ? price.hashCode() : 0);
        result = 43 * result + (numberOfKM != null ? numberOfKM.hashCode() : 0);
        return result;
    }
}
