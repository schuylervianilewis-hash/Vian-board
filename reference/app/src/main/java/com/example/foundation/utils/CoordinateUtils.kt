package com.example.foundation.utils

import kotlin.math.sqrt

/**
 * Geometric and spatial coordinate utilities for key proximity and touch hit-testing.
 */
object CoordinateUtils {

    /**
     * Calculates squared Euclidean distance between two points (X1, Y1) and (X2, Y2).
     * Avoids costly square-root calculations during hot path hit testing.
     */
    fun distanceSquared(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return dx * dx + dy * dy
    }

    /**
     * Calculates true Euclidean distance between two points.
     */
    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt(distanceSquared(x1, y1, x2, y2))
    }

    /**
     * Checks if a touch point (x, y) falls within a bounding rectangle with optional padding.
     */
    fun contains(x: Float, y: Float, left: Float, top: Float, right: Float, bottom: Float, padding: Float = 0f): Boolean {
        return x >= (left - padding) && x <= (right + padding) &&
                y >= (top - padding) && y <= (bottom + padding)
    }

    /**
     * Clamps a value between min and max bounds.
     */
    fun clamp(value: Float, min: Float, max: Float): Float {
        return when {
            value < min -> min
            value > max -> max
            else -> value
        }
    }
}
