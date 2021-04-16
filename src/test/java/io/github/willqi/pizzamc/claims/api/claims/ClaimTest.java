package io.github.willqi.pizzamc.claims.api.claims;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.UUID;

public class ClaimTest {

    @Test
    public void addFlagShouldNotReaddAFlag() {
        Claim claim = new Claim(UUID.randomUUID(), 0, 0, 0);
        claim.addFlag(Claim.Flags.ALWAYS_DAY);
        claim.addFlag(Claim.Flags.ALWAYS_DAY);
        assertEquals(Claim.Flags.ALWAYS_DAY.getValue(), claim.getFlags());
    }

    @Test
    public void removeFlagShouldNotRemoveAFlagThatDoesNotExist() {
        Claim claim = new Claim(UUID.randomUUID(), 0, 0, 0);
        claim.removeFlag(Claim.Flags.ALWAYS_DAY);
        assertEquals(0, claim.getFlags());

        claim.addFlag(Claim.Flags.ALWAYS_DAY);
        claim.removeFlag(Claim.Flags.ALWAYS_DAY);
        assertEquals(0, claim.getFlags());
    }

}
