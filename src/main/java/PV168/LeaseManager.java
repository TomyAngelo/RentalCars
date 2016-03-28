package PV168;

/**
 * Created by TomyAngelo on 18. 3. 2016.
 */

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;

public interface LeaseManager {

    public void createLease(Lease lease);

    public Lease getLeaseByID(Long id);

    public Collection<Lease> getAllLeases();

    public Collection<Lease> getAllLeasesByEndDate(LocalDate endDate);

    public Collection<Lease> findLeasesForCustomer(Customer customer);

    public Collection<Lease> findLeasesForCar(Car car);

    public void updateLease(Lease lease);

    public void deleteLease(Lease lease);




}