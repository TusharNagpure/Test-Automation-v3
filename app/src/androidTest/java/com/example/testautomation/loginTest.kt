package com.example.testautomation
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.concurrent.thread
import android.widget.Toast
import androidx.test.uiautomator.UiSelector
import kotlin.math.log
import kotlin.math.sign

@RunWith(AndroidJUnit4::class)
class MyAutomationTest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressHome()
    }

    private fun launchApp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
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
        device.wait(Until.hasObject(By.textContains("Welcome to")),5000)
        val userField = device.findObject(By.clazz("android.widget.EditText")) ?: return
        userField.click()
        userField.setText(emailOrPhone)

        val nextButton = device.findObject(By.text("Next")) ?: return
        nextButton.click()

        device.wait(Until.hasObject(By.textContains("Enter password")),3000)
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

    fun logout(){
        val consoles = device.wait(Until.findObject(By.textContains("Consoles")),3000)
        consoles.click()
        val moreOptions = device.findObject(UiSelector().description("More options"))
        moreOptions.click()
        val logoutButton = device.wait(Until.findObject(By.textContains("Logout")),2000)
        logoutButton.click()

        val page = device.wait(Until.findObject(By.textContains("Welcome to")),3000)
        assert(page != null){
            "FAIL : Could'nt find the login page after the logout"
        }
    }

    fun countryCodesSelect(code : String) {
        launchApp()
        val userField = device.wait(Until.findObject(By.clazz("android.widget.EditText")), 2000)
        assert(userField != null) { " FAIL: Phone input field not found" }
        userField.click()
        userField.setText("99")
        val dropDown = device.wait(Until.findObject(By.textContains("+")), 2000)
        assert(dropDown != null) { " FAIL: Country code dropdown not found" }
        dropDown.click()
        val countrySel = device.wait(Until.findObject(By.textContains(code)), 3000)
        assert(countrySel != null) { " FAIL: +93 not found in country list" }
        countrySel.click()

        // Step 4: Wait for "Next" to confirm UI returned
        device.wait(Until.hasObject(By.textContains("Next")), 2000)

        println("PASS: Country code '+93' selected successfully")
    }

    @Test
    //TC number: 908052
    fun loginWithEmail() {
        launchApp()
        login("yyy@yopmail.com", "test1234")
        Thread.sleep(2000)
        val homeScreen = device.wait(Until.findObject(By.textContains("Consoles")), 200)
        assert(homeScreen != null)
        logout()
    }

    @Test
    //TC number: 908039
    fun loginWithPhone() {
        launchApp()
        login("9960819344", "12345678")
        val homeScreen = device.wait(Until.findObject(By.textContains("Consoles")), 5000)
        assert(homeScreen != null) { "FAIL: Home screen not displayed after login" }
        logout()
    }

    @Test
    // TC number: 908049
    fun loginAsSuperAdmin(){
        device.pressBack()
        launchApp()
        login("9960819344", "12345678")
        val homeScreen = device.wait(Until.findObject(By.textContains("Consoles")), 5000)
        assert(homeScreen != null) { "FAIL: Home screen not displayed after login" }
        logout()
    }

    @Test
    // TC number: 908050
    fun loginAsSiteAdmin(){
        launchApp()
        login("yzy@yopmail.com","12345678")
        val homeScreen = device.wait(Until.findObject(By.textContains("Consoles")), 5000)
        assert(homeScreen != null) { "FAIL: Home screen not displayed after login" }
        logout()
    }

    @Test
    // TC number: 908051
    fun loginAsManager(){
        launchApp()
        login("jjj@yopmail.com","12345678")
        val homeScreen = device.wait(Until.findObject(By.textContains("Consoles")), 5000)
        assert(homeScreen != null) { "FAIL: Home screen not displayed after login" }
        logout()
    }

    @Test
    // TC number: 908053
    fun loginAsFrontdesk(){
        launchApp()
        login("ypp@yopmail.com","12345678")
        val homeScreen = device.wait(Until.findObject(By.textContains("Consoles")), 5000)
        assert(homeScreen != null) { "FAIL: Home screen not displayed after login" }
        logout()
    }
    @Test
    //TC number: 908014,908038// manual otp required
    fun loginUsingOTP(){
        launchApp()
        val phone = "9960819344"
        val userField = device.wait(Until.findObject(By.clazz("android.widget.EditText")),4000)
        userField.click()
        userField.setText(phone)

        val nextButton = device.findObject(By.text("Next"))
        nextButton.click()
        device.wait(Until.hasObject(By.textContains("Login with OTP")),2000)
        val loginWithOTP = device.findObject(By.text("Login with OTP"))
        loginWithOTP.click()
        val dashboard = device.wait(Until.findObject(By.textContains("Consoles")),30000)
        assert(dashboard != null ){ "FAIL: Home screen not displayed after login" }
        logout()
    }

    @Test
    fun loginAsSSO(){
        launchApp()
        device.wait(Until.hasObject(By.textContains("Welcome to")),5000)
        val ssoButton = device.findObject(By.textContains("SSO Login"))
        ssoButton.click()
        device.wait(Until.hasObject(By.textContains("Welcome to")),2000)
        val email = "ssotestuser1@infospintly.onmicrosoft.com"
        val userField = device.findObject(By.clazz("android.widget.EditText")) ?: return
        userField.click()
        userField.setText(email)
        device.pressBack()
        val nextButton = device.wait(Until.findObject(By.clazz("android.widget.Button")),2000)
        nextButton.click()
        println("Current package: ${device.currentPackageName}")

        device.wait(Until.hasObject(By.pkg("com.android.chrome")), 8000)
        val user = device.wait(Until.findObject(By.textContains("ssotestuser1")),5000)
        user.click()
        showToast("Please click the CONTINUE button manually in Chrome", 4000)
        device.wait(Until.hasObject(By.textContains("Smart Access V3")),10000)
        val clickApp = device.wait(Until.findObject(By.textContains("Smart Access V3")),4000)
        clickApp.click()
        val dashboard = device.wait(Until.findObject(By.textContains("Consoles")),7000)
        assert(dashboard != null){
            "FAIL: Could'nt find the dashboard"
        }
        println("PASS: Logged in successfully")
        logout()
    }

    fun enterEmail(email: String) {
        val userField = device.findObject(By.clazz("android.widget.EditText"))
        userField.click()
        userField.setText(email)
        device.pressBack()

        val nextButton = device.findObject(By.text("Next"))
        nextButton.click()
        Thread.sleep(1000)
    }

    @Test
    //TC: 908007
    fun testInvalidEmail() {
        launchApp()
        val emailInput1 = "John"
        val emailInput2 = "john@spintly"
        val expectedError = "Invalid email"
        device.wait(Until.hasObject(By.textContains(expectedError)), 1000)
        enterEmail(emailInput1)
        val errorText = device.findObject(By.textContains(expectedError))

        assert(errorText != null) {
            "FAIL: Expected '$expectedError' for input '$emailInput1'"
        }
        println(" PASS: '$emailInput1' correctly showed error '$expectedError'")

        enterEmail(emailInput2)
        val errorText2 = device.findObject((By.textContains(expectedError)))

        assert(errorText2 != null) {
            "FAIL: Expected '$expectedError' for input '$emailInput2'"
        }
        println(" PASS: '$emailInput2' correctly showed error '$expectedError'")

    }

    @Test
    //TC: 908008
    fun testEmptyField() {
        launchApp()
        val emailInput = ""
        val expectedError = "Email or Phone number is required"
        device.wait(Until.hasObject(By.textContains(expectedError)), 1000)
        enterEmail(emailInput)
        val errorText = device.findObject(By.textContains(expectedError))

        assert(errorText != null) {
            "FAIL: Expected $expectedError for input $emailInput"
        }
        println(" PASS: '$emailInput' correctly showed error '$expectedError'")
    }

    @Test
    // TC: 908009
    fun testCorrectEmail() {
        launchApp()
        val emailInput = "test@gmail.com"
        device.wait(Until.hasObject(By.textContains("Email")), 1000)
        enterEmail(emailInput)
        val errorText = device.findObject(By.textContains("Invalid email"))

        assert(errorText == null) {
            "FAIL for input $emailInput"
        }
        println(" PASS: '$emailInput' correct ")
        device.pressBack()
        device.pressBack()
    }

    fun signup(){
        launchApp()
        val emailInput = "uuu@yopmail.com"
        device.wait(Until.hasObject(By.textContains("Email")), 1000)
        enterEmail(emailInput)
        showToast("Enter OTP manually within 60 secs",3000)
        device.wait(Until.hasObject(By.textContains("Verify Your Details")),60000)
    }

    @Test
    // TC: 908012,908017,90801l
    fun otpOnEmailSignup() {
        launchApp()
        val emailInput = "uuu@yopmail.com"
        device.wait(Until.hasObject(By.textContains("Email")), 1000)
        enterEmail(emailInput)
        device.wait(Until.hasObject(By.textContains("Verify your email")), 2000)
        val otpBoxes = device.findObject(By.clazz("android.widget.EditText"))
        otpBoxes.setText("123") // this will handle the case of empty as well as incomplete length
        val continueBox = device.findObject(By.textContains("Continue"))
        device.wait(Until.hasObject(By.textContains("Continue")), 2000)
        continueBox.click()
        device.wait(Until.hasObject(By.textContains("incorrect otp")), 2000)
        val message = device.findObject(By.textContains("Incorrect otp"))

        assert(message == null) {
            "FAIL for input $emailInput"
        }
        println("PASS: Cannot enter incomplete OTP")

        otpBoxes.setText("123456")
        device.wait(Until.hasObject(By.textContains("Continue")), 2000)
        continueBox.click()
        device.wait(Until.hasObject(By.textContains("Incorrect otp")), 3000)
        val message1 = device.hasObject(By.textContains("incorrect OTP"))

        device.pressBack()
        device.pressBack()

        assert(message1) {
            "FAIL for input $emailInput"
        }
        println("PASS: 6 digit otp validated")

    }

    @Test
    // TC: 908037 fails for less than 6 it shows  user does not exists....
    fun phoneValidation() {
        launchApp()
        val phNum1 = "9960819"
        val phNum2 = "9960819344"
        val expectedError = "Invalid phone number"
        device.wait(Until.hasObject(By.textContains("Invalid phone number")), 1000)
        enterEmail(phNum1)
        val errorText = device.findObject(By.textContains(expectedError))

        assert(errorText != null) {
            "FAIL: Expected $expectedError for input $phNum1"
        }
        println(" PASS: '$phNum1' correctly showed error '$expectedError'")

        enterEmail(phNum2)
        device.wait(Until.hasObject(By.textContains("Invalid phone number")), 1000)

        val errorText2 = device.findObject(By.textContains(expectedError))

        assert(errorText2 == null) {
            "FAIL: Expected $expectedError for input $phNum2 what it showed $errorText2"
        }
        println(" PASS: '$phNum2' correctly showed error '$expectedError'")
    }

    @Test
    //TC: 908041
    fun inputWithSpaces() {
        launchApp()
        val emailInput = "  xyz@gmail.co  m"
        val phNum = "99 60 8193 33"
        device.wait(Until.hasObject(By.textContains("Invalid")), 1000)
        enterEmail(emailInput)
        val errorText = device.findObject(By.textContains("Invalid"))

        assert(errorText == null) {
            "FAIL: for the input $emailInput it showed: $errorText instead of invalid"
        }
        println(" PASS: '$emailInput' correct")

        Thread.sleep(2000)

        enterEmail(phNum)
        device.wait(Until.hasObject(By.textContains("Invalid")), 1000)
        val errorText2 = device.findObject(By.textContains("Invalid"))
        assert(errorText2 == null) {
            "FAIL: for the input $phNum it showed: $errorText2 instead of invalid"
        }
        println(" PASS: '$phNum' correct")
    }

    @Test
    // TC: 908002
    fun countryCodeUS() {
        launchApp()
        countryCodesSelect("+1")
        val phone_number = "9960819344"
        val expected_number = "(996) 081-9344"
        device.wait(Until.hasObject(By.textContains("Invalid")), 1000)
        val userField = device.findObject(By.clazz("android.widget.EditText"))
        userField.click()
        userField.setText(phone_number)
        device.pressBack()  // hides keyboard
        Thread.sleep(1000)
        val formattedText = userField.text

        assert(formattedText == expected_number) {
            "FAIL: Expected '$expected_number' but got '$formattedText'\""
        }
        println("println(\"PASS: Phone number formatted correctly as per US region\")")
    }

    @Test
    // TC number: 908001, 908004
    fun checkCountryCodeIndia(){
        launchApp()
        device.wait(Until.hasObject(By.textContains("Simplifying Access")), 2000)
        val field = device.findObject(By.clazz("android.widget.EditText"))
        field.click()
        field.setText("80")
        device.wait(Until.hasObject(By.textContains("+")),1000)
        val code = device.findObject(By.text("+91"))
        assert(code != null) { "FAIL: +91 not shown for India region" }
    }

    @Test
    // TC number: 908001,908004
    fun checkCountryCodeUS() {
        launchApp()
        device.wait(Until.hasObject(By.textContains("Simplifying Access")), 2000)
        val field = device.findObject(By.clazz("android.widget.EditText"))
        field.click()
        field.setText("80")
        device.wait(Until.hasObject(By.textContains("+")),1000)
        val code = device.findObject(By.text("+1"))
        assert(code != null) { "FAIL: +1 not shown for US region" }
        countryCodesSelect("+91")
    }


    @Test
    // TC:908005
    fun countryCodesSelection() {
        launchApp()
        val userField = device.wait(Until.findObject(By.clazz("android.widget.EditText")), 2000)
        assert(userField != null) { " FAIL: Phone input field not found" }
        userField.click()
        userField.setText("9960819344")
        val dropDown = device.wait(Until.findObject(By.textContains("+")), 2000)
        assert(dropDown != null) { " FAIL: Country code dropdown not found" }
        dropDown.click()
        val countrySel = device.wait(Until.findObject(By.textContains("+93")), 3000)
        assert(countrySel != null) { " FAIL: +93 not found in country list" }
        countrySel.click()

        // Step 4: Wait for "Next" to confirm UI returned
        device.wait(Until.hasObject(By.textContains("Next")), 2000)
        val code = device.findObject(By.textContains("+93"))
        assert(code != null){
            "FAIL: Cannot find the +93 in the login page"
        }

        println("PASS: Country code '+93' selected successfully")
        countryCodesSelect("+91")
    }

    @Test
    //TC: 908010
    fun otpScreenVisible() {
        launchApp()
        val phNum = "9960819344"
        val userField = device.wait(Until.findObject(By.clazz("android.widget.EditText")), 3000)
        assert(userField != null) { "FAIL: Input field not found" }
        userField.click()
        userField.setText(phNum)
        val nextButton = device.wait(Until.findObject(By.text("Next")), 3000)
        assert(nextButton != null) { "FAIL: 'Next' button not found" }
        nextButton.click()
        val clickOtp = device.wait(Until.findObject(By.textContains("Login with OTP")), 3000)
        assert(clickOtp != null) { "FAIL: 'Login with OTP' not found" }
        clickOtp.click()
        val otpPage = device.wait(Until.findObject(By.textContains("One time password")), 3000)
        assert(otpPage != null) { "FAIL: OTP page not visible" }
        println("PASS: OTP page displayed successfully")
        device.pressBack()
        device.pressBack()
    }

    @Test
    // TC number: 908011
    fun otpScreenShowsCorrectEmailorPhone() {
        launchApp()
        val phNum = "99608 19344"
        val userField = device.wait(Until.findObject(By.clazz("android.widget.EditText")), 3000)
        assert(userField != null) { "FAIL: Input field not found" }
        userField.click()
        userField.setText(phNum)
        val nextButton = device.wait(Until.findObject(By.text("Next")), 3000)
        assert(nextButton != null) { "FAIL: 'Next' button not found" }
        nextButton.click()
        val clickOtp = device.wait(Until.findObject(By.textContains("Login with OTP")), 3000)
        assert(clickOtp != null) { "FAIL: 'Login with OTP' not found" }
        clickOtp.click()
        val otpPage = device.wait(Until.findObject(By.textContains(phNum)), 3000)
        assert(otpPage != null) { "FAIL: phone number not visible" }
        println("PASS: phone number displayed successfully")

        device.pressBack()
        device.pressBack()

        val email = "yyy@yopmail.com"
        val emailField = device.wait(Until.findObject(By.clazz("android.widget.EditText")), 3000)
        assert(userField != null) { "FAIL: Input field not found" }
        emailField.click()
        emailField.setText(email)
        val nextButton2 = device.wait(Until.findObject(By.text("Next")), 3000)
        assert(nextButton != null) { "FAIL: 'Next' button not found" }
        nextButton2.click()
        val clickOtp2 = device.wait(Until.findObject(By.textContains("Login with OTP")), 3000)
        assert(clickOtp != null) { "FAIL: 'Login with OTP' not found" }
        clickOtp2.click()
        val otpPage2 = device.wait(Until.findObject(By.textContains(email)), 3000)
        assert(otpPage2 != null) { "FAIL: email not visible" }
        println("PASS: email displayed successfully")

        device.pressBack()
        device.pressBack()
    }

    @Test
    //TC number: 908015,908021
    fun enterOtpWithinOneMinuteAndCheckWhetherTimerWorksInBG(){
        launchApp()
        val editField = device.wait(Until.findObject(By.clazz("android.widget.EditText")),4000)
        editField.click()
        editField.setText("9960819344")
        device.findObject(By.textContains("Next")).click()

        device.wait(Until.findObject(By.textContains("Login with OTP")),4000).click()
        device.wait(Until.hasObject(By.textContains("One time password")),2000)

        val timerObject = device.wait(Until.findObject(By.textStartsWith("Resend OTP in")), 5000)
        val timerText = timerObject?.text

        val seconds = Regex("\\d+").find(timerText ?: "")?.value?.toIntOrNull()

        assert(seconds != null && seconds in 58..60) {
            "FAIL: Expected timer to start around 60 seconds but got: $seconds"
        }

        device.pressHome()
        Thread.sleep(5000)
        launchApp()
        val timerObject2 = device.wait(Until.findObject(By.textStartsWith("Resend OTP in")), 5000)
        val timerText2 = timerObject2?.text
        val seconds2 = Regex("\\d+").find(timerText2 ?: "")?.value?.toIntOrNull()

        assert(seconds2 != null && seconds2 < seconds!!) {
            "FAIL: Timer did not decrease in background. Before: $seconds, After: $seconds2"
        }
        device.pressBack()
        device.pressBack()
    }

    @Test
    //TC number: 908045 manual otp needs to be entered
    fun resendOTPInForgetPassword(){
        launchApp()
        val emailField = device.wait(Until.findObject(By.clazz("android.widget.EditText")),3000)
        emailField.click()
        emailField.setText("yyy@yopmail.com")

        device.findObject(By.clazz("android.widget.Button")).click()
        val forgetPass = device.wait(Until.findObject(By.textContains("Forgot Password")),3000)
        forgetPass.click()
        device.findObject(By.textContains("Resend OTP")).click()
        showToast("Enter the OTP manually in 60 secs")
        val page = device.wait(Until.findObject(By.textContains("Create Password")),60000)
        assert(page != null){
            "FAIL: Could'nt find the page"
        }
        device.pressBack()
        device.pressBack()

    }

    @Test
    //TC number: 908022
    fun userSignUpBeforeRegis() {
        launchApp()
        val emailInput = "ykdh@yop.com"
        val userField = device.wait(Until.findObject(By.clazz("android.widget.EditText")), 2000)
        assert(userField != null) {
            "Fail: User field not found"
        }
        userField.click()
        userField.setText(emailInput)
        val nextButton = device.wait(Until.findObject(By.textContains("Next")), 2000)
        assert(nextButton != null) { "FAIL: 'Next' button not found" }
        nextButton.click()

        val errorText = device.wait(
            Until.findObject(By.textContains("User does not exist. Please contact your admin")),
            4000
        )
        assert(errorText != null) {
            "FAIL: User does not exists. Please contact your admin was not found"
        }
        println("PASS: Found")
    }

    @Test/*TC number: 908023, 908024: this checks that the email, name fields and the
    password fields are avail and also checks that the name and the email gets auto populated
     Manual otp needs to be entered.*/
    fun testSignupPageFunctionalities() {
        signup()
        launchApp()
        device.wait(Until.hasObject(By.textContains("Welcome to Spintly")), 2000)
        val name = device.findObject(By.textContains("Name"))
        assert(name != null) {
            "FAIL: Unable to find Name label"
        }
        val email = device.findObject(By.textContains("Email"))
        assert(email != null) {
            "FAIL: Unable to find Email label"
        }
        val fields = device.findObjects(By.clazz("android.widget.EditText"))
        val nameField = fields[0]
        val emailField = fields[1]

        assert(nameField != null) {
            "FAIL: Name input field not found"
        }

        assert(emailField != null) {
            "FAIL: Email input field not found"
        }

        assert(nameField.text != " ") {
            "FAIL: the field is not auto populated"
        }

        assert(emailField.text != " ") {
            "FAIL: the field is not auto populated"
        }
        val nextButton = device.findObject(By.textContains("Next"))
        nextButton.click()
        device.wait(Until.hasObject(By.textContains("Password")), 2000)
        val passwordLabel = device.findObject(
            By.clazz("android.widget.TextView").text("Password")
        )
        val confirmPasswordLabel = device.findObject(
            By.clazz("android.widget.TextView").text("Confirm Password")
        )

        assert(passwordLabel.text == "Password") {
            "FAIL : unable to find the password label: ${passwordLabel.text}"
        }

        assert(confirmPasswordLabel.text == "Confirm Password") {
            "FAIL : unable to find the confirm password label"
        }

        val fields2 = device.findObjects(By.clazz("android.widget.EditText"))
        val passField = fields2[0]
        val cnfpassField = fields2[1]

        assert(passField != null) {
            "FAIL: password input field not found"
        }

        assert(cnfpassField != null) {
            "FAIL: confirm password input field not found"
        }
        device.pressBack()
        device.pressBack()
    }
    @Test
//TC number: 908036,908055 //Manual otp needs to be entered
    fun signupUsingEmail() {
        launchApp()
        device.wait(Until.hasObject(By.textContains("Welcome to")), 3000)

        val userField = device.findObject(By.clazz("android.widget.EditText"))
        userField.click()
        userField.setText("test15@yopmail.com")

        val nextButton1 = device.findObject(By.clazz("android.widget.Button"))
        nextButton1.click()

        device.wait(Until.findObject(By.textContains("Verify your email")), 2000)

        // Step 3: Wait for manual correct OTP entry
        showToast("Please enter the correct OTP manually within 60 seconds.",3000)

        // Step 4: Continue flow after correct OTP entered
        device.wait(Until.hasObject(By.textContains("Verify Your Details")), 60000)

        val nextButton2 = device.findObject(By.clazz("android.widget.Button"))
        nextButton2.click()

        device.wait(Until.hasObject(By.textContains("Create Password")), 2000)

        val fields = device.findObjects(By.clazz("android.widget.EditText"))
        val passField = fields[0]
        val cnfPassField = fields[1]

        passField.click()
        passField.setText("test1234")

        cnfPassField.click()
        cnfPassField.setText("test1234")
        device.pressBack()

        val signupButton = device.findObject(By.clazz("android.widget.Button"))
        signupButton.click()

        println("PASS: Signup completed after manually entering correct OTP.")
        val dashboard = device.wait(Until.findObject(By.textContains("Consoles")),4000)
        assert(dashboard != null){
            "Fail: Dashboard not found"
        }
        logout()
    }


    @Test
    //TC number: 908058 manual otp needs to be entered.
    fun signinUsingEmailAndOTP(){
        launchApp()
        device.wait(Until.hasObject(By.textContains("Welcome to")),3000)
        val userField = device.findObject(By.clazz("android.widget.EditText"))
        userField.click()
        userField.setText("kjk@yopmail.com")
        val nextButton1 = device.findObject(By.clazz("android.widget.Button"))
        nextButton1.click()
        device.wait(Until.findObject(By.textContains("Login with OTP")),3000).click()
        device.wait(Until.hasObject(By.textContains("One time password")),2000)
        showToast("Please enter the correct OTP manually")
        val dashboard = device.wait(Until.findObject(By.textContains("Consoles")),60000)
        assert(dashboard != null){
            "FAIL: Could'nt find the dashboard!"
        }
        logout()
    }

    @Test
// TC: 908025 & 908026 Combined requires manual otp signup
    fun verifySignupFields() {
        signup()
        // Name Field Test (908025)
        val name = "Tushar"
        val nameField = device.findObject(By.clazz("android.widget.EditText"))
        nameField.setText(name)
        assert(nameField.text != name) {
            "Fail: name can be edited: ${nameField.text}"
        }
        // Email Field Test (908026)
        val email = "xyz@gmail.com"
        val fields = device.findObjects(By.clazz("android.widget.EditText"))
        val emailField = fields[1]
        emailField.setText(email)
        assert(emailField.text != email) {
            "FAIL: the email could be changed"
        }
        println("PASS: the email could not be changed")
        device.pressBack()
    }
    @Test
    // TC: 908030(Bug found) ,908031(Bug found), 908032 requires manual otp signup
    fun PasswordValidationAtSignUp(){
        signup()
        val password = "12345"
        val passField = device.findObject(By.clazz("android.widget.EditText"))
        passField.setText("")
        val signupButton = device.findObject(By.clazz("android.widget.Button"))
        signupButton.click()
        val errorText = device.findObject(By.textContains("Enter Password"))
        assert(errorText != null){
            "Fail: Test failed due to incorrect text label written!"
        }
        passField.setText(password)
        signupButton.click()
        val errorText2 = device.findObject(By.textContains("password should atleast"))
        assert(errorText2 != null){
            "Fail: Test failed due to incorrect text label written!"
        }
        println("Pass: All testcases passed")

        val password2 = "12345789#@@***f"
        passField.setText(password2)

        assert(password2 == passField.text){
            "Fail: Cannot enter more than 8 char or special chars"
        }
        device.pressBack()
        device.pressBack()
    }
    @Test
    //TC: 908033 requires manual otp signup
    fun MatchingOfPasswordsAtSignup(){
        signup()
        device.findObject(By.clazz("android.widget.Button"))
        device.wait(Until.hasObject(By.textContains("Password")),3000)
        val pass = "12345678"
        val cnfPass = "87654321"

        val field = device.findObjects(By.clazz("android.widget.EditText"))
        val passField = field[0]
        val cnfPassField = field[1]

        passField.setText(pass)
        cnfPassField.setText(cnfPass)

        val signupButton = device.findObject(By.clazz("android.widget.Button"))
        signupButton.click()
        val errorText = device.findObject(By.textContains("Password do not match"))
        assert(errorText != null){
            "Fail"
        }
        println("Pass")
        device.pressBack()
        device.pressBack()
    }

    @Test
    //TC number: 908040,908042
    fun testIncorrectPasswordAtLoginWithSpaces(){
        launchApp()
        login("9960819344","1234567 7888")
        device.wait(Until.hasObject(By.textContains("Incorrect password")),2000)
        val errorText = device.findObject(By.textContains("Incorrect password"))
        device.pressBack()
        device.pressBack()
        assert(errorText != null){
            "FAIL: didnt show incorrect password"
        }
        println("PASS")
    }

    @Test
    //TC number: 908043,908044,908046,908047 // requires otp manual otp login
    fun resetPassword() {
        launchApp()
        device.wait(Until.hasObject(By.textContains("Welcome to")), 3000)

        device.findObject(By.clazz("android.widget.EditText")).apply {
            click()
            setText("yyy@yopmail.com")
        }

        device.findObject(By.clazz("android.widget.Button")).click()

        device.wait(Until.findObject(By.textContains("Forgot Password")), 2000)?.click()
        device.wait(Until.hasObject(By.textContains("Create Password")), 60000)

        val fields = device.findObjects(By.clazz("android.widget.EditText"))
        fields[0].setText("test1234")
        fields[1].setText("test123")

        device.findObject(By.clazz("android.widget.Button")).click()

        val errorText = device.findObject(By.textContains("Password do not match"))
        assert(errorText != null) { "FAIL: Unable to see the error text" }

        fields[0].setText("test12345")
        fields[1].setText("test12345")

        device.findObject(By.clazz("android.widget.Button")).click()

        val dashboard = device.wait(Until.findObject(By.textContains("For the")), 3000)
        assert(dashboard != null) { "FAIL: Unable to open the dashboard" }

        println("PASS: Dashboard was opened successfully")
    }

    @Test
    fun logoutTest(){
        launchApp()
        logout()
    }

    @Test
    fun loginAndLogout(){
        launchApp()
        login("9960819344","12345678")
        device.pressBack()
        logout()
    }
}
