package io.github.willqi.pizzamc.claims.api.users;

import java.util.UUID;

public class User implements Cloneable {

    private final UUID uuid;
    private String name;

    public User(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public User clone() {
        try {
            return (User)super.clone();
        } catch (CloneNotSupportedException exception) {
            return new User(this.uuid, this.name);
        }
    }

}
