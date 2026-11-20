package com.dhun.music.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class RelativeSeekPositionTest {
    @Test fun forwardTenSecondsAddsTenSeconds() = assertEquals(40_000L, calculateSeekPositionMs(30_000L, 120_000L, 10_000L))
    @Test fun backwardTenSecondsSubtractsTenSeconds() = assertEquals(20_000L, calculateSeekPositionMs(30_000L, 120_000L, -10_000L))
    @Test fun tenSecondForwardSeekStopsAtDuration() = assertEquals(120_000L, calculateSeekPositionMs(115_000L, 120_000L, 10_000L))
    @Test fun tenSecondBackwardSeekStopsAtZero() = assertEquals(0L, calculateSeekPositionMs(5_000L, 120_000L, -10_000L))
    @Test fun forwardThirtySecondsAddsThirtySeconds() = assertEquals(60_000L, calculateSeekPositionMs(30_000L, 120_000L, 30_000L))
    @Test fun backwardThirtySecondsSubtractsThirtySeconds() = assertEquals(0L, calculateSeekPositionMs(30_000L, 120_000L, -30_000L))
    @Test fun backwardSeekAtBeginningNeverGoesNegative() = assertEquals(0L, calculateSeekPositionMs(5_000L, 120_000L, -30_000L))
    @Test fun forwardSeekNearEndNeverExceedsDuration() = assertEquals(120_000L, calculateSeekPositionMs(115_000L, 120_000L, 30_000L))
    @Test fun unknownDurationKeepsCurrentPositionAsSafeFallback() = assertEquals(45_000L, calculateSeekPositionMs(45_000L, 0L, 10_000L))
    @Test fun negativeCurrentPositionIsNormalized() = assertEquals(0L, calculateSeekPositionMs(-500L, 0L, -10_000L))
}
