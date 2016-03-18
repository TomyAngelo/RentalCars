package PV168;

import java.util.Collection;
import java.util.Date;

/**
 * Created by TomyAngelo on 18. 3. 2016.
 */
public class LeaseManagerImpl implements LeaseManager {
    @Override
    public void createLease(Lease lease) {

    }

    @Override
    public Lease getLeaseByID(Long ID) {
        return null;
    }

    @Override
    public Collection<Lease> getAllLeases() {
        return null;
    }

    @Override
    public Collection<Lease> getAllLeasesByEndDate(Date endDate) {
        return null;
    }

    @Override
    public Collection<Lease> findLeasesForCustomer(Customer customer) {
        return null;
    }

    @Override
    public Collection<Lease> findLeasesForCar(Car car) {
        return null;
    }

    @Override
    public void updateLease(Lease lease) {

    }

    @Override
    public void deleteLease(Lease lease) {

    }
}
