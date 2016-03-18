package PV168;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by TomyAngelo on 18. 3. 2016.
 */
public class Lease {
    private Customer customer;
    private Car car;
    private Date dateFrom;
    private Date dateTo;
    private BigDecimal price;
    private Date realEndDate;

    public Lease(Customer customer, Car car, Date dateFrom, BigDecimal price, Date dateTo, Date realEndDate) {
        this.customer = customer;
        this.car = car;
        this.dateFrom = dateFrom;
        this.price = price;
        this.dateTo = dateTo;
        this.realEndDate = realEndDate;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Car getCar() {
        return car;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Date getRealEndDate() {
        return realEndDate;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setRealEndDate(Date realEndDate) {
        this.realEndDate = realEndDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lease lease = (Lease) o;

        if (customer != null ? !customer.equals(lease.customer) : lease.customer != null) return false;
        if (car != null ? !car.equals(lease.car) : lease.car != null) return false;
        if (dateFrom != null ? !dateFrom.equals(lease.dateFrom) : lease.dateFrom != null) return false;
        if (dateTo != null ? !dateTo.equals(lease.dateTo) : lease.dateTo != null) return false;
        if (price != null ? !price.equals(lease.price) : lease.price != null) return false;
        return realEndDate != null ? realEndDate.equals(lease.realEndDate) : lease.realEndDate == null;

    }

    @Override
    public int hashCode() {
        int result = customer != null ? customer.hashCode() : 0;
        result = 31 * result + (car != null ? car.hashCode() : 0);
        result = 31 * result + (dateFrom != null ? dateFrom.hashCode() : 0);
        result = 31 * result + (dateTo != null ? dateTo.hashCode() : 0);
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + (realEndDate != null ? realEndDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Lease{" +
                "customer=" + customer +
                ", car=" + car +
                ", dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", price=" + price +
                ", realEndDate=" + realEndDate +
                '}';
    }
}
