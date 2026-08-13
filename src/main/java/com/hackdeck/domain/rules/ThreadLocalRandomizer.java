package com.hackdeck.domain.rules;

import java.util.concurrent.ThreadLocalRandom;

public final class ThreadLocalRandomizer implements Randomizer {

    @Override
    public boolean chance(double probability) {
        if (probability <= 0) {
            return false;
        }
        if (probability >= 1) {
            return true;
        }
        return ThreadLocalRandom.current().nextDouble() < probability;
    }
}
