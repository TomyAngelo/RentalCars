package PV168;


import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.sql.*;
import java.util.List;


/**
 * Created by TomyAngelo on 18. 3. 2016.
 */
public class LeaseManagerImpl implements LeaseManager {

    private DataSource dataSource;
    private CustomerManagerImpl customerManager;
    private CarManagerImpl carManager;

    public LeaseManagerImpl(DataSource dataSource) {
        setDataSource(dataSource);
        customerManager = new CustomerManagerImpl(dataSource);
        carManager = new CarManagerImpl(dataSource);
    }


    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }

    @Override
    public void createLease(Lease lease) {
        checkDataSource();
        validateLease(lease);

        if(lease.getId() != null){
            throw new IllegalArgumentException("ID should be null");
        }

        if(lease.getDateTo().isBefore(lease.getDateFrom()) ){
            throw new IllegalArgumentException("Date to should not be before date from");
        }


        if(lease.getCar().getIsBorrowed()){
            throw new IllegalArgumentException("Car is already borrowed");
        }



        Connection conn = null;
        PreparedStatement st = null;
        try{
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement("INSERT INTO LEASES (IDCUSTOMER, IDCAR, DATEFROM, DATETO, PRICE) VALUES (?,?,?,?,?)",
                     Statement.RETURN_GENERATED_KEYS);

            st.setLong(1, lease.getCustomer().getId());
            st.setLong(2, lease.getCar().getId());
            st.setDate(3, toSqlDate(lease.getDateFrom()));
            st.setDate(4, toSqlDate(lease.getDateTo()));

            try {
                st.setBigDecimal(5, lease.getPrice().setScale(2));
            } catch (ArithmeticException ex){
                throw new ServiceFailureException("bad BigDecimal value");
            }

            int addedRows = st.executeUpdate();
            DBUtils.checkUpdatesCount(addedRows,lease,true);

            ResultSet keyRS = st.getGeneratedKeys();
            lease.setId(getKey(keyRS, lease));
            lease.getCar().setIsBorrowed(true);
            conn.commit();
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when creating cars", ex);
        } finally{
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn,st);
        }
    }

    private Long getKey(ResultSet keyRS, Lease lease) throws SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retrieving failed when trying to insert lease " + lease
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retrieving failed when trying to insert lease " + lease
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retrieving failed when trying to insert lease " + lease
                    + " - no key found");
        }
    }



    @Override
    public Lease getLeaseByID(Long id) {
        checkDataSource();
        if(id == null){
            throw new IllegalArgumentException("id is null");
        }

        if(id < 0){
            throw new IllegalArgumentException("id is negative");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try{
            conn = dataSource.getConnection();
            st = conn.prepareStatement("SELECT ID, IDCUSTOMER, IDCAR, DATEFROM, DATETO, PRICE FROM LEASES WHERE ID=?");
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();
            if(rs.next()){
                Lease lease = resultSetToLease(rs);
                if (rs.next()) {
                    throw new ServiceFailureException(
                            "Internal error: More entities with the same id found "
                                    + "(source id: " + id + ", found " + lease + " and " + resultSetToLease(rs));
                }
                return lease;
            }else{
                return null;
            }
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when retrieving lease by id", ex);
        } finally{
            DBUtils.closeQuietly(conn,st);
        }
    }

    private Lease resultSetToLease(ResultSet rs) throws SQLException {
        Lease lease= new Lease();
        lease.setId(rs.getLong("ID"));
        lease.setCustomer(customerManager.findCustomerById(rs.getLong("IDCUSTOMER")));
        lease.setCar(carManager.getCarById (rs.getLong("IDCAR")));
        lease.setDateFrom(toLocalDate(rs.getDate("DATEFROM")));
        lease.setDateTo(toLocalDate(rs.getDate("DATETO")));
        try {
            lease.setPrice(rs.getBigDecimal("PRICE"));
        } catch (ArithmeticException ex) {
            throw new ServiceFailureException("bad BigDecimal value");
        }
        return lease;
    }

    @Override
    public List<Lease> getAllLeases() {
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;
        try{
            conn = dataSource.getConnection();
            st = conn.prepareStatement("SELECT ID, IDCUSTOMER, IDCAR, DATEFROM, DATETO, PRICE FROM LEASES");
            ResultSet rs = st.executeQuery();
            List<Lease> leases= new ArrayList<>();
            while(rs.next()){
                leases.add(resultSetToLease(rs));
            }
            return leases;
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when retrieving all leases", ex);
        }finally{
            DBUtils.closeQuietly(conn,st);
        }
    }

    @Override
    public List<Lease> getAllLeasesByEndDate(LocalDate endDate) {
        checkDataSource();
        if(endDate == null){
            throw new IllegalArgumentException("end lease is null");
        }

        Connection conn = null;
        PreparedStatement st = null;
        try{
            conn = dataSource.getConnection();
            st = conn.prepareStatement("SELECT ID, IDCUSTOMER, IDCAR, DATEFROM, DATETO, PRICE FROM LEASES WHERE DATETO=?");
            st.setDate(1, toSqlDate(endDate));
            ResultSet rs = st.executeQuery();
            List<Lease> leases= new ArrayList<>();
            while(rs.next()){
                leases.add(resultSetToLease(rs));
            }
            return leases;
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when retrieving lease for customer", ex);
        }
    }

    @Override
    public List<Lease> findLeasesForCustomer(Customer customer) {
        checkDataSource();
        if(customer == null){
            throw new IllegalArgumentException("customer is null");
        }

        if(customer.getId() == null){
            throw new IllegalArgumentException("customer id is null");
        }

        Connection conn = null;
        PreparedStatement st = null;
        try{
            conn = dataSource.getConnection();
            st = conn.prepareStatement("SELECT ID, IDCUSTOMER, IDCAR, DATEFROM, DATETO, PRICE FROM LEASES WHERE IDCUSTOMER=?");
            st.setLong(1, customer.getId());
            ResultSet rs = st.executeQuery();
            List<Lease> leases= new ArrayList<>();
            while(rs.next()){
                leases.add(resultSetToLease(rs));
            }
            return leases;
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when retrieving lease for customer", ex);
        }finally {
            DBUtils.closeQuietly(conn,st);
        }
    }

    @Override
    public List<Lease> findLeasesForCar(Car car) {
        checkDataSource();
        if(car == null){
            throw new IllegalArgumentException("car is null");
        }

        if(car.getId() == null){
            throw new IllegalArgumentException("car id is null");
        }

        Connection conn = null;
        PreparedStatement st = null;
        try{
            conn = dataSource.getConnection();
            st = conn.prepareStatement("SELECT ID, IDCUSTOMER, IDCAR, DATEFROM, DATETO, PRICE FROM LEASES WHERE IDCAR=?");
            st.setLong(1, car.getId());
            ResultSet rs = st.executeQuery();
            List<Lease> leases= new ArrayList<>();
            while(rs.next()){
                leases.add(resultSetToLease(rs));
            }
            return leases;
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when retrieving lease for car", ex);
        }finally {
            DBUtils.closeQuietly(conn,st);
        }
    }

    @Override
    public void updateLease(Lease lease) {
        checkDataSource();
        validateLease(lease);
        if (lease.getId() == null) {
            throw new IllegalArgumentException("lease id is null");
        }

        if(lease.getDateFrom() == null){
            throw new IllegalArgumentException("Date from is null");
        }

        if(lease.getDateTo().isBefore(lease.getDateFrom())){
            throw new IllegalArgumentException("Date to is after real end date.");
        }

        Connection conn = null;
        PreparedStatement st = null;
        try{
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement("UPDATE LEASES SET IDCUSTOMER=?, IDCAR=?, DATEFROM=?, DATETO=?, PRICE=? WHERE id=?");
            st.setLong(1, lease.getCustomer().getId());
            st.setLong(2, lease.getCar().getId());
            st.setDate(3, toSqlDate(lease.getDateFrom()));
            st.setDate(4, toSqlDate(lease.getDateTo()));

            try {
                st.setBigDecimal(5, lease.getPrice().setScale(2));
            } catch (ArithmeticException ex){
                throw new ServiceFailureException("bad BigDecimal value");
            }

            st.setLong(6, lease.getId());
            if(st.executeUpdate() != 1) {
                throw new IllegalArgumentException("lease with id=" + lease.getId() + " do not exist");
            }
            conn.commit();
        } catch(SQLException ex) {
            throw new ServiceFailureException("Error when updating lease", ex);
        }finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn,st);
        }
    }

    private boolean checkCarID(Long id, Car car) {
        if(car == null){
            throw new IllegalArgumentException("car is null");
        }

        if(car.getId() == null){
            throw new IllegalArgumentException("car id is null");
        }

        Connection conn = null;
        PreparedStatement st = null;
        try{
            conn = dataSource.getConnection();
            st = conn.prepareStatement("SELECT ID, IDCAR FROM LEASES WHERE IDCAR=? ");
            st.setLong(1, car.getId());
            ResultSet rs = st.executeQuery();

            if(rs.next()){
                return id != rs.getLong("ID");
            }else{
                return false;
            }

        } catch (SQLException ex){
            throw new ServiceFailureException("Error when lease for car", ex);
        }finally {
            DBUtils.closeQuietly(conn,st);
        }
    }

    @Override
    public void deleteLease(Lease lease) {
        checkDataSource();
        if(lease == null){
            throw new IllegalArgumentException("lease is null");
        }

        if(lease.getId() == null){
            throw new IllegalArgumentException("lease id is null");
        }

        Connection conn = null;
        PreparedStatement st = null;
        try{
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement("DELETE FROM LEASES WHERE id=?");
            st.setLong(1, lease.getId());
            if(st.executeUpdate() != 1) {
                throw new IllegalArgumentException("lease with id=" + lease.getId() + " do not exist");
            }
            lease.getCar().setIsBorrowed(false);
            conn.commit();
        } catch(SQLException ex) {
            throw new ServiceFailureException("Error when deleting lease", ex);
        }finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn,st);
        }
    }



    public void deleteAllLeases() {
        Collection<Lease> leases = new ArrayList<>(getAllLeases());
        for(Lease l : leases) {
            deleteLease(l);
        }
    }

    private void validateLease(Lease lease) {
        if(lease==null){
            throw new IllegalArgumentException("Lease is null");
        }

        if(lease.getCustomer() == null){
            throw new IllegalArgumentException("Customer is null");
        }

        if(lease.getCustomer().getId() == null){
            throw new IllegalArgumentException("Customer id is null");
        }

        if(lease.getCustomer().getId() < 0 ){
            throw new IllegalArgumentException("Customer id is negative");
        }

        if(lease.getCar() == null){
            throw new IllegalArgumentException("Car is null");
        }

        if(lease.getCar().getId() == null){
            throw new IllegalArgumentException("Car id is null");
        }

        if(lease.getCar().getId() < 0 ){
            throw new IllegalArgumentException("Car id is negative");
        }

        if(lease.getPrice().compareTo(new BigDecimal(0)) < 0){
            throw new IllegalArgumentException("Lease price is negative");
        }

        if(lease.getPrice() == null){
            throw new IllegalArgumentException("Lease price is null");
        }

        if(lease.getDateTo() == null){
            throw new IllegalArgumentException("To date is null");
        }
    }

    private static Date toSqlDate(LocalDate localDate) {
        return localDate == null ? null : Date.valueOf(localDate);
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
