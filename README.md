# Shop Service

A basic RESTful(ish) API for a SHOP resource.
 

### Prerequisites
The project expects Java 8 and gradle to be installed.

### Usage

#### Running the Service

```sh
./gradlew bootRun # use 'gradlew.bat bootRun' on Windows
```

#### Distributing the Service

```sh
./gradlew build # use 'gradlew.bat build' on Windows
```
generates the binary `./build/libs/shop-service-0.0.1-SNAPSHOT.jar`

A Dockerfile is also available to build an image `shop-service:latest`. 
The image build process can be launched using 
```sh
./gradlew buildDocker # use 'gradlew.bat buildDocker' on windows`
```
#### Testing the Service

##### Java
Some Java unit-tests and integration tests are included. To execute them, run:  
```sh
./gradlew test # use 'gradlew.bat test' on windows
```

##### Postman
A `shop-service.postman_collection` file is also available, with a basic test suite. 
To run it, use the _Postman_ GUI or if you have `npm` installed, install the CLI package with
```sh
npm install -g newman
```

and then execute the test suite

```sh
newman run shop-service.postman_collection --delay-request 500
```

Example of an execution:
```
❯❯❯ newman run shop-service.postman_collection --delay-request 500
newman

shop-service

→ Get all shops (Empty)
  GET http://localhost:8080/shops [200 OK, 228B, 168ms]
  ✓  Status code is 200
  ✓  Content-Type is present

→ Add shop without address
  POST http://localhost:8080/shops [400 Bad Request, 227B, 38ms]
  ✓  Status code is 400
  ✓  Error message is descriptive

→ Add London shop
  POST http://localhost:8080/shops [201 Created, 169B, 122ms]
  ✓  Status code is 201
  ✓  Resource location is present

→ Get London shop
  GET http://localhost:8080/shops/London%20shop [200 OK, 359B, 18ms]
  ✓  Status code is 200
  ✓  Version 0 received

→ Get all shops (shops available)
  GET http://localhost:8080/shops [200 OK, 463B, 13ms]
  ✓  Status code is 200
  ✓  Content-Type is present
  ✓  Returns 1 shop
  ✓  HAL self link to list
  ✓  HAL self link to each resource

→ Update London shop
  POST http://localhost:8080/shops [200 OK, 424B, 8ms]
  ✓  Status code is 200
  ✓  Resource location is present
  ✓  Version 0 received

→ Add Edinburgh shop
  POST http://localhost:8080/shops [201 Created, 172B, 7ms]
  ✓  Status code is 201
  ✓  Resource location is present

→ Add Liverpool shop
  POST http://localhost:8080/shops [201 Created, 172B, 7ms]
  ✓  Status code is 201
  ✓  Resource location is present

→ Get London shop (should have coordinates)
  GET http://localhost:8080/shops/London%20shop [200 OK, 379B, 7ms]
  ✓  Status code is 200
  ✓  Version 1 received
  ✓  Has coordinates

→ Get closest shop to Edinburgh
  GET http://localhost:8080/shops?lat=55.959736&lng=-2.789885 [200 OK, 376B, 8ms]
  ✓  Status code is 200
  ✓  Edinburgh shop received

→ Get closest shop to Brighton
  GET http://localhost:8080/shops?lat=50.8375077&lng=-0.1764013 [200 OK, 379B, 6ms]
  ✓  Status code is 200
  ✓  London shop received

┌─────────────────────────┬──────────┬──────────┐
│                         │ executed │   failed │
├─────────────────────────┼──────────┼──────────┤
│              iterations │        1 │        0 │
├─────────────────────────┼──────────┼──────────┤
│                requests │       11 │        0 │
├─────────────────────────┼──────────┼──────────┤
│            test-scripts │       11 │        0 │
├─────────────────────────┼──────────┼──────────┤
│      prerequest-scripts │        0 │        0 │
├─────────────────────────┼──────────┼──────────┤
│              assertions │       27 │        0 │
├─────────────────────────┴──────────┴──────────┤
│ total run duration: 6.2s                      │
├───────────────────────────────────────────────┤
│ total data received: 1.37KB (approx)          │
├───────────────────────────────────────────────┤
│ average response time: 36ms                   │
└───────────────────────────────────────────────┘
```

A delay of **500ms** is defined in the CLI and **should be defined as well** when running the test suite from the GUI as
The geolocation of the shops is asynchronous, and the location related validations could fail. 

### Decisions made

#### Persistence

I used a `ConcurrentHashMap` for a quick and dirty in-memory "persistence" solution instead of an H2 DB for example. 
If the requirement should keep something similar to this solution, clustering the service would require a switch 
to a distributed data structure such as a _Hazelcast Map_ that seems to implement the standard `ConcurrentMap`
(I haven't actually used `Hazelcast` yet)

#### Geolocation

I considered the geolocation of the shop as an enrichment step, not critical during creation, and best handled asynchronously 
to avoid blocking the user request while waiting for the GMaps API to reply.
I also decided to implement a basic versioning system of the entities to avoid possible race conditions during the geolocation.
The `com.example.shopService.domain.ShopService.geolocateShop()` method only sets the coordinates of the shop if the version matches.

The distance calculation is using a standard formula called Haversine and the closest shop is calculated with that formula and a 
List Comparator (The formula offers an approximation in Km between two points. If greater accuracy is needed, other solutions should be envisaged).

#### Rest

The service is based on Spring HATEOAS and implements some common best practices such as returning the location of a resource on POST
or adding links to resources.
The service validates the request's body and tries to return meaningful errors.

### Comments

It has been a fun exercise. As an MVP and with the time restrictions, I had to find the right balance between quality and features. 
The service is far from perfect, and unit tests could be largely expanded to cover quite a few edge cases.
I'm still doubting if I understood and implemented as expected the response with the previous shop in case of an update.

I'm looking forward to embark in this new and exciting opportunity :)