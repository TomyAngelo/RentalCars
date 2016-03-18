package PV168;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/**
 * Created by TomyAngelo on 9. 3. 2016.
 */
public class CustomerManagerImpl implements CustomerManager {
    //final static Logger log = LoggerFactory.getLogger(GraveManagerImpl.class);

    private final DataSource dataSource;

    public CustomerManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public void createCustomer(Customer customer) {

        if (customer == null) {
            throw new IllegalArgumentException("Customer is null");
        }
        if (customer.getId() != null) {
            throw new IllegalArgumentException("Customer id is not null");
        }
        if (customer.getAddress().isEmpty() ) {
            throw new IllegalArgumentException("Customer address is empty");
        }
        if (customer.getAddress()==null ) {
            throw new IllegalArgumentException("Customer address is null");
        }
        if (customer.getName().isEmpty() ) {
            throw new IllegalArgumentException("Customer names is empty");
        }
        if (customer.getName()==null ) {
            throw new IllegalArgumentException("Customer names is null");
        }
        if (customer.getPhoneNumber().isEmpty() ) {
            throw new IllegalArgumentException("Customer phone number is empty");
        }
        if (customer.getPhoneNumber()==null ) {
            throw new IllegalArgumentException("Customer phone number is null");
        }

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "INSERT INTO CUSTOMER (name,address,phoneNumber) VALUES (?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {

            st.setString(1, customer.getName());
            st.setString(2, customer.getAddress());
            st.setString(3, customer.getPhoneNumber());
            int addedRows = st.executeUpdate();
            if (addedRows != 1) {
                throw new ServiceFailureException("Internal Error: More rows ("
                        + addedRows + ") inserted when trying to insert customer " + customer);
            }

            ResultSet keyRS = st.getGeneratedKeys();
            customer.setId(getKey(keyRS, customer));

        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when inserting customer " + customer, ex);
        }
    }

    private Long getKey(ResultSet keyRS, Customer customer) throws ServiceFailureException, SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retrieving failed when trying to insert customer " + customer
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retrieving failed when trying to insert customer " + customer
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retrieving failed when trying to insert customer " + customer
                    + " - no key found");
        }
    }

    public void updateCustomer(Customer customer) {

    }

    public void deleteCustomer(Customer customer) {

    }

    public Customer findCustomerById(Long id) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT id,name,address,phoneNumber FROM customer WHERE id = ?")) {

            st.setLong(1, id);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                Customer customer = resultSetToCustomer(rs);

                if (rs.next()) {
                    throw new ServiceFailureException(
                            "Internal error: More entities with the same id found "
                                    + "(source id: " + id + ", found " + customer + " and " + resultSetToCustomer(rs));
                }

                return customer;
            } else {
                return null;
            }

        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when retrieving grave with id " + id, ex);
        }
    }

    private Customer resultSetToCustomer(ResultSet rs) throws SQLException{
        Customer customer = new Customer();
        customer.setId(rs.getLong("id"));
        customer.setName(rs.getString("name"));
        customer.setAddress(rs.getString("address"));
        customer.setPhoneNumber(rs.getString("phoneNumber"));

        return customer;
    }

    public List<Customer> getAllCustomers() {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT id,name,address,phoneNumber FROM customer")) {

            ResultSet rs = st.executeQuery();

            List<Customer> result = new ArrayList<>();
            while (rs.next()) {
                result.add(resultSetToCustomer(rs));
            }
            return result;

        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when retrieving all customers", ex);
        }
    }
}
