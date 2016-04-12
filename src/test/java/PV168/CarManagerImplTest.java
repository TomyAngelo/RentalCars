package PV168;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Created by jima88 on 16.3.2016.
 */
public class CarManagerImplTest {

    private CarManagerImpl manager;
    private DataSource dataSource;

    @Before
    public void setUp() throws Exception {
        dataSource = prepareDataSource();
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("CREATE TABLE CARS ("
                    + "id bigint primary key generated always as identity,"
                    + "licensePlate varchar(255),"
                    + "model varchar(255),"
                    + "numberOfKM int,"
                    + "price int)").executeUpdate();
        }
        manager = new CarManagerImpl(dataSource);
    }

    @Rule
    // attribute annotated with @Rule annotation must be public :-(
    public ExpectedException expectedException = ExpectedException.none();

    @After
    public void tearDown() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("DROP TABLE CARS").executeUpdate();
        }
    }

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        //we will use in memory database
        ds.setDatabaseName("memory:carmgr-test");
        ds.setCreateDatabase("create");
        return ds;
    }

    @Test
    public void testAddCar() throws Exception {
        Car car = new Car( "4M2 3000", "Volkswagen Passat", new BigDecimal(5000), new BigDecimal(20000));
        manager.addCar(car);
        Long carId = car.getId();
        assertThat("saved car has null id",car.getId(),is(not(equalTo(null))));
        Car result = manager.getCarById(carId);


        assertThat("retrieved car differs from the saved one", result, is(equalTo(car)));
        assertThat("retrieved car is the same instance", result, is(not(sameInstance(car))));
        assertDeepEquals(car, result);
    }

    @Test
    public void testAddCarErrors() {
        Car car = new Car( "4M2 3000", "Volkswagen Passat", new BigDecimal(5000), new BigDecimal(20000));
        //when we try to add null, we should get an exception
        try {
            manager.addCar(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        //when we try to add a car with null licensePlate, we should get an exception
        car.setId(1L);
        car.setLicensePlate(null);
        try {
            manager.addCar(car);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        //when we try to add a car with null model, we should get an exception
        car.setId(1L);
        car.setModel(null);
        try {
            manager.addCar(car);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        //when we try to add a car with zero price, we should get an exception
        car.setId(1L);
        car.setPrice(new BigDecimal(0));
        try {
            manager.addCar(car);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        //when we try to add a car with <0 price, we should get an exception
        car.setId(1L);
        car.setPrice(new BigDecimal(-2));
        try {
            manager.addCar(car);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        //when we try to add a car with <0 Number of KM, we should get an exception
        car.setId(1L);
        car.setNumberOfKM(new BigDecimal(-2));
        try {
            manager.addCar(car);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
    }

    @Test
    public void testDeleteCar() throws Exception {
        // try delete method with null argument
        try {
            manager.deleteCar(null);
            fail();
        } catch (IllegalArgumentException e) {
        }
        Car car = new Car( "4M2 3000", "Volkswagen Passat", new BigDecimal(5000), new BigDecimal(20000));
        //try delete nonexistent car
        //car.setId(-1L);
//        try {
//            manager.deleteCar(car);
//            fail();
//        } catch (IllegalArgumentException e) {
//        }
        //try delete the car properly
        manager.addCar(car);
        manager.deleteCar(car);
        //check if the right car was found
        assertEquals(0,manager.getAllCars().size());
    }

    @Test
    public void testEditCar() throws Exception {
        Car car1 = new Car( "4M2 3000", "Volkswagen Passat", new BigDecimal(5000), new BigDecimal(20000));
        Car car2 = new Car( "5M1 2164", "Skoda Octavia", new BigDecimal(4000), new BigDecimal(40000));
        manager.addCar(car1);
        manager.addCar(car2);

        Long carId = car1.getId();
        car1 = manager.getCarById(carId);
        car1.setModel("Citroen Berlingo");
        manager.editCar(car1);
        assertThat("model was not changed", car1.getModel(), is(equalTo("Citroen Berlingo")));
        assertThat("license plate was changed", car1.getLicensePlate(), is(equalTo("4M2 3000")));
        assertThat("number of km was changed", car1.getNumberOfKM(), is(equalTo(new BigDecimal(20000))));

        car1 = manager.getCarById(carId);
        car1.setLicensePlate("5M1 2164");
        manager.editCar(car1);
        assertThat("license plate was not changed", car1.getLicensePlate(), is(equalTo("5M1 2164")));
        assertThat("number of km was changed", car1.getNumberOfKM(), is(equalTo(new BigDecimal(20000))));

        car1 = manager.getCarById(carId);
        car1.setNumberOfKM(new BigDecimal(40000));
        manager.editCar(car1);
       assertThat("number of km was not changed", car1.getNumberOfKM(), is(equalTo(new BigDecimal(40000))));


        assertDeepEquals(car2, manager.getCarById(car2.getId()));
    }

    @Test
    public void testGetAllCars() throws Exception {
        Car car1 = new Car( "4M2 3000", "Volkswagen Passat", new BigDecimal(5000), new BigDecimal(20000));
        Car car2 = new Car( "5M1 2164", "Skoda Octavia", new BigDecimal(4000), new BigDecimal(40000));

        manager.addCar(car1);
        manager.addCar(car2);

        List<Car> expected = Arrays.asList(car1, car2);
        List<Car> actual = manager.getAllCars();

        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);
    }

    @Test
    public void testGetCarById() throws Exception {
        // try find by null argument
        try {
            manager.getCarById(null);
            fail();
        } catch (IllegalArgumentException e) {
        }
        Car car = new Car( "4M2 3000", "Volkswagen Passat", new BigDecimal(5000), new BigDecimal(20000));
        manager.addCar(car);
        //try find car by nonexistent argument
        assertNull("license plate does not exist",manager.getCarById(Long.MAX_VALUE));


        //try find the car properly
        Long carId = car.getId();
        Car c = manager.getCarById(carId);
        //check if the right car was found
        assertEquals(car, c);
    }

    @Test
    public void testGetCarByLicensePlate() throws Exception {
        // try find by null argument
        try {
            manager.getCarByLicensePlate(null);
            fail();
        } catch (IllegalArgumentException e) {
        }
        Car car = new Car( "4M2 3000", "Volkswagen Passat", new BigDecimal(5000), new BigDecimal(20000));
        manager.addCar(car);
        //try find car by nonexistent argument

        assertNull("license plate does not exist",manager.getCarByLicensePlate(" "));


        //try find the car properly
        String carLicensePlate = car.getLicensePlate();
        Car c = manager.getCarByLicensePlate(carLicensePlate);
        //check if the right car was found
        assertEquals(car, c);
    }

    /*@Test
    public void testGetAvailabilityOfCar() throws Exception {
        // try find by null argument
        try {
            manager.getAvailabilityOfCar(null);
            fail();
        } catch (NullPointerException e) {
        }
        Car car = new Car(11L, "4M2 3000", "Volkswagen Passat", new BigDecimal(5000), new BigDecimal(20000));
        manager.addCar(car);
        System.out.println(manager.getAllCars().get(0).getId());
        //try find nonexistent car
        try {
            manager.getAvailabilityOfCar(-1L);
            fail();
        } catch (IllegalArgumentException e) {
        }
        //try find the car properly
        boolean b1 = manager.getAvailabilityOfCar(11L);
        //check if the right car was found
        assertEquals(true, b1);
    }*/




    private void assertDeepEquals(List<Car> expectedList, List<Car> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Car expected = expectedList.get(i);
            Car actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }

    private void assertDeepEquals(Car expected, Car actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getLicensePlate(), actual.getLicensePlate());
        assertEquals(expected.getNumberOfKM(), actual.getNumberOfKM());
        assertEquals(expected.getModel(), actual.getModel());
        assertEquals(expected.getPrice(), actual.getPrice());

    }
    private static Comparator<Car> idComparator = new Comparator<Car>() {

        public int compare(Car o1, Car o2) {
            return Long.valueOf(o1.getId()).compareTo(Long.valueOf(o2.getId()));
        }
    };


}