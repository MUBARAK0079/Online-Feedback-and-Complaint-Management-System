package app;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

public class WebApp {
    static Map<String, String> users = new HashMap<>();
    static List<String> complaints = new ArrayList<>();
    static HttpServer server;
    public static String resultsHtml = "<h2>No results yet</h2>";

    public static void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", e -> send(e, page(
            "<h1 id='title'>Feedback and Complaint System</h1>"
            + "<a href='/register'>Register</a> | <a href='/login'>Login</a> | "
            + "<a href='/feedback'>Feedback</a> | <a href='/complaint'>Complaint</a> | "
            + "<a href='/status'>Status</a>")));

        server.createContext("/register", e -> handle(e, "Register", "username,password", p -> {
            users.put(p.get("username"), p.get("password"));
            return "Registration successful";
        }));
        server.createContext("/login", e -> handle(e, "Login", "username,password", p ->
            p.get("password") != null && p.get("password").equals(users.get(p.get("username")))
                ? "Login successful" : "Invalid credentials"));
        server.createContext("/feedback", e -> handle(e, "Feedback", "name,message", p ->
            "Feedback submitted"));
        server.createContext("/complaint", e -> handle(e, "Complaint", "title,details", p -> {
            complaints.add(p.get("title"));
            return "Complaint submitted. ID: " + complaints.size();
        }));
        server.createContext("/status", e -> handle(e, "Status", "id", p -> {
            try {
                int id = Integer.parseInt(p.get("id"));
                return (id >= 1 && id <= complaints.size()) ? "Status: Pending" : "Complaint not found";
            } catch (Exception ex) { return "Complaint not found"; }
        }));
        server.createContext("/results", e -> send(e, resultsHtml));
        server.start();
    }

    public static void stop() { if (server != null) server.stop(0); }

    static void handle(HttpExchange e, String title, String fields,
                       Function<Map<String, String>, String> action) throws IOException {
        if (e.getRequestMethod().equals("POST")) {
            String body = new String(e.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> p = new HashMap<>();
            for (String kv : body.split("&")) {
                String[] a = kv.split("=", 2);
                if (a.length == 2) p.put(URLDecoder.decode(a[0], StandardCharsets.UTF_8),
                                         URLDecoder.decode(a[1], StandardCharsets.UTF_8));
            }
            send(e, page("<h2>" + title + "</h2><p id='msg'>" + action.apply(p) + "</p><a href='/'>Home</a>"));
        } else {
            StringBuilder f = new StringBuilder("<h2>" + title + "</h2><form method='post'>");
            for (String n : fields.split(",")) {
                String type = n.equals("password") ? "password" : "text";
                f.append(n).append(": <input id='").append(n).append("' name='").append(n)
                 .append("' type='").append(type).append("'><br><br>");
            }
            f.append("<button id='submitBtn' type='submit'>Submit</button></form>");
            send(e, page(f.toString()));
        }
    }

    static String page(String body) {
        return "<html><head><title>Feedback System</title></head>"
             + "<body style='font-family:Arial;margin:40px'>" + body + "</body></html>";
    }

    static void send(HttpExchange e, String html) throws IOException {
        byte[] b = html.getBytes(StandardCharsets.UTF_8);
        e.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        e.sendResponseHeaders(200, b.length);
        try (OutputStream o = e.getResponseBody()) { o.write(b); }
    }
}