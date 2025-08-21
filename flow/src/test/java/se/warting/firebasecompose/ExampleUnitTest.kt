@file:Suppress("MagicNumber")
package se.warting.firebasecompose

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun additionIsCorrect() {
        assertEquals(4, 2L + 2L)
    }
    
    // Note: ProductStatus.Available price functionality tests would require 
    // Android framework dependencies and mocking libraries. 
    // The pricing logic is simple property access, so integration testing 
    // in the sample app provides sufficient coverage.
}
