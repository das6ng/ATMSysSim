CREATE TABLE accounts (
  sn char(16) NOT NULL, -- 4+4+8
  password varchar(255) NOT NULL,
  name varchar(255) DEFAULT NULL,
  ID char(18) DEFAULT NULL,
  tel varchar(255) DEFAULT NULL,
  addr varchar(255) DEFAULT NULL,
  balance double NOT NULL,
  
  PRIMARY KEY (sn)
);

insert into accounts(sn, password, name, ID, tel, addr, balance)
    values(
        '0001000100000001',
        '123456',
        'Peter',
        '360721199901011122',
        '10086',
        'Peking',
        9999
    );