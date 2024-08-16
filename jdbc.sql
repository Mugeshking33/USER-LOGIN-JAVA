CREATE database data;
use data;
create table user(
id int primary key auto_increment,
password varchar(100),
name varchar(30),
salary float(10,2),
star varchar(1),
joindate date
);
alter table  user add(age int);
select *from user;
SELECT id, password, name, age, star AS rate, salary, joindate FROM user;
DELIMITER //
CREATE PROCEDURE checkUserCredentials(
    IN userId INT,
    IN userPassword VARCHAR(100),
    OUT isValid BOOLEAN
)
BEGIN
    DECLARE userCount INT;
    SELECT COUNT(*) INTO userCount FROM user WHERE id = userId AND password = userPassword;
    SET isValid = (userCount > 0);
END //
DELIMITER ;

