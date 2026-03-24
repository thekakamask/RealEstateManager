package com.dcac.realestatemanager.utilsTest

import androidx.core.net.toUri
import com.dcac.realestatemanager.utils.Utils
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File

@RunWith(JUnit4::class)
class UtilsInstrumentedTest {

    private val context = androidx.test.platform.app.InstrumentationRegistry
        .getInstrumentation()
        .targetContext


    @Test
    fun saveBytesToAppStorage_createsFileSuccessfully() {
        val bytes = "Hello World".toByteArray()

        val uri = Utils.saveBytesToAppStorage(
            context = context,
            bytes = bytes,
            subDir = "testDir",
            fileName = "test.txt"
        )

        assertNotNull(uri)

        val file = File(uri!!.path!!)
        assertTrue(file.exists())
        assertTrue(file.readText().contains("Hello World"))
    }

    @Test
    fun saveUriToAppStorage_copiesFileSuccessfully() {
        val tempFile = File(context.cacheDir, "temp.txt")
        tempFile.writeText("Test content")

        val uri = tempFile.toURI().toString().toUri()

        val result = Utils.saveUriToAppStorage(context, uri)

        assertNotNull(result)
        assertTrue(result!!.exists())
    }

    @Test
    fun isInternetAvailable_doesNotCrash() {
        val result = Utils.isInternetAvailable(context)

        assertNotNull(result)
    }



}