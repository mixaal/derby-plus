# derby-plus

List contents of local derby database w/o having the real Derby database stared. It came handy when debugging unit-tests with embedded driver.

## Quick Start

```
./build.sh && ./run.sh
```

Inside the terminal:

1. Connect

```
[derbyplus]$ connect jdbc:derby:/home/mikc/git/derbyDB;user=admin;databaseName=testdb
Connected to: jdbc:derby:/home/mikc/git/derbyDB;user=admin;databaseName=testdb
```
2. Show tables
```
[derbyplus]$ show tables
DATABASECHANGELOG
DATABASECHANGELOGLOCK
[derbyplus]$ 
```
3. Get schema:

```
[derbyplus]$ connect jdbc:derby:/home/mikc/git/derbyDB;user=admin;databaseName=testdb
Connected to: jdbc:derby:/home/mikc/git/derbyDB;user=admin;databaseName=testdb
[derbyplus]$ get schema
Schema: ADMIN
```

4. Do the query
```
[derbyplus]$ select * from test
[20874c1b-6da3-487a-a97a-2ae3b9d4cf16, hugacaga, hugacaga, null, http://192.168.1.1/private, https://ingress-1-1/, http://admin1, http://admin1, cloudsql-v1, 1.0, 0, 0, NPI-1, nnnnn-1, 1624550245227, 1624550245227, org.apache.derby.impl.jdbc.EmbedClob@fa36558, null, FREE, DONE, null, null, 0, null, 0, null, 0, 0, 0, null, 0]
[10208220-93b8-4278-aba4-e9f2baaa27bb, hugacaga, hugacaga, null, http://192.168.1.1/private, https://ingress-1-1/, http://admin1, http://admin1, cloudsql-v1, 1.0, 0, 0, NPI-2, nnnnn-2, 1624550245243, 1624550245243, org.apache.derby.impl.jdbc.EmbedClob@672872e1, null, FREE, DONE, null, null, 0, null, 0, null, 0, 0, 0, null, 0]
[eae13162-c681-4268-9303-f9ecb4313880, hugacaga, hugacaga, null, http://192.168.1.1/private, https://ingress-1-1/, http://admin1, http://admin1, cloudsql-v1, 1.0, 0, 0, NPI-3, nnnnn-3, 1624550245243, 1624550245243, org.apache.derby.impl.jdbc.EmbedClob@32910148, null, FREE, DONE, null, null, 0, null, 0, null, 0, 0, 0, null, 0]
```

5. Exit the connection

```
[derbyplus]$ exit
Closing connection to :jdbc:derby:/home/mikc/git/derbyDB;user=admin;databaseName=testdb
```
