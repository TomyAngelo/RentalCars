package PV168;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/**
 * Created by TomyAngelo on 18. 3. 2016.
 */
public class Lease {
    private Long id;
    private Customer customer;
    private Car car;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private BigDecimal price;


    public Lease(Long id, Customer customer, Car car, LocalDate dateFrom, BigDecimal price, LocalDate dateTo) {
        this.id = id;
        this.customer = customer;
        this.car = car;
        this.dateFrom = dateFrom;
        this.price = price;
        this.dateTo = dateTo;
    }

    public Lease() {

    }

    public Customer getCustomer() {
        return customer;
    }

    public Car getCar() {
        return car;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lease lease = (Lease) o;

        if (id != null ? !id.equals(lease.id) : lease.id != null) return false;
        if (customer != null ? !customer.equals(lease.customer) : lease.customer != null) return false;
        if (car != null ? !car.equals(lease.car) : lease.car != null) return false;
        if (dateFrom != null ? !dateFrom.equals(lease.dateFrom) : lease.dateFrom != null) return false;
        if (dateTo != null ? !dateTo.equals(lease.dateTo) : lease.dateTo != null) return false;
        return price != null ? price.equals(lease.price) : lease.price == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (customer != null ? customer.hashCode() : 0);
        result = 31 * result + (car != null ? car.hashCode() : 0);
        result = 31 * result + (dateFrom != null ? dateFrom.hashCode() : 0);
        result = 31 * result + (dateTo != null ? dateTo.hashCode() : 0);
        result = 31 * result + (price != null ? price.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Lease{" +
                "id=" + id +
                ", customer=" + customer +
                ", car=" + car +
                ", dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", price=" + price +
                '}';
    }
}