package io.github.willqi.pizzamc.claims.api.homes;

import io.github.willqi.pizzamc.claims.database.SaveableObject;

public class Home implements SaveableObject {

    private boolean wasModified;

    @Override
    public boolean isModified() {
        return wasModified;
    }

    @Override
    public void save() {

    }

}
