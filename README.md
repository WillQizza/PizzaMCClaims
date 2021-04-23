# PizzaMCClaims
**PizzaMCClaims** is a claims and homes system built for Minecraft servers.
This system only supports **Spigot** currently, but it can extend to other server software as the API is separate from the Spigot plugin.

| Module        | Description                                     |
| ------------- | ----------------------------------------------- |
| api           | Contains the core claims/homes systems          | 
| spigot-plugin | Spigot plugin utilizing the claims/homes system |

For more information, you can navigate to the `README.md` in each module.

# Build
You can get the latest jars from the [releases](https://github.com/WillQi/PizzaMCClaims/releases) section or you can build it yourself.
- `git clone https://github.com/WillQi/PizzaMCClaims.git`
- `cd PizzaMCClaims`
- `mvn clean package` (or `mvn test` if you're only interested in the test cases)