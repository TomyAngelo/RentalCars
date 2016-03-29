package PV168;

/**
 * Created by TomyAngelo on 18. 3. 2016.
 */

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface LeaseManager {

    public void createLease(Lease lease);

    public Lease getLeaseByID(Long id);

    public List<Lease> getAllLeases();

    public List<Lease> getAllLeasesByEndDate(LocalDate endDate);

    public List<Lease> findLeasesForCustomer(Customer customer);

    public List<Lease> findLeasesForCar(Car car);

    public void updateLease(Lease lease);

    public void deleteLease(Lease lease);




}