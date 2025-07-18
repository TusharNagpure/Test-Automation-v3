package com.example.testautomation

import android.content.ContentValues
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import java.io.FileWriter
import java.io.OutputStreamWriter
import java.util.*

@RunWith(AndroidJUnit4::class)
class addUser {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        //device.pressHome()
    }

    private fun launchApp() {
        val currentApp = device.currentPackageName
        if (currentApp != "com.spintly.smartacccessandroidv3.debug") {
            val context = InstrumentationRegistry.getInstrumentation().context
            val intent = Intent().apply {
                setClassName(
                    "com.spintly.smartacccessandroidv3.debug",
                    "com.spintly.smartacccessandroidv3.mainActivity.MainActivity"
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            device.wait(
                Until.hasObject(By.pkg("com.spintly.smartacccessandroidv3.debug").depth(0)),
                8000
            )
        }
    }

    fun saveTestLogs(logList: List<String>) {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val time = SimpleDateFormat("HH:mm:ss",    Locale.US).format(Date())

        // Prepare metadata for a new Download entry
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "uiautomator_logs_$date.txt")
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            // Places it in Downloads/TestLogs/
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/TestLogs")
        }

        // Insert a new row into MediaStore → gives you a URI to write to
        val resolver = ctx.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("Failed to create MediaStore entry")

        // Write your log lines
        resolver.openOutputStream(uri).use { os ->
            OutputStreamWriter(os).use { writer ->
                writer.appendLine("=== Test Run: $time ===")
                logList.forEach { writer.appendLine(it) }
                writer.appendLine()
            }
        }
    }

    fun restoreState() {
        while (!device.hasObject(By.textContains("User Information"))) {
            val cancelButton = device.wait(Until.findObject(By.textContains("Cancel")), 1000)
            cancelButton.click()
            device.wait(Until.gone(By.textContains("Cancel")), 1000)
        }
    }

    fun restoreToUserManagement() {
        device.pressBack()
        val userManagementDashboard =
            device.wait(Until.findObject(By.textContains("User Management")), 2000)
        userManagementDashboard.click()
    }


    fun getFormFields(): List<UiObject2> {
        return device.wait(Until.findObjects(By.clazz("android.widget.EditText")), 2000)
    }


    fun resumeAppFromRecents(appLabel: String = "Smart Access") {
        device.pressRecentApps()
        Thread.sleep(1000) // wait for animations to settle

        val appThumbnail = device.findObject(UiSelector().textContains(appLabel))

        assert(appThumbnail != null) {
            "FAIL: Could not find '$appLabel' in recent apps. App may not be open or label is incorrect."
        }

        appThumbnail.click()
        device.wait(Until.hasObject(By.pkg("com.spintly.smartacccessandroidv3.debug")), 5000)
    }


    fun ensureAppInForeground(appLabel: String = "Smart Access") {
        try {
            resumeAppFromRecents(appLabel)
        } catch (e: AssertionError) {
            println("App not found in recents. Launching fresh...")
            launchApp()
        }
    }


    fun showToast(message: String, durationInMillis: Long = 3500L) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            Toast.makeText(
                InstrumentationRegistry.getInstrumentation().targetContext,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
        Thread.sleep(durationInMillis)
    }

    private fun login(emailOrPhone: String, password: String) {
        device.wait(Until.hasObject(By.textContains("Welcome to")), 5000)
        val userField = device.findObject(By.clazz("android.widget.EditText")) ?: return
        userField.click()
        userField.setText(emailOrPhone)

        val nextButton = device.findObject(By.text("Next")) ?: return
        nextButton.click()

        device.wait(Until.hasObject(By.textContains("Enter password")), 3000)
        val passwordField =
            device.wait(Until.findObject(By.clazz("android.widget.EditText")), 5000) ?: return
        passwordField.click()
        passwordField.setText(password)
        Thread.sleep(500)
        device.pressBack() // hide keyboard to trigger blur
        val loginButton = device.findObject(By.text("Login")) ?: return
        loginButton.click()
        Thread.sleep(2000)
        device.pressBack()
    }

    fun fallBack() {
        while (!device.hasObject(By.textContains("User Management"))) {
            device.pressBack()
            device.wait(Until.hasObject(By.textContains("User Management")), 1000)
        }
    }

    @Test
    fun gotoAddUser() {

        assert(device.wait(Until.hasObject(By.textContains("User Management")), 2000)) {
            "User Management page not loaded"
        }

        Thread.sleep(1000)

        val addButton = device.wait(Until.findObject(By.desc("Leading Icon")), 3000)
        assert(addButton != null) { "Add User button not found" }
        Thread.sleep(500)
        addButton.click()

        Thread.sleep(1000)

        assert(device.wait(Until.hasObject(By.text("Add a User")), 3000)) {
            "Add a User dialog not displayed"
        }
    }


    @Test
    fun addAValidUser() {
        val errorLog = mutableListOf<String>()
        val passedLog = mutableListOf<String>()

        // === 1. Add User with all the fields ===
        try {
            val randomSuffix = (1000..9999).random()
            val setName = "John$randomSuffix  Stuart"
            val setEmail = "john$randomSuffix@yopmail.com"
            val setPhone = "90000000000000"

            assert(device.wait(Until.hasObject(By.textContains("User Management")), 2000)) {
                "User Management page not loaded"
            }

            val addButton = device.findObject(UiSelector().description("Leading Icon").instance(0))
            assert(addButton.exists()) { "Add User button not found" }
            addButton.click()

            Thread.sleep(500)

            assert(device.wait(Until.hasObject(By.text("Add a User")), 2000)) {
                "Add a User dialog not displayed"
            }

            Thread.sleep(500)

            val fields = device.wait(Until.findObjects(By.clazz("android.widget.EditText")), 2000)
            assert(fields.size >= 4) { "All form fields not found" }

            val name = fields[0]
            val email = fields[1]
            val phone = fields[2]
            val empCode = fields[3]

            name.setText(setName)
            email.setText(setEmail)
            phone.setText(setPhone)
            empCode.setText("101")

            val joiningDate = device.findObject(
                UiSelector().className("android.widget.TextView").text("NA").instance(0)
            )
            assert(joiningDate.exists()) { "Joining Date field not found" }
            joiningDate.click()
            device.wait(Until.findObject(By.textContains("17")), 2000).click()
            device.wait(Until.findObject(By.textContains("Ok")), 2000)?.click()

            val accessExpiry = device.findObject(
                UiSelector().className("android.widget.TextView").text("NA").instance(0)
            )
            assert(accessExpiry.exists()) { "Access Expiry field not found" }
            accessExpiry.click()
            device.wait(Until.findObject(By.textContains("19")), 2000).click()
            device.wait(Until.findObject(By.textContains("Ok")), 2000)?.click()

            val reportingDropdown = device.findObject(
                UiSelector().className("android.widget.TextView").text("NA").instance(0)
            )
            assert(reportingDropdown.exists()) { "Reporting dropdown not found" }
            reportingDropdown.click()

            val managerName = device.wait(Until.findObject(By.text("rish")), 2000)
            assert(managerName != null) { "Manager name not clickable" }
            managerName.click()

            val scrollable = UiScrollable(UiSelector().scrollable(true))
            scrollable.scrollIntoView(UiSelector().text("None"))

            val homeSite = device.findObject(By.textContains("None"))
            assert(homeSite != null) { "Home site not found" }
            Thread.sleep(300)
            homeSite.click()

            val firstFloor = device.wait(Until.findObject(By.textContains("1st floor")), 2000)
            assert(firstFloor != null) { "'1st floor' option not found" }
            firstFloor.click()

            val toggleableViews = device.findObjects(By.clickable(true)).filter { it.isCheckable }
            assert(toggleableViews.size >= 4) { "Not enough toggleable checkboxes found" }
            toggleableViews[0].click()
            toggleableViews[1].click()
            toggleableViews[2].click()
            toggleableViews[3].click()

            if (scrollable.exists()) scrollable.scrollForward()

            val genderDropdown = device.findObject(UiSelector().text("NA").instance(0))
            genderDropdown.click()
            device.wait(Until.findObject(By.textContains("Male")), 2000).click()

            val officeDropdown = device.findObject(UiSelector().text("NA").instance(0))
            officeDropdown.click()
            device.wait(Until.findObject(By.textContains("Goa")), 2000).click()

            val oldDropdown = device.findObject(UiSelector().text("NA").instance(0))
            oldDropdown.click()
            device.wait(Until.findObject(By.textContains("Old")), 2000)
            device.wait(Until.findObject(By.textContains("Old1")), 2000).click()

            device.wait(Until.findObject(By.textContains("Next")), 2000).click()
            device.wait(Until.hasObject(By.textContains("Privileges")), 2000)

            device.wait(Until.findObject(By.textContains("Next")), 2000).click()
            device.wait(Until.hasObject(By.textContains("Assigned doors")), 2000)

            device.wait(Until.findObject(By.textContains("Next")), 2000).click()
            device.wait(Until.hasObject(By.textContains("User Management")), 3000)

            val addedUser = device.wait(Until.findObject(By.textContains(setName)), 1500)
            restoreToUserManagement()
            if (addedUser != null) {
                passedLog.add("✅ User added with Name, Email, and Home Site")
            } else {
                errorLog.add("❌ User with all the fields entered not found after creation")
            }

        } catch (e: Exception) {
            errorLog.add("❌ Test failed: User is not displayed in the User Management list! ${e.message}")
        }

        // === 2. Add user with only required fields ==
        try {
            val randomSuffix = (1000..9999).random()
            val setName = "Jake$randomSuffix"
            Thread.sleep(2000)

            val addButton = device.findObject(UiSelector().description("Leading Icon").instance(0))
            assert(addButton.exists()) { "Add User button not found" }
            addButton.click()

            Thread.sleep(500)

            assert(device.wait(Until.hasObject(By.text("Add a User")), 2000)) {
                "Add a User dialog not displayed"
            }

            val fields = device.wait(Until.findObjects(By.clazz("android.widget.EditText")), 2000)
            assert(fields.size >= 4) { "All form fields not found" }

            val name = fields[0]
            val email = fields[1]
            val phone = fields[2]
            val empCode = fields[3]

            name.setText(setName)
            val scrollable = UiScrollable(UiSelector().scrollable(true))
            scrollable.scrollIntoView(UiSelector().text("None"))

            val homeSite = device.findObject(By.textContains("None"))
            assert(homeSite != null) { "Home site not found" }
            Thread.sleep(300)
            homeSite.click()

            val firstFloor = device.wait(Until.findObject(By.textContains("1st floor")), 2000)
            assert(firstFloor != null) { "'1st floor' option not found" }
            firstFloor.click()
            Thread.sleep(300)
            device.wait(Until.findObject(By.textContains("Next")), 2000).click()
            device.wait(Until.hasObject(By.textContains("Privileges")), 2000)
            Thread.sleep(300)
            device.wait(Until.findObject(By.textContains("Next")), 2000).click()
            device.wait(Until.hasObject(By.textContains("Assigned doors")), 2000)
            Thread.sleep(300)
            device.wait(Until.findObject(By.textContains("Next")), 2000).click()
            device.wait(Until.hasObject(By.textContains("User Management")), 3000)

            val addedUser = device.wait(Until.findObject(By.textContains(setName)), 1500)
            restoreToUserManagement()
            if (addedUser != null) {
                passedLog.add("✅ User added with Name, Email, and Home Site")
            } else {
                errorLog.add("❌ User with only required fields not found after creation")
            }
        } catch (e: Exception) {
            errorLog.add("❌ Test failed: User is not displayed in the User Management list! ${e.message}")
        }

        // === 3. Add user with name, email and homesite ==
        try {
            val randomSuffix = (1000..9999).random()
            val setName = "Jake$randomSuffix"
            val setEmail = "jake$randomSuffix@yopmail.com"


            val addButton = device.findObject(UiSelector().description("Leading Icon").instance(0))
            addButton.click()
            device.wait(Until.hasObject(By.text("Add a User")), 2000)
            Thread.sleep(500)

            val fields = device.wait(Until.findObjects(By.clazz("android.widget.EditText")), 2000)
            val name = fields[0]
            name.setText(setName)
            Thread.sleep(200)
            val email = fields[1]
            email.setText(setEmail)

            val scrollable = UiScrollable(UiSelector().scrollable(true))
            scrollable.scrollIntoView(UiSelector().text("None"))

            val homeSite = device.findObject(By.textContains("None"))
            Thread.sleep(300)
            homeSite.click()
            val firstFloor = device.wait(Until.findObject(By.textContains("1st floor")), 2000)
            firstFloor.click()

            device.wait(Until.findObject(By.textContains("Next")), 2000).click()
            device.wait(Until.hasObject(By.textContains("Privileges")), 2000)
            device.wait(Until.findObject(By.textContains("Next")), 2000).click()
            device.wait(Until.hasObject(By.textContains("Assigned doors")), 2000)
            device.wait(Until.findObject(By.textContains("Next")), 2000).click()
            device.wait(Until.hasObject(By.textContains("User Management")), 3000)

            val addedUser = device.wait(Until.findObject(By.textContains(setName)), 1500)
            restoreToUserManagement()
            if (addedUser != null) {
                passedLog.add("✅ User added with Name, Email, and Home Site")
            } else {
                errorLog.add("❌ User with Name, Email, and Home Site not found after creation")
            }

        } catch (e: Exception) {
            errorLog.add("❌ Adding user with Name, Email, and Home Site failed: ${e.message}")
        }

        println("\n--------- TEST RESULTS ---------")
        passedLog.forEach { println(it) }
        errorLog.forEach { println(it) }

        val finalLog = mutableListOf<String>()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        finalLog.add("===== TEST RUN AT: $timestamp =====")
        finalLog.add("---- ✅ PASSED ----")
        finalLog.addAll(passedLog)
        finalLog.add("---- ❌ FAILED ----")
        finalLog.addAll(errorLog)
        finalLog.add("\n")

        saveTestLogs(finalLog)

        assert(errorLog.isEmpty()) {
            "\nSome steps failed:\n" + errorLog.joinToString("\n")
        }
    }

    @Test
    fun addUserValidation() {
        gotoAddUser()
        val errorLog = mutableListOf<String>()
        val passedLog = mutableListOf<String>()

        val randomSuffix = (1000..9999).random()
        val setName = "John$randomSuffix"
        val setEmail = "john$randomSuffix@yopmail.com"

        try {
            val scrollable = UiScrollable(UiSelector().scrollable(true))

            // === 1. Name field empty ===
            try {
                val (name, _, _, _) = getFormFields()
                name.setText(" ")
                device.findObject(By.textContains("Next")).click()
                val nextPage = device.wait(Until.findObject(By.textContains("Privileges")), 2000)
                restoreState()
                if (nextPage != null) errorLog.add("❌ Name field accepted blank input")
                else passedLog.add("✅ Name empty test passed")
            } catch (e: Exception) {
                errorLog.add(" Name empty test failed: ${e.message}")
            }

            // === 2. Homesite not selected ===
            try {
                val (name, _, _, _) = getFormFields()
                name.setText(setName)
                scrollable.scrollIntoView(UiSelector().text("None"))
                device.findObject(By.textContains("Next")).click()
                val nextPage = device.wait(Until.findObject(By.textContains("Privileges")), 2000)
                restoreState()
                if (nextPage != null) errorLog.add("❌ Homesite not selected but moved ahead")
                else passedLog.add("✅ Homesite empty test passed")
            } catch (e: Exception) {
                errorLog.add("❌ Homesite empty test failed: ${e.message}")
            }

            // === 3. Invalid email - case 1 ===
            try {
                val (name, email, _, _) = getFormFields()
                name.setText(setName)
                val homeSite = device.findObject(By.textContains("None"))
                homeSite?.click()
                device.wait(Until.findObject(By.textContains("1st floor")), 2000)?.click()
                scrollable.scrollIntoView(UiSelector().textContains("Email"))
                val emailInput1 = "John"
                val expectedError = "Invalid email"
                email.setText(emailInput1)
                device.findObject(By.textContains("Next")).click()
                restoreState()
                val errorText = device.findObject(By.textContains(expectedError))
                if (errorText == null) errorLog.add("❌ '$emailInput1' didn't show error '$expectedError'")
                else passedLog.add("✅ '$emailInput1' correctly showed error '$expectedError'")
            } catch (e: Exception) {
                errorLog.add("❌ Invalid email test 1 failed: ${e.message}")
            }

            // === 4. Invalid email - case 2 ===
            try {
                val (_, email, _, _) = getFormFields()
                val emailInput2 = "john@spintly"
                val expectedError = "Invalid email"
                email.setText(emailInput2)
                device.findObject(By.textContains("Next")).click()
                restoreState()
                val errorText = device.findObject(By.textContains(expectedError))
                if (errorText == null) errorLog.add("❌ '$emailInput2' didn't show error '$expectedError'")
                else passedLog.add("✅ '$emailInput2' correctly showed error '$expectedError'")
            } catch (e: Exception) {
                errorLog.add("❌ Invalid email test 2 failed: ${e.message}")
            }

            // === 5. Invalid phone number ===
            try {
                val (_, email, phone, _) = getFormFields()
                val phNum = "9960819"
                val expectedError = "Invalid phone"
                email.setText(setEmail)  // Valid email
                phone.setText(phNum)
                device.findObject(By.textContains("Next")).click()
                restoreState()
                val errorText = device.findObject(By.textContains(expectedError))
                if (errorText == null) errorLog.add("❌ '$phNum' didn't show error '$expectedError'")
                else passedLog.add("✅ '$phNum' correctly showed error '$expectedError'")
            } catch (e: Exception) {
                errorLog.add("❌ Invalid phone test failed: ${e.message}")
            }

            // === 6. Check whether access expiry is greater than the joining date ===
            try {
                val joiningDateDropdown = device.findObject(
                    UiSelector().className("android.widget.TextView").instance(6)
                )
                joiningDateDropdown.click()
                val pickDate = device.wait(Until.findObject(By.textContains("24")), 2000)
                pickDate.click()
                device.findObject(By.textContains("Ok")).click()
                device.wait(Until.findObject(By.textContains("Access Expiry")), 1000)
                val accessExpiryDropdown = device.findObject(
                    UiSelector().className("android.widget.TextView").instance(8)
                )
                accessExpiryDropdown.click()
                val pickDate2 = device.wait(Until.findObject(By.textContains("15")), 2000)
                pickDate2.click()
                device.findObject(By.textContains("Ok")).click()

                val joiningField = device.findObject(
                    UiSelector().className("android.widget.TextView").instance(6)
                )
                val expiryField = device.findObject(
                    UiSelector().className("android.widget.TextView").instance(8)
                )

                val joiningDateText = joiningField.text.trim()
                val accessExpiryText = expiryField.text.trim()

                println(joiningDateText)
                println(accessExpiryText)
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
                val joiningDate = formatter.parse(joiningDateText)
                val accessExpiryDate = formatter.parse(accessExpiryText)

                if (joiningDate != null && accessExpiryDate != null) {
                    if (accessExpiryDate.after(joiningDate)) {
                        println("✅ Access Expiry is after Joining Date — PASS")
                    } else {
                        errorLog.add("❌ Access Expiry is before or same as Joining Date — FAIL")
                    }
                } else {
                    errorLog.add("❌ Failed to parse one or both dates")
                }

            } catch (e: Exception) {
                errorLog.add("❌ Joining date is less than expiry date test failed: ${e.message}")
            }

            // === 7. Check whether the joining date format is as expected ===
            try {
                val dateField = device.findObject(
                    UiSelector().className("android.widget.TextView").instance(6)
                ).text.trim()

                val expectedFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
                expectedFormat.isLenient = false // Strict parsing

                expectedFormat.parse(dateField)
                passedLog.add("✅ Joining date format '$dateField' is correct")
            } catch (e: ParseException) {
                errorLog.add("❌ Joining date format is incorrect: ${e.message}")
            } catch (e: Exception) {
                errorLog.add("❌ Joining date format validation test failed: ${e.message}")
            }

            // === 8. Check whether the expiry date format is as expected ===
            try {
                val dateField = device.findObject(
                    UiSelector().className("android.widget.TextView").instance(8)
                ).text.trim()

                val expectedFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
                expectedFormat.isLenient = false // Strict parsing

                expectedFormat.parse(dateField)
                passedLog.add("✅ Access Expiry date format '$dateField' is correct")
            } catch (e: ParseException) {
                errorLog.add("❌ Access Expiry date format is incorrect: ${e.message}")
            } catch (e: Exception) {
                errorLog.add("❌ Access Expiry date format validation test failed: ${e.message}")
            }

            // === 9. Check whether the probation period is less than 365 days ===
            try {
                val scrollable = UiScrollable(UiSelector().scrollable(true))
                scrollable.scrollIntoView(UiSelector().textContains("On Probation"))

                val allViews = device.wait(Until.findObjects(By.clickable(true)), 2000)
                val toggleableViews = allViews.filter { it.isCheckable }

                toggleableViews[0].click()

                val probationField = device.findObject(
                    UiSelector().className("android.widget.EditText").text("30")
                )
                probationField.click()
                probationField.setText("390")


                val nextButton = device.findObject(UiSelector().text("Next"))
                nextButton.click()

                val errorMessage = device.wait(
                    Until.findObject(By.textContains("max probation  period can be 365 only")),
                    2000
                )

                val samePageIndicator =
                    device.findObject(UiSelector().text("Probation")) // replace with unique element on current page
                val nextPageIndicator =
                    device.findObject(UiSelector().text("Access Control")) // replace with unique element on *next* page
                restoreState()

                if (errorMessage != null) {
                    println("✅ Error message detected — validation working.")
                } else if (nextPageIndicator.exists()) {
                    errorLog.add("❌ App navigated to next page despite invalid probation period.")
                } else if (samePageIndicator.exists()) {
                    println("✅ Stayed on the same page — form submission blocked as expected.")
                } else {
                    errorLog.add("❌ Error message for probation periood is not visible.")
                }
            } catch (e: Exception) {
                errorLog.add("❌ Probation period test crashed: ${e.message}")
            }
            // === 10. Check whether the joining date can be past, present and the future dates can be selected ===
            try {
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
                val calendar = Calendar.getInstance()
                val today = calendar.get(Calendar.DAY_OF_MONTH)


                // --- (a) Past Date ---
                val pastDay = (today - 1).toString()
                println(pastDay)
                val joiningDateDropdown = device.findObject(
                    UiSelector().className("android.widget.TextView").instance(6)
                )
                joiningDateDropdown.click()
                val pickDate = device.wait(Until.findObject(By.textContains(pastDay)), 2000)
                pickDate.click()
                device.findObject(By.text("Ok")).click()
                val pastDateText = joiningDateDropdown.text.trim()
                val pastDate = formatter.parse(pastDateText)
                if (pastDate.before(calendar.time)) {
                    passedLog.add("✅ Past date selected successfully for joining date")
                } else {
                    errorLog.add("❌ Failed: Past date not set properly")
                }

                // --- (b) Present Date ---
                joiningDateDropdown.click()
                val presentDay = today.toString()
                val pickDate2 = device.wait(Until.findObject(By.textContains(presentDay)), 2000)
                pickDate2.click()
                device.findObject(By.text("Ok")).click()
                val presentDateText = joiningDateDropdown.text.trim()
                val presentDate = formatter.parse(presentDateText)
                val diff = abs(calendar.time.time - presentDate.time)
                if (diff < 24 * 60 * 60 * 1000) {
                    passedLog.add("✅ Present date selected successfully for joining date")
                } else {
                    errorLog.add("❌ Failed: Present date not set properly")
                }

                // --- (c) Future Date ---
                joiningDateDropdown.click()
                val futureDay = (today + 1).toString()
                val pickDate3 = device.wait(Until.findObject(By.textContains(futureDay)), 2000)
                pickDate3.click()
                device.findObject(By.text("Ok")).click()
                val futureDateText = joiningDateDropdown.text.trim()
                val futureDate = formatter.parse(futureDateText)
                if (futureDate.after(calendar.time)) {
                    passedLog.add("✅ Future date selected successfully for joining date")
                } else {
                    errorLog.add("❌ Failed: Future date not set properly")
                }

            } catch (e: Exception) {
                errorLog.add("❌ Joining date past/present/future selection test failed: ${e.message}")
            }

            // === 11. Check whether the expiry date cannot be past and present ===
            try {
                val formatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
                val calendar = Calendar.getInstance()
                val today = calendar.get(Calendar.DAY_OF_MONTH)

                val expiryDateDropdown = device.findObject(
                    UiSelector().className("android.widget.TextView").instance(8)
                )

                // Store original date to compare later
                val originalDate = expiryDateDropdown.text.trim()

                // --- (a) Try to select Past Date ---
                val pastDay = (today - 1).toString()
                expiryDateDropdown.click()
                val pickPast = device.wait(Until.findObject(By.textContains(pastDay)), 2000)
                pickPast?.click()
                device.findObject(By.text("Ok")).click()
                val updatedPastDate = expiryDateDropdown.text.trim()

                if (updatedPastDate != originalDate) {
                    errorLog.add("❌ Past date was incorrectly allowed for Access Expiry Date")
                } else {
                    passedLog.add("✅ Past date was correctly blocked for Access Expiry Date")
                }

            } catch (e: Exception) {
                errorLog.add("❌ Access Expiry past date restriction test failed: ${e.message}")
            }

            // === Final Result Summary and Single Assert ===
            println("\n--------- TEST RESULTS ---------")
            passedLog.forEach { println(it) }
            errorLog.forEach { println(it) }

            val finalLog = mutableListOf<String>()
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
            finalLog.add("===== TEST RUN AT: $timestamp =====")
            finalLog.add("---- ✅ PASSED ----")
            finalLog.addAll(passedLog)
            finalLog.add("---- ❌ FAILED ----")
            finalLog.addAll(errorLog)
            finalLog.add("\n")

            saveTestLogs(finalLog)

            assert(errorLog.isEmpty()) {
                "\nSome validations failed:\n" + errorLog.joinToString("\n")
            }

        } catch (e: Exception) {
            errorLog.add("❌ Unexpected error during setup: ${e.message}")
            assert(false) { "❌ Test aborted due to unexpected setup failure:\n${e.message}" }
        }
    }

    @Test
    fun runAllTests(){
        addAValidUser()
        gotoAddUser()
        Thread.sleep(500)
        addUserValidation()
    }

    @Test
    fun pastDate() {
        val joiningDateDropdown = device.findObject(
            UiSelector().className("android.widget.TextView").instance(6)
        )
        joiningDateDropdown.click()
        val pickDate = device.wait(Until.findObject(By.textContains("24")), 2000)
        pickDate.click()
    }

    @Test
    fun toFInd() {
        for (i in 0..30) {
            Thread.sleep(500)
            try {
                val obj = device.findObject(UiSelector().instance(i))
                println("[$i] class: ${obj.className}, desc: ${obj.contentDescription}, text: ${obj.text}")
            } catch (_: Exception) {
            }
        }
    }

    @Test
    fun findButton() {
        val buttons = device.findObjects(By.clazz("android.widget.Text"))

        for ((index, button) in buttons.withIndex()) {
            val text = button.text ?: "NO_TEXT"
            val bounds = button.visibleBounds
            val isClickable = button.isClickable
            val isEnabled = button.isEnabled

            println("[$index] BUTTON => text: $text | bounds: $bounds | clickable: $isClickable | enabled: $isEnabled")
        }
    }

    @Test
    fun findToggle() {
        val clickableViews = device.findObjects(By.clickable(true))
        println("Clickable views count: ${clickableViews.size}")

        for ((i, view) in clickableViews.withIndex()) {
            println("[$i] Class: ${view.className}, Text: ${view.text}, ContentDesc: ${view.contentDescription}")
        }
    }

    @Test
    fun logAllClickableViews() {
        val allViews = device.findObjects(By.clickable(true))
        val toggleableViews = allViews.filter { it.isCheckable }

        toggleableViews[1].click()
    }


    @Test
    // === 11. Check whether the expiry date cannot be past and present ===
    fun clickExpiry(){
        val errorLog = mutableListOf<String>()
        val passedLog = mutableListOf<String>()

        try{
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
            val calendar = Calendar.getInstance()
            val today = calendar.get(Calendar.DAY_OF_MONTH)

            val expiryDateDropdown = device.findObject(
                UiSelector().className("android.widget.TextView").instance(8)
            )

            // Store original date to compare later
            val originalDate = expiryDateDropdown.text.trim()

            // --- (a) Try to select Past Date ---
            val pastDay = (today - 1).toString()
            expiryDateDropdown.click()
            val pickPast = device.wait(Until.findObject(By.textContains(pastDay)), 2000)
            pickPast?.click()
            device.findObject(By.text("Ok")).click()
            val updatedPastDate = expiryDateDropdown.text.trim()

            if (updatedPastDate != originalDate) {
                errorLog.add("❌ Past date was incorrectly allowed for Access Expiry Date")
            } else {
                passedLog.add("✅ Past date was correctly blocked for Access Expiry Date")
            }

            // --- (b) Try to select Present Date ---
            expiryDateDropdown.click()
            val presentDay = today.toString()
            val pickPresent = device.wait(Until.findObject(By.textContains(presentDay)), 2000)
            pickPresent?.click()
            device.findObject(By.text("Ok")).click()
            val updatedPresentDate = expiryDateDropdown.text.trim()

            if (updatedPresentDate != originalDate) {
                errorLog.add("❌ Present date was incorrectly allowed for Access Expiry Date")
                println(errorLog)
            } else {
                passedLog.add("✅ Present date was correctly blocked for Access Expiry Date")
            }

        } catch (e: Exception)
        {
            errorLog.add("❌ Access Expiry past/present date restriction test failed: ${e.message}")
        }
    }


}
