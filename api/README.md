# PizzaMCClaims - API
The API is responsible for handling all backend features for a claims/home system.

# Implementation 
In order to fully utilize the API, you will need an instance of the `ClaimsManager`, `HomesManager` and optionally the `UsersManager`.

| Manager       | Description                                                 |
| ------------- | ----------------------------------------------------------- |
| ClaimsManager | Responsible for retrieving and updating claim/claim helpers | 
| HomesManager  | Responsible for retrieving and updating homes               |
| UsersManager  | Responsible for retrieving and updating user records        |

All three managers will require their respective daos in their constructors.
These can be retrieved via a `DaoSource` object.

Below are the sources available at the moment.

| Class        | Description                                |
| ------------ | ------------------------------------------ |
| SQLDaoSource | Access and save data via an SQL connection |

# Build
You can get the latest jar from the [releases](https://github.com/WillQi/PizzaMCClaims/releases) section or you can build it yourself.
- `git clone https://github.com/WillQi/PizzaMCClaims.git`
- `cd PizzaMCClaims/api`
- `mvn clean package` (or `mvn test` if you're only interested in the test cases)
