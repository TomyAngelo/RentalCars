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
    private LocalDate realEndDate;

    public Lease(Long id, Customer customer, Car car, LocalDate dateFrom, BigDecimal price, LocalDate dateTo, LocalDate realEndDate) {
        this.id = id;
        this.customer = customer;
        this.car = car;
        this.dateFrom = dateFrom;
        this.price = price;
        this.dateTo = dateTo;
        this.realEndDate = realEndDate;
    }

    public Lease(){

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

    public LocalDate getRealEndDate() {
        return realEndDate;
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

    public void setRealEndDate(LocalDate realEndDate) {
        this.realEndDate = realEndDate;
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

        if (!id.equals(lease.id)) return false;
        if (!customer.equals(lease.customer)) return false;
        if (!car.equals(lease.car)) return false;
        if (!dateFrom.equals(lease.dateFrom)) return false;
        if (!dateTo.equals(lease.dateTo)) return false;
        if (!price.equals(lease.price)) return false;
        return realEndDate.equals(lease.realEndDate);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + customer.hashCode();
        result = 31 * result + car.hashCode();
        result = 31 * result + dateFrom.hashCode();
        result = 31 * result + dateTo.hashCode();
        result = 31 * result + price.hashCode();
        result = 31 * result + realEndDate.hashCode();
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
                ", realEndDate=" + realEndDate +
                '}';
    }
}
