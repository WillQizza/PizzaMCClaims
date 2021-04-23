# PizzaMCClaims - Spigot
Spigot implementation of the API

# Installation
You can get the latest jar from the [releases](https://github.com/WillQi/PizzaMCClaims/releases) section or you can build it yourself.
- `git clone https://github.com/WillQi/PizzaMCClaims.git`
- `cd PizzaMCClaims/spigot-plugin`
- `mvn clean package`
- Upload `PizzaMCClaims-Spigot-1.0-SNAPSHOT.jar` to your `plugins` directory.
- Run your Spigot server once and fill in the details in the `config.yml` file in the plugin folder

# Configuration
Once you run the plugin at least once, it will create a `config.yml` file in its plugin directory that you will need to fill out.
```yml
# Database connection (SQL)
host: Your SQL host
port: Your SQL port
database: Your SQL database
username: Your SQL username
password: Your SQL password

# -1 for infinity
max_claims_per_player: -1 # Amount of claims you want players to have at max
max_homes_per_player: -1  # Amount of homes you want players to have at max
```

# Commands
| Command                 | Description                                              | Permission                             |
| ----------------------- | -------------------------------------------------------- | -------------------------------------- |
| `/claim add`            | Claim the current chunk as your own                      | `pizzamcclaims.commands.claim`         |
| `/claim remove`         | Unclaim the current chunk                                | `pizzamcclaims.commands.claim`         |
| `/claim flags`          | Set special modifiers for your claim                     | `pizzamcclaims.commands.claim.flags`   |
| `/claim helpers`        | Set the permissions for other players in your claim      | `pizzamcclaims.commands.claim.helpers` |
| `/claim view`           | View the claim boundaries of yours' and others' claims   | `pizzamcclaims.commands.claim`         |
| `/home list`            | View all of your homes                                   | `pizzamcclaims.commands.home`          |
| `/home teleport <name>` | Teleport to a specific home you set                      | `pizzamcclaims.commands.home`          |
| `/home create <name>`   | Create a new home                                        | `pizzamcclaims.commands.home`          |
| `/home destroy <name>`  | Destroy one of your homes                                | `pizzamcclaims.commands.home`          |
| `/home details <name>`  | Edit or view the details of your home                    | `pizzamcclaims.commands.home`          |

# Permissions
| Permission                             | Description                                                                                                  | Default  |
| -------------------------------------- | ------------------------------------------------------------------------------------------------------------ | -------- |
| `pizzamcclaims.commands.claim`         | Access to `/claim add`, `/claim remove` and `/claim view`                                                    | `true`   |
| `pizzamcclaims.commands.claim.flags`   | Access to `/claim flags`                                                                                     | `op`     |
| `pizzamcclaims.commands.claim.helpers` | Access to `/claim helpers`                                                                                   | `true`   |
| `pizzamcclaims.commands.claim.admin`   | Allows you to act as the owner of any existing claim, bypass the claim limit, and claim already claimed land | `op`     |
| `pizzamcclaims.commands.home`          | Access to all `/home` commands                                                                               | `true`   |
| `pizzamcclaims.commands.home.admin`    | Bypass the home limit                                                                                        | `op`     |