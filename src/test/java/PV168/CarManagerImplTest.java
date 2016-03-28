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
            connection.prepareStatement("CREATE TABLE CUSTOMER ("
                    + "id bigint primary key generated always as identity,"
                    + "name varchar(255),"
                    + "address varchar(255),"
                    + "phoneNumber varchar(255))").executeUpdate();
        }
        manager = new CarManagerImpl(dataSource);
    }

    @Rule
    // attribute annotated with @Rule annotation must be public :-(
    public ExpectedException expectedException = ExpectedException.none();

    @After
    public void tearDown() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("DROP TABLE CUSTOMER").executeUpdate();
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
        Car car = new Car(11L, "4M2 3000", "Volkswagen Passat", new BigDecimal(5000), new BigDecimal(20000));
        //we try to add a correct car
        manager.addCar(car);
        Long carId = car.getId();
        //now id should not be null
        assertNotNull(carId);
        //we try to get the object back
        Car result = manager.getCarById(carId);
        //should be the same
        assertEquals(car, result);
        //all the attributes should be the same
        assertEquals(car.getLicensePlate(), result.getLicensePlate());
        assertEquals(car.getId(), result.getId());
        assertEquals(car.getModel(), result.getModel());
        assertEquals(car.getPrice(), result.getPrice());
        assertEquals(car.getNumberOfKM(), result.getNumberOfKM());
    }

    @Test
    public void testAddCarErrors() {
        Car car = new Car(11L, "4M2 3000", "Volkswagen Passat", new BigDecimal(5000), new BigDecimal(20000));
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
        } catch (NullPointerException e) {
        }
        Car car = new Car(11L, "4M2 3000", "Volkswagen Passat", new BigDecimal(5000), new BigDecimal(20000));
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
        Car car1 = new Car(11L, "4M2 3000", "Volkswagen Passat", new BigDecimal(5000), new BigDecimal(20000));
        Car car2 = new Car(20L, "5M1 2164", "Skoda Octavia", new BigDecimal(4000), new BigDecimal(40000));
        manager.addCar(car1);
        // try edit method with null argument
        try {
            manager.editCar(null, car1);
            fail();
        } catch (NullPointerException e) {
        }
        try {
            manager.editCar(11L, null);
            fail();
        } catch (NullPointerException e) {
        }
        try {
            manager.editCar(null, null);
            fail();
        } catch (NullPointerException e) {
        }
        //try edit nonexistent car
        try {
            manager.editCar(-1L, car1);
            fail();
        } catch (IllegalArgumentException e) {
        }
        //try edit the car properly
        manager.editCar(11L, car2);
        car2.setId(11L);
        //check if the right car was found
        assertEquals(car2, manager.getCarById(11L));
    }

    @Test
    public void testGetAllCars() throws Exception {
        Car car1 = new Car(11L, "4M2 3000", "Volkswagen Passat", new BigDecimal(5000), new BigDecimal(20000));
        Car car2 = new Car(20L, "5M1 2164", "Skoda Octavia", new BigDecimal(4000), new BigDecimal(40000));

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
        } catch (NullPointerException e) {
        }
        Car car = new Car(11L, "4M2 3000", "Volkswagen Passat", new BigDecimal(5000), new BigDecimal(20000));
        manager.addCar(car);
        //try find car by nonexistent argument
        try {
            manager.getCarById(1208L);
            fail();
        } catch (IllegalArgumentException e) {
        }
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
        } catch (NullPointerException e) {
        }
        Car car = new Car(11L, "4M2 3000", "Volkswagen Passat", new BigDecimal(5000), new BigDecimal(20000));
        manager.addCar(car);
        //try find car by nonexistent argument
        try {
            manager.getCarByLicensePlate(" ");
            fail();
        } catch (IllegalArgumentException e) {
        }
        //try find the car properly
        String carLicensePlate = car.getLicensePlate();
        Car c = manager.getCarByLicensePlate(carLicensePlate);
        //check if the right car was found
        assertEquals(car, c);
    }

    @Test
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
    }




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