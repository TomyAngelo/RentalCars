package PV168;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Created by TomyAngelo on 9. 3. 2016.
 */
public class CustomerManagerTest {

    private CustomerManagerImpl manager;
    private DataSource dataSource;

    @Before
    public void setUp() throws Exception {
        dataSource = prepareDataSource();
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("CREATE TABLE CUSTOMERS ("
                    + "id bigint primary key generated always as identity,"
                    + "name varchar(255),"
                    + "address varchar(255),"
                    + "phoneNumber varchar(255))").executeUpdate();
        }
        manager = new CustomerManagerImpl(dataSource);
    }

    @Rule
    // attribute annotated with @Rule annotation must be public :-(
    public ExpectedException expectedException = ExpectedException.none();

    @After
    public void tearDown() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("DROP TABLE CUSTOMERS").executeUpdate();
        }
    }

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        //we will use in memory database
        ds.setDatabaseName("memory:customermgr-test");
        ds.setCreateDatabase("create");
        return ds;
    }

    @Test
    public void createCustomer(){
        Customer customer = new Customer("Tomy","Brno 102", "0944999777");
        manager.createCustomer(customer);

        Long customerId = customer.getId();
        assertThat("saved customer has null id",customer.getId(),is(not(equalTo(null))));
        Customer result = manager.findCustomerById(customerId);

        assertThat("retrieved customer differs from the saved one", result, is(equalTo(customer)));
        assertThat("retrieved customer is the same instance", result, is(not(sameInstance(customer))));
        assertDeepEquals(customer, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithNull() throws Exception {
        manager.createCustomer(null);
    }

    @Test
    public void createCustomerWithWrongArguments(){
        Customer customer = new Customer("Tomy","Brno 102", "0944999777");
        customer.setId(1L);
        try{
            manager.createCustomer(customer);
            fail("should refuse");
        }catch(IllegalArgumentException ex){
            //OK
        }

        customer = new Customer( null , "Brno 102" , "0944999777");
        try{
            manager.createCustomer(customer);
            fail("name cannot be null");
        }catch(IllegalArgumentException ex){
            //OK
        }

        customer = new Customer( "" , "Brno 102" , "0944999777");
        try{
            manager.createCustomer(customer);
            fail("name cannot be empty");
        }catch(IllegalArgumentException ex){
            //OK
        }

        customer = new Customer( "Tomy" , null , "0944999777");
        try{
            manager.createCustomer(customer);
            fail("address cannot be null");
        }catch(IllegalArgumentException ex){
            //OK
        }

        customer = new Customer( "Tomy" , "" , "0944999777");
        try{
            manager.createCustomer(customer);
            fail("address cannot be empty");
        }catch(IllegalArgumentException ex){
            //OK
        }

        customer = new Customer( "Tomy" , "Brno 102" , null);
        try{
            manager.createCustomer(customer);
            fail("phoneNumber cannot be null");
        }catch(IllegalArgumentException ex){
            //OK
        }

        customer = new Customer( "Tomy" , "Brno 102" , "");
        try{
            manager.createCustomer(customer);
            fail("phoneNumber cannot be empty");
        }catch(IllegalArgumentException ex){
            //OK
        }
    }


    @Test
    public void updateCustomer(){
        Customer cus1 = new Customer("Tomy","Brno 102", "0944999777");
        Customer cus2 = new Customer("Angelo","Brno 64", "0933888555");
        manager.createCustomer(cus1);
        manager.createCustomer(cus2);
        Long customerId = cus1.getId();

        cus1 = manager.findCustomerById(customerId);
        cus1.setAddress("Kosice 21");
        manager.updateCustomer(cus1);
        assertThat("address was not changed", cus1.getAddress(), is(equalTo("Kosice 21")));
        assertThat("name was changed", cus1.getName(), is(equalTo("Tomy")));
        assertThat("phone number was changed", cus1.getPhoneNumber(), is(equalTo("0944999777")));

        cus1 = manager.findCustomerById(customerId);
        cus1.setName("Paul");
        manager.updateCustomer(cus1);
        assertThat("address was changed", cus1.getAddress(), is(equalTo("Kosice 21")));
        assertThat("name was not changed", cus1.getName(), is(equalTo("Paul")));
        assertThat("phone number was changed", cus1.getPhoneNumber(), is(equalTo("0944999777")));

        cus1 = manager.findCustomerById(customerId);
        cus1.setPhoneNumber("0911123654");
        manager.updateCustomer(cus1);
        assertThat("address was changed", cus1.getAddress(), is(equalTo("Kosice 21")));
        assertThat("name was  changed", cus1.getName(), is(equalTo("Paul")));
        assertThat("phone number was not changed", cus1.getPhoneNumber(), is(equalTo("0911123654")));


        assertDeepEquals(cus2, manager.findCustomerById(cus2.getId()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateWithNull() throws Exception {
        manager.updateCustomer(null);
    }

    @Test
    public void updateCustomerWithWrongAttributes() {
        Customer customer = new Customer("Tomy","Brno 102", "0944999777");
        manager.createCustomer(customer);
        Long customerId = customer.getId();

        try {
            customer = manager.findCustomerById(customerId);
            customer.setId(null);
            manager.updateCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            customer = manager.findCustomerById(customerId);
            customer.setId(customerId - 1);
            manager.updateCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            customer = manager.findCustomerById(customerId);
            customer.setAddress(null);
            manager.updateCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            customer = manager.findCustomerById(customerId);
            customer.setAddress("");
            manager.updateCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            customer = manager.findCustomerById(customerId);
            customer.setName(null);
            manager.updateCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            customer = manager.findCustomerById(customerId);
            customer.setName("");
            manager.updateCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            customer = manager.findCustomerById(customerId);
            customer.setPhoneNumber(null);
            manager.updateCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            customer = manager.findCustomerById(customerId);
            customer.setPhoneNumber("");
            manager.updateCustomer(customer);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

    }

    @Test
    public void deleteCustomer() {

        Customer cus1 = new Customer("Tomy","Brno 102", "0944999777");
        Customer cus2 = new Customer("Angelo","Brno 64", "0933888555");
        manager.createCustomer(cus1);
        manager.createCustomer(cus2);

        assertNotNull(manager.findCustomerById(cus1.getId()));
        assertNotNull(manager.findCustomerById(cus2.getId()));

        manager.deleteCustomer(cus1);

        assertNull(manager.findCustomerById(cus1.getId()));
        assertNotNull(manager.findCustomerById(cus2.getId()));
    }

    @Test
    public void deleteCustomerWithWrongAttributes() {

        Customer cus1 = new Customer("Tomy","Brno 102", "0944999777");

        try {
            manager.deleteCustomer(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            cus1.setId(null);
            manager.deleteCustomer(cus1);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

        try {
            cus1.setId(1L);
            manager.deleteCustomer(cus1);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }

    }



    @Test
    public void getAllCustomers(){
        assertTrue(manager.getAllCustomers().isEmpty());

        Customer cus1 = new Customer("Tomy","Brno 102", "0944999777");
        Customer cus2 = new Customer("Angelo","Brno 64", "0933888555");

        manager.createCustomer(cus1);
        manager.createCustomer(cus2);

        List<Customer> expected = Arrays.asList(cus1, cus2);
        List<Customer> actual = manager.getAllCustomers();

        Collections.sort(actual, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals("saved and retrieved customers differ", expected, actual);
        assertDeepEquals(expected, actual);
    }





    private void assertDeepEquals(List<Customer> expectedList, List<Customer> actualList) {
        for (int i = 0; i < expectedList.size(); i++) {
            Customer expected = expectedList.get(i);
            Customer actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }

    private void assertDeepEquals(Customer expected, Customer actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getAddress(), actual.getAddress());
        assertEquals(expected.getPhoneNumber(), actual.getPhoneNumber());
    }

    private static Comparator<Customer> idComparator = new Comparator<Customer>() {
        public int compare(Customer o1, Customer o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };
}