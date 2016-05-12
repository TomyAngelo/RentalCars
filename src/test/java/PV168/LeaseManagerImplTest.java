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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created by TomyAngelo on 18. 3. 2016.
 */
public class LeaseManagerImplTest {

    private LeaseManagerImpl manager;
    private CustomerManagerImpl managercust;
    private CarManagerImpl managercar;
    private DataSource ds;
    private static Car car1;
    private static Car car2;
    private static Car car3;
    private static Car car4;

    private static Customer customer1;
    private static Customer customer2;
    private static Customer customer3;

    public LeaseManagerImplTest() {
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws SQLException {
         car1 = new Car( "1A1 2547", "Audi A3", new BigDecimal(450), new BigDecimal(23000));
         car2 = new Car( "1B3 3546",  "BMW X6", new BigDecimal(340),new BigDecimal(45000));
         car3 = new Car( "1T5 6784", "VW PASSAT", new BigDecimal(467), new BigDecimal(57000));
         car4 = new Car( "1C3 9809",  "SKODA FABIA", new BigDecimal(450), new BigDecimal(10000));

        customer1 = new Customer("Michal Vitek", "Vajanskeho 47", "+420746654738");
        customer2 = new Customer("Jozko Voracek", "Tlusteho 47", "+420733456980");
        customer3 = new Customer("David Konecny", "Lokalni 23", "+420723434580");

        ds = prepareDataSource();
        try (Connection connection = ds.getConnection()) {

            connection.prepareStatement("CREATE TABLE CUSTOMERS ("
                    + "ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                    + "NAME VARCHAR(255) NOT NULL,"
                    + "ADDRESS VARCHAR(255) NOT NULL,"
                    + "PHONENUMBER VARCHAR(255) NOT NULL)").executeUpdate();

            connection.prepareStatement("CREATE TABLE CARS ("
                   + "ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                   + "LICENSEPLATE VARCHAR(255) NOT NULL,"
                   + "MODEL VARCHAR(255) NOT NULL,"
                   + "PRICE DECIMAL,"
                   + "NUMBEROFKM DECIMAL)").executeUpdate();

            connection.prepareStatement("CREATE TABLE LEASES ("
                    + "ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                    + "IDCUSTOMER BIGINT REFERENCES CUSTOMERS(ID) ON DELETE CASCADE,"
                    + "IDCAR BIGINT REFERENCES CARS(ID) ON DELETE CASCADE,"
                    + "DATEFROM DATE,"
                    + "DATETO DATE,"
                    + "PRICE DECIMAL)").executeUpdate();
        }
        manager = new LeaseManagerImpl( ds);
        managercar=new CarManagerImpl(ds);
        managercust=new CustomerManagerImpl(ds);
    }


    @After
    public void tearDown() throws SQLException {
        try (Connection connection = ds.getConnection()) {
            connection.prepareStatement("DROP TABLE LEASES").executeUpdate();
            connection.prepareStatement("DROP TABLE CUSTOMERS").executeUpdate();
            connection.prepareStatement("DROP TABLE CARS").executeUpdate();

        }
        //DBUtils.executeSqlScript(ds,LeaseManager.class.getResource("dropTables.sql"));
    }

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        ds.setDatabaseName("memory:leasemgr-test");
        ds.setCreateDatabase("create");
        return ds;
    }

    @Test
    public void testCreateLease() throws Exception {
        Lease rent = new Lease();
        rent.setPrice(new BigDecimal(12000));
        rent.setDateFrom(LocalDate.of(2017,3,25));
        rent.setDateTo(LocalDate.of(2017,3,27));
        rent.setCustomer(customer1);
        rent.setCar(car1);
    }

    @Test
    public void testGetLeaseByID() throws Exception {
        managercust.createCustomer(customer1);

        managercar.addCar(car1);

        Lease lease1 = createLease1();


        manager.createLease(lease1);

        Lease l1 = manager.getAllLeases().get(0);
        long id = l1.getId();

        assertNotNull(id);

        assertEquals(l1,manager.getLeaseByID(id));
        assertDeepEquals(l1,manager.getLeaseByID(id));
    }

    @Test
    public void testGetAllLeases() throws Exception {
        manager.deleteAllLeases();
        managercar.deleteAllCars();
        managercust.deleteAllCustomers();

        managercust.createCustomer(customer1);
        managercust.createCustomer(customer2);

        managercar.addCar(car1);
        managercar.addCar(car2);
        managercar.addCar(car3);


        Lease lease1 = createLease1();
        Lease lease2 = createLease2();


        manager.createLease(lease1);
        manager.createLease(lease2);

        List<Lease> expected = Arrays.asList(lease1, lease2);
        List<Lease> actual = manager.getAllLeases();

        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);
    }

    @Test
    public void testGetAllLeasesByEndDate() throws Exception {
        manager.deleteAllLeases();
        managercar.deleteAllCars();
        managercust.deleteAllCustomers();

        managercust.createCustomer(customer1);
        managercust.createCustomer(customer2);

        managercar.addCar(car1);
        managercar.addCar(car2);

        Lease lease1 = createLease1();

        manager.createLease(lease1);
        Long leaseId = lease1.getId();
        //now id should not be null
        assertNotNull(leaseId);
        //we try to get the object back
        Lease result = manager.getLeaseByID(leaseId);
        //should be the same
        assertEquals(lease1, result);

        assertDeepEquals(lease1, result);

        List<Lease> expected = Arrays.asList(lease1);
        List<Lease> actual = manager.getAllLeases();

        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);


        Lease lease2 = createLease2();
        manager.createLease(lease2);
        LocalDate endDate = lease2.getDateTo();
        List<Lease> res = manager.getAllLeasesByEndDate(endDate);

        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);
        //should be the same
        assertEquals(lease2, res.get(0));

        assertDeepEquals(lease2, res.get(0));

        expected = Arrays.asList(lease1, lease2);
        actual = manager.getAllLeases();

        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);
    }

    @Test
    public void testFindLeasesForCustomer() throws Exception {
        manager.deleteAllLeases();
        managercar.deleteAllCars();
        managercust.deleteAllCustomers();

        managercust.createCustomer(customer1);
        managercust.createCustomer(customer2);

        managercar.addCar(car1);
        managercar.addCar(car2);

        Lease lease1 = createLease1();

        manager.createLease(lease1);
        Long leaseId = lease1.getId();
        //now id should not be null
        assertNotNull(leaseId);
        //we try to get the object back
        Lease result = manager.getLeaseByID(leaseId);
        //should be the same
        assertEquals(lease1, result);

        assertDeepEquals(lease1, result);

        List<Lease> expected = Arrays.asList(lease1);
        List<Lease> actual = manager.getAllLeases();

        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);


        Lease lease2 = createLease2();
        manager.createLease(lease2);
        Customer leaseCustomer = lease2.getCustomer();
        List<Lease> res = manager.findLeasesForCustomer(leaseCustomer);
        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);
        //should be the same
        assertEquals(lease2, res.get(0));

        assertDeepEquals(lease2, res.get(0));

        expected = Arrays.asList(lease1, lease2);
        actual = manager.getAllLeases();

        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);
    }

    @Test
    public void testFindLeasesForCar() throws Exception {
        manager.deleteAllLeases();
        managercar.deleteAllCars();
        managercust.deleteAllCustomers();

        managercust.createCustomer(customer1);
        managercust.createCustomer(customer2);

        managercar.addCar(car1);
        managercar.addCar(car2);

        Lease lease1 = createLease1();

        manager.createLease(lease1);
        Long leaseId = lease1.getId();
        //now id should not be null
        assertNotNull(leaseId);
        //we try to get the object back
        Lease result = manager.getLeaseByID(leaseId);
        //should be the same
        assertEquals(lease1, result);

        assertDeepEquals(lease1, result);

        List<Lease> expected = Arrays.asList(lease1);
        List<Lease> actual = manager.getAllLeases();

        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);


        Lease lease2 = createLease2();
        manager.createLease(lease2);
        Customer leaseCustomer = lease2.getCustomer();
        List<Lease> res = manager.findLeasesForCustomer(leaseCustomer);

        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        //should be the same
        assertEquals(lease2, res.get(0));

        assertDeepEquals(lease2, res.get(0));

        expected = Arrays.asList(lease1, lease2);
        actual = manager.getAllLeases();

        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);
    }

    @Test
    public void testUpdateLease() throws Exception {
        managercust.createCustomer(customer1);
        managercust.createCustomer(customer2);

        managercar.addCar(car1);
        managercar.addCar(car2);
        managercar.addCar(car3);

        Lease lease1 = createLease1();
        manager.createLease(lease1);

        Lease lease2 = createLease2();
        manager.createLease(lease2);
        Long leaseId = lease1.getId();


        lease1 = manager.getLeaseByID(leaseId);
        lease1.setPrice(lease2.getPrice());
        manager.updateLease(lease1);
        assertThat("price was not changed", lease1.getPrice(), is(equalTo(lease2.getPrice())));
        assertThat("customer was changed", lease1.getCustomer(), is(equalTo(customer1)));
        assertThat("car was changed", lease1.getCar(), is(equalTo(car1)));

        lease1 = manager.getLeaseByID(leaseId);
        lease1.setCustomer(customer2);
        manager.updateLease(lease1);
        assertThat("customer was not changed", lease1.getCustomer(), is(equalTo(customer2)));
        assertThat("car was changed", lease1.getCar(), is(equalTo(car1)));

        lease1 = manager.getLeaseByID(leaseId);
        lease1.setCar(car3);
        manager.updateLease(lease1);
        assertThat("car was not changed", lease1.getCar(), is(equalTo(car3)));


        assertDeepEquals(lease2, manager.getLeaseByID(lease2.getId()));
    }

    @Test
    public void testDeleteLease() throws Exception {
        manager.deleteAllLeases();
        managercust.deleteAllCustomers();
        managercar.deleteAllCars();

        managercust.createCustomer(customer1);
        managercust.createCustomer(customer2);
        managercust.createCustomer(customer3);
        managercar.addCar(car1);
        managercar.addCar(car2);
        managercar.addCar(car3);
        managercar.addCar(car4);


        Lease lease1 = createLease1();
        Lease lease2 = createLease2();
        Lease lease3 = createLease3();


        manager.createLease(lease1);
        manager.createLease(lease2);
        manager.createLease(lease3);

        manager.deleteLease(lease1);
        Collection<Lease> leases = manager.getAllLeases();
        assertEquals("Pocet zakaznikov v databazi neni po odobrani jedneho dva", 2, leases.size());

        manager.deleteLease(lease2);
        Collection<Lease> leases2 = manager.getAllLeases();
        assertEquals("Pocet zakaznikov v databazi neni po odobrani jedneho jedna", 1, leases2.size());

        manager.deleteLease(lease3);
        Collection<Lease> leases3 = manager.getAllLeases();
        assertEquals("Pocet zakaznikov v databazi neni po odobrani jedneho nula", 0, leases3.size());

    }

    @Test
    public void testDeleteAllRents() {
        manager.deleteAllLeases();
        managercust.deleteAllCustomers();
        managercar.deleteAllCars();

        Lease lease1 = createLease1();
        Lease lease2 = createLease2();


        managercust.createCustomer(customer1);
        managercust.createCustomer(customer2);
        managercar.addCar(car1);
        managercar.addCar(car2);
        managercar.addCar(car3);

        manager.createLease(lease1);
        manager.createLease(lease2);

        manager.deleteAllLeases();
        managercar.deleteAllCars();
        managercust.deleteAllCustomers();

        Collection<Lease> leases = manager.getAllLeases();
        assertEquals("Po vymazani zakazniku z databaze neni prazdna", 0, leases.size());
    }

    @Test
    public void testAddLeaseErrors() {
        manager.deleteAllLeases();
        managercar.deleteAllCars();
        managercust.deleteAllCustomers();


        try {
            manager.createLease(null);
            fail();
        } catch (IllegalArgumentException ex) {}

    }

    private static Lease createLease1() {
        Lease lease = new Lease();
        lease.setPrice(new BigDecimal(12000));
        lease.setDateFrom(LocalDate.of(2017,3,25));
        lease.setDateTo(LocalDate.of(2017,3,27));
        lease.setCustomer(customer1);
        lease.setCar(car1);
        return lease;
    }

    private static Lease createLease2() {
        Lease lease = new Lease();
        lease.setPrice(new BigDecimal(10000));
        lease.setDateFrom(LocalDate.of(2017,2,25));
        lease.setDateTo(LocalDate.of(2017,2,27));
        lease.setCustomer(customer2);
        lease.setCar(car2);
        return lease;
    }

    private static Lease createLease3() {
        Lease lease = new Lease();
        lease.setPrice(new BigDecimal(14000));
        lease.setDateFrom(LocalDate.of(2017,3,15));
        lease.setDateTo(LocalDate.of(2017,3,19));
        lease.setCustomer(customer3);
        lease.setCar(car3);
        return lease;
    }

    private void assertDeepEquals(List<Lease> expectedList, List<Lease> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Lease expected = expectedList.get(i);
            Lease actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }

    private void assertDeepEquals(Lease expected, Lease actual) {

        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getDateFrom(), actual.getDateFrom());
        assertEquals(expected.getDateTo(), actual.getDateTo());
        assertEquals(expected.getPrice(), actual.getPrice());
        assertEquals(expected.getCustomer(), actual.getCustomer());
        assertEquals(expected.getCar(), actual.getCar());
    }


    private static Comparator<Lease> idComparator = new Comparator<Lease>() {

        public int compare(Lease o1, Lease o2) {
            return Long.valueOf(o1.getId()).compareTo(Long.valueOf(o2.getId()));
        }
    };
}