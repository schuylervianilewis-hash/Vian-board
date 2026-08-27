package com.example.foundation.common

/**
 * Array and collection manipulation utilities optimized for keyboard performance.
 */
object CollectionUtils {

    /**
     * Performs an allocation-free array copy into a target array up to target length.
     */
    fun arrayCopy(src: IntArray, srcPos: Int, dest: IntArray, destPos: Int, length: Int) {
        System.arraycopy(src, srcPos, dest, destPos, length)
    }

    /**
     * Reusable fast binary search for integer arrays.
     */
    fun binarySearch(array: IntArray, size: Int, value: Int): Int {
        var low = 0
        var high = size - 1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midVal = array[mid]

            when {
                midVal < value -> low = mid + 1
                midVal > value -> high = mid - 1
                else -> return mid
            }
        }
        return -(low + 1)
    }
}
