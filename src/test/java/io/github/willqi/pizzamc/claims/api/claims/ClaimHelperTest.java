package io.github.willqi.pizzamc.claims.api.claims;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.UUID;

public class ClaimHelperTest {

    @Test
    public void addPermissionShouldNotReaddAPermission() {
        ClaimHelper helper = new ClaimHelper(UUID.randomUUID(), 0);
        helper.addPermission(ClaimHelper.Permissions.FLY);
        helper.addPermission(ClaimHelper.Permissions.FLY);
        assertEquals(ClaimHelper.Permissions.FLY.getValue(), helper.getPermissions());
    }

    @Test
    public void removePermissionShouldNotRemoveAPermissionThatDoesNotExist() {
        ClaimHelper helper = new ClaimHelper(UUID.randomUUID(), 0);
        helper.addPermission(ClaimHelper.Permissions.FLY);
        helper.removePermission(ClaimHelper.Permissions.ADMIN);
        assertEquals(ClaimHelper.Permissions.FLY.getValue(), helper.getPermissions());

        helper.removePermission(ClaimHelper.Permissions.FLY);
        assertEquals(0, helper.getPermissions());
    }

}
