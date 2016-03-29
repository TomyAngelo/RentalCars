package PV168;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
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

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }

    public void createCustomer(Customer customer) {
        checkDataSource();
        if(customer==null){
            throw new IllegalArgumentException("Customer is null");
        }

        if(customer.getId()!=null) {
            throw new IllegalArgumentException("Customer is already in DB");
        }

        validate(customer);
        Connection conn = null;
        PreparedStatement st = null;
        try{
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement("INSERT INTO CUSTOMERS (NAME,ADDRESS,PHONENUMBER) VALUES (?,?,?)",Statement.RETURN_GENERATED_KEYS);
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
            conn.commit();
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when inserting customer " + customer, ex);
        }finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn,st);
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
        checkDataSource();
        if (customer == null) {
            throw new IllegalArgumentException("Customer is null");
        }
        if (customer.getId() == null) {
            throw new IllegalArgumentException("Customer isn't in DB");
        }
        validate(customer);

        Connection conn = null;
        PreparedStatement st = null;
        try{
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement("UPDATE CUSTOMERS SET NAME = ?, ADDRESS = ?,  PHONENUMBER = ? WHERE ID = ?");
            st.setString(1, customer.getName());
            st.setString(2, customer.getAddress());
            st.setString(3, customer.getPhoneNumber());
            st.setLong(4, customer.getId());

            int count = st.executeUpdate();
                if (count != 1){
                    throw new IllegalArgumentException("customer id not found");
                }
            conn.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Error when updating customer from DB", ex);
        }finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn,st);
        }
    }

    public void deleteCustomer(Customer customer) {
        checkDataSource();
        if (customer == null) {
            throw new IllegalArgumentException("Customer is null");
        }
        if (customer.getId() == null) {
            throw new IllegalArgumentException("Customer isn't in DB");
        }

        Connection conn = null;
        PreparedStatement st = null;
        try{
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement("DELETE FROM CUSTOMERS WHERE id=?");
            st.setLong(1, customer.getId());
            if (st.executeUpdate() == 0) {
                throw new IllegalArgumentException("customer not found");
            }
            conn.commit();
        } catch (SQLException ex) {
            throw new RuntimeException("Error when deleting customer from DB", ex);
        }finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn,st);
        }
    }

    public void deleteAllCustomers(){
        Collection<Customer> customers = new ArrayList<>(getAllCustomers());
        for(Customer c : customers) {
            deleteCustomer(c);
        }
    }

    public Customer findCustomerById(Long id) {
        checkDataSource();
        if(id==null){
            throw new IllegalArgumentException("argumentis null");
        }
        if(id < 0){
            throw new IllegalArgumentException("id is negative ");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try{
            conn = dataSource.getConnection();
            st = conn.prepareStatement("SELECT ID,NAME,ADDRESS,PHONENUMBER FROM CUSTOMERS WHERE ID = ?");

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
        }finally {
            DBUtils.closeQuietly(conn,st);
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
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;
        try{
            conn = dataSource.getConnection();
            st = conn.prepareStatement("SELECT ID,NAME,ADDRESS,PHONENUMBER FROM CUSTOMERS");

            ResultSet rs = st.executeQuery();

            List<Customer> result = new ArrayList<>();
            while (rs.next()) {
                result.add(resultSetToCustomer(rs));
            }
            return result;

        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when retrieving all customers", ex);
        }finally {
            DBUtils.closeQuietly(conn,st);
        }
    }

    private void validate(Customer customer){

        if (customer.getAddress()==null || customer.getAddress().isEmpty()) {
            throw new IllegalArgumentException("Customer address is null or empty");
        }
        if (customer.getName()==null || customer.getName().isEmpty()) {
            throw new IllegalArgumentException("Customer names is null or empty");
        }
        if (customer.getPhoneNumber()==null || customer.getPhoneNumber().isEmpty()  ) {
            throw new IllegalArgumentException("Customer phone number is null or empty");
        }

    }
}
