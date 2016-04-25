CREATE TABLE cars (
  id              INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  licensePlate    VARCHAR(70),
  model           VARCHAR(45),
  price           DECIMAL(20,2),
  numberOfKm      DECIMAL(20,2)
);

CREATE TABLE customers (
  id              INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  name            VARCHAR(50),
  address         VARCHAR(150),
  phoneNumber     VARCHAR(20)
);

CREATE TABLE leases (
  id          INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
  IdCustomer  INT REFERENCES customers (id) ON DELETE CASCADE,
  IdCar       INT REFERENCES cars (id) ON DELETE CASCADE,
  dateFrom    DATE,
  dateTo      DATE,
  realEndDate DATE,
  price       DECIMAL(20,2)
);