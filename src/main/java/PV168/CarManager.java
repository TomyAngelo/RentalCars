package PV168;

import java.util.List;

/**
 * Created by jima88 on 16.3.2016.
 */
public interface CarManager {

    void addCar(Car car);
    void deleteCar(Long id);
    void editCar(Long idOfOriginal, Car updatedCar);
    List<Car> getAllCars();
    Car getCarById(Long id);
    Car getCarByLicensePlate(String licensePlate);
    boolean getAvailabilityOfCar (Long id);

}
