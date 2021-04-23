package io.github.willqi.pizzamc.claims.api.claims;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.UUID;

public class ClaimTest {

    private static final UUID NULL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final ChunkCoordinates DEFAULT_COORDINATES = new ChunkCoordinates(NULL_UUID, 0, 0);

    @Test
    public void addFlagShouldNotReaddAFlag() {
        Claim claim = new Claim(DEFAULT_COORDINATES, 0);
        claim.addFlag(Claim.Flag.ALWAYS_DAY);
        claim.addFlag(Claim.Flag.ALWAYS_DAY);
        assertEquals(Claim.Flag.ALWAYS_DAY.getValue(), claim.getFlags());
    }

    @Test
    public void removeFlagShouldNotRemoveAFlagThatDoesNotExist() {
        Claim claim = new Claim(DEFAULT_COORDINATES, 0);
        claim.removeFlag(Claim.Flag.ALWAYS_DAY);
        assertEquals(0, claim.getFlags());

        claim.addFlag(Claim.Flag.ALWAYS_DAY);
        claim.removeFlag(Claim.Flag.ALWAYS_DAY);
        assertEquals(0, claim.getFlags());
    }

}
