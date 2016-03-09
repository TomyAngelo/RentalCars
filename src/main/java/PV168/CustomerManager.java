package PV168;

import java.util.List;

/**
 * Created by TomyAngelo on 9. 3. 2016.
 */
public interface CustomerManager {

    void createCustomer(Customer customer);
    void updateCustomer(Customer customer);
    void deleteCustomer(Customer customer);
    Customer findCustomerById(Long id);
    List<Customer> getAllCustomers();
}
