package app;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FeedbackSystemTest {
    static WebDriver driver;
    static final String BASE = "http://localhost:8080";
    static final int SHOW_RESULTS_SECONDS = 5; // how long Chrome shows the results page
    static final String ECLIPSE_APP = "/Users/mubarakj/eclipse/java-2026-09/Eclipse.app";
    static final List<String[]> results = new ArrayList<>();

    @RegisterExtension
    static TestWatcher watcher = new TestWatcher() {
        public void testSuccessful(ExtensionContext c) {
            results.add(new String[]{c.getDisplayName(), "PASS", ""});
        }
        public void testFailed(ExtensionContext c, Throwable t) {
            results.add(new String[]{c.getDisplayName(), "FAIL", String.valueOf(t.getMessage())});
        }
    };

    @BeforeAll
    static void setup() throws Exception {
        WebApp.start();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @AfterAll
    static void teardown() throws Exception {
        int pass = 0;
        StringBuilder rows = new StringBuilder();
        int n = 1;
        for (String[] r : results) {
            boolean ok = r[1].equals("PASS");
            if (ok) pass++;
            rows.append("<tr><td>").append(n++).append("</td><td>")
                .append(r[0].replace("()", "")).append("</td><td style='font-weight:bold;color:")
                .append(ok ? "#1a8a1a" : "#d00000").append("'>").append(r[1])
                .append("</td><td>")
                .append(r[2].replace("&", "&amp;").replace("<", "&lt;"))
                .append("</td></tr>");
        }
        WebApp.resultsHtml = "<html><head><title>Test Results</title><style>"
            + "body{font-family:Arial;margin:40px}table{border-collapse:collapse;width:90%}"
            + "td,th{border:1px solid #999;padding:10px;text-align:left}th{background:#222;color:#fff}"
            + "</style></head><body><h1>Automated Test Results</h1>"
            + "<h2>" + pass + " of " + results.size() + " test cases passed</h2>"
            + "<table><tr><th>#</th><th>Test Case</th><th>Result</th><th>Details</th></tr>"
            + rows + "</table></body></html>";

        // 1. show the results page in Chrome for a few seconds
        try {
            driver.get(BASE + "/results");
            Thread.sleep(SHOW_RESULTS_SECONDS * 1000L);
        } catch (Exception ignored) {}

        // 2. close Chrome and stop the website
        try { driver.quit(); } catch (Exception ignored) {}
        WebApp.stop();

        // 3. bring Eclipse back to the front (macOS)
        try {
            int code = new ProcessBuilder("open", ECLIPSE_APP).start().waitFor();
            if (code != 0) {
                new ProcessBuilder("open", "-a", "Eclipse").start().waitFor();
            }
        } catch (Exception ignored) {}
    }

    void type(String id, String text) {
        driver.findElement(By.id(id)).sendKeys(text);
    }

    void submit() throws InterruptedException {
        driver.findElement(By.id("submitBtn")).click();
        Thread.sleep(1200);
    }

    String msg() {
        return driver.findElement(By.id("msg")).getText();
    }

    @Test @Order(1)
    void verifyWebsiteOpens() throws Exception {
        driver.get(BASE);
        Thread.sleep(1200);
        assertEquals("Feedback and Complaint System",
                driver.findElement(By.id("title")).getText());
    }

    @Test @Order(2)
    void verifyRegistration() throws Exception {
        driver.get(BASE + "/register");
        type("username", "mubarak");
        type("password", "pass123");
        submit();
        assertEquals("Registration successful", msg());
    }

    @Test @Order(3)
    void verifyLoginValid() throws Exception {
        driver.get(BASE + "/login");
        type("username", "mubarak");
        type("password", "pass123");
        submit();
        assertEquals("Login successful", msg());
    }

    @Test @Order(4)
    void verifyLoginInvalid() throws Exception {
        driver.get(BASE + "/login");
        type("username", "mubarak");
        type("password", "wrong");
        submit();
        assertEquals("Invalid credentials", msg());
    }

    @Test @Order(5)
    void verifyFeedbackSubmission() throws Exception {
        driver.get(BASE + "/feedback");
        type("name", "Mubarak");
        type("message", "Great service");
        submit();
        assertEquals("Feedback submitted", msg());
    }

    @Test @Order(6)
    void verifyComplaintSubmission() throws Exception {
        driver.get(BASE + "/complaint");
        type("title", "Slow response");
        type("details", "Support took too long");
        submit();
        assertTrue(msg().startsWith("Complaint submitted"));
    }

    @Test @Order(7)
    void verifyComplaintStatus() throws Exception {
        driver.get(BASE + "/status");
        type("id", "1");
        submit();
        assertEquals("Status: Pending", msg());
    }
}