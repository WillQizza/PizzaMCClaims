package io.github.willqi.pizzamc.claims.database;


public interface SaveableObject {

    /**
     * Check if the object was modified
     * @return if the object differs from it's database record
     */
    boolean isModified ();

    /**
     * Database calls to update the records should be placed here.
     */
    void save ();

}
