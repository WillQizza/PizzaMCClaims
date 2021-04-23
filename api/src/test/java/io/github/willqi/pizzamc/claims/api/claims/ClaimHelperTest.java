package io.github.willqi.pizzamc.claims.api.claims;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.UUID;

public class ClaimHelperTest {

    @Test
    public void addPermissionShouldNotReaddAPermission() {
        ClaimHelper helper = new ClaimHelper(UUID.randomUUID(), 0);
        helper.addPermission(ClaimHelper.Permission.BUILD);
        helper.addPermission(ClaimHelper.Permission.BUILD);
        assertEquals(ClaimHelper.Permission.BUILD.getValue(), helper.getPermissions());
    }

    @Test
    public void removePermissionShouldNotRemoveAPermissionThatDoesNotExist() {
        ClaimHelper helper = new ClaimHelper(UUID.randomUUID(), 0);
        helper.addPermission(ClaimHelper.Permission.BUILD);
        helper.removePermission(ClaimHelper.Permission.INTERACT);
        assertEquals(ClaimHelper.Permission.BUILD.getValue(), helper.getPermissions());

        helper.removePermission(ClaimHelper.Permission.BUILD);
        assertEquals(0, helper.getPermissions());
    }

}
