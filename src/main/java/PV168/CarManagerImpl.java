package PV168;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by jima88 on 16.3.2016.
 */
public class CarManagerImpl implements CarManager {

    private final DataSource dataSource;

    public CarManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }

    public void addCar(Car car) {
        checkDataSource();
        if (car == null){
            throw new IllegalArgumentException("car is null");
        }

        validate(car);
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            st = conn.prepareStatement("INSERT INTO CARS (LICENSEPLATE,MODEL,PRICE,NUMBEROFKM) VALUES (?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS);

            st.setString(1, car.getLicensePlate());
            st.setString(2, car.getModel());
            st.setBigDecimal(3, car.getPrice());
            st.setBigDecimal(4, car.getNumberOfKM());
            int addedRows = st.executeUpdate();
            if (addedRows != 1) {
                throw new ServiceFailureException("Internal Error: More rows ("
                        + addedRows + ") inserted when trying to insert car " + car);
            }

            ResultSet keyRS = st.getGeneratedKeys();
            car.setId(getKey(keyRS, car));
            conn.commit();

        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when inserting car " + car, ex);
        }
        finally{
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn,st);
        }
    }

    private Long getKey(ResultSet keyRS, Car car ) throws ServiceFailureException, SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retrieving failed when trying to insert car " + car
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retrieving failed when trying to insert car " + car
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retrieving failed when trying to insert car " + car
                    + " - no key found");
        }
    }

    public void deleteCar(Car car) throws ServiceFailureException {
        validate(car);

        try (Connection conn = dataSource.getConnection()){
            try(PreparedStatement st = conn.prepareStatement("DELETE FROM CARS WHERE ID = ?")){
                st.setLong(1, car.getId());
                if (st.executeUpdate() != 1) {
                    throw new ServiceFailureException("did not delete car " + car);
                }
            }
        } catch (SQLException ex){
            throw new ServiceFailureException("Error when deleting car from DB", ex);
        }

    }

    public void deleteAllCars(){
        Collection<Car> cars = new ArrayList<>(getAllCars());
        for(Car c : cars) {
            deleteCar(c);
        }
    }

    public void editCar(Car car) {
        if(car == null){
            throw new IllegalArgumentException("car is null");
        }
        validate(car);

        try (Connection conn = dataSource.getConnection()){
              try(PreparedStatement st = conn.prepareStatement("UPDATE CARS SET LICENSEPLATE = ?, NUMBEROFKM = ?, MODEL = ?, PRICE = ? WHERE ID = ?")) {
                  st.setString(1, car.getLicensePlate());
                  st.setBigDecimal(2, car.getNumberOfKM());
                  st.setString(3, car.getModel());
                  st.setBigDecimal(4, car.getPrice());
                  st.setLong(5, car.getId());
                  if (st.executeUpdate() != 1) {
                      throw new IllegalArgumentException("cannot update car" + car);
                  }
              }
        } catch(SQLException ex){
            throw new ServiceFailureException("Error, when updating car from DB.", ex);
        }


    }

    public List<Car> getAllCars() throws ServiceFailureException{
        try (Connection connection = dataSource.getConnection()){
              try(PreparedStatement st = connection.prepareStatement("SELECT id,licensePlate,model,price,numberOfKm FROM cars")) {

                  ResultSet rs = st.executeQuery();

                  List<Car> result = new ArrayList<>();
                  while (rs.next()) {
                      result.add(resultSetToCar(rs));
                  }
                  return result;
              }
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when retrieving all cars", ex);
        }
    }


    public Car getCarById(Long id) {
        checkDataSource();
        if (id == null){
            throw new IllegalArgumentException("id is null");
        }
        if(id < 0){
            throw new IllegalArgumentException("id is negative ");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {conn = dataSource.getConnection();
             st = conn.prepareStatement("SELECT id,licensePlace,model,price,numberOfKm FROM cars WHERE id = ?");

            st.setLong(1, id);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                Car car = resultSetToCar(rs);

                if (rs.next()) {
                    throw new ServiceFailureException(
                            "Internal error: More entities with the same id found "
                                    + "(source id: " + id + ", found " + car + " and " + resultSetToCar(rs));
                }

                return car;
            } else {
                return null;
            }

        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when retrieving car with id " + id, ex);
        }finally {
            DBUtils.closeQuietly(conn,st);
        }
    }

    private Car resultSetToCar(ResultSet rs) throws SQLException{
        Car car = new Car();
        car.setId(rs.getLong("id"));
        car.setLicensePlate(rs.getString("licensePlate"));
        car.setModel(rs.getString("model"));
        car.setPrice(rs.getBigDecimal("price"));
        car.setNumberOfKM(rs.getBigDecimal("numberOfKm"));

        return car;
    }

    public Car getCarByLicensePlate(String licensePlate) {
        if (licensePlate == null){
            throw new IllegalArgumentException("license plate is null");
        }
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT id,licensePlace,model,price,numberOfKm FROM cars WHERE licensePlate = ?")) {

            st.setString(1, licensePlate);
            ResultSet rs = st.executeQuery();

            if (rs.first()) {
                Car car = resultSetToCar(rs);

                if (rs.next()) {
                    throw new ServiceFailureException(
                            "Internal error: More entities with the same license plate found "
                                    + "(source license plate: " + licensePlate + ", found " + car + " and " + resultSetToCar(rs));
                }

                return car;
            } else {
                return null;
            }

        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when retrieving car with license plate " + licensePlate, ex);
        }
    }

    public boolean getAvailabilityOfCar(Long id) {
       /* if (id == null){
            throw new IllegalArgumentException("id is null");
        }
        Car car = getCarById(id);
        List<Lease> leases = findLeasesForCar(car);
        if()*/





        return false;
    }

    private void validate(Car car ){
        if (car == null) {
            throw new IllegalArgumentException("Car is null");
        }
        if (car.getId() == null) {
            throw new IllegalArgumentException("Car id is null");
        }
        if (car.getLicensePlate()== null || car.getLicensePlate().isEmpty()) {
            throw new IllegalArgumentException("Car license plate is null or empty");
        }
        if (car.getModel()==null || car.getModel().isEmpty()) {
            throw new IllegalArgumentException("Car model is null or empty");
        }
        if (car.getPrice().compareTo(BigDecimal.ZERO) <= 0  ) {
            throw new IllegalArgumentException("Car price is <= 0");
        }
        if (car.getNumberOfKM().compareTo(BigDecimal.ZERO) < 0  ) {
            throw new IllegalArgumentException("Car number of kilometers is lower than 0");
        }
    }
}
