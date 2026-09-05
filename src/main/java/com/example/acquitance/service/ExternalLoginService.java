package com.example.acquitance.service;

import com.example.acquitance.dto.AuthResultDto;
import com.example.acquitance.dto.EnrollmentDto;
import com.example.acquitance.dto.UserProfileDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExternalLoginService {

    private static final String BASE_URL = "https://siocon.jrmsu-arms.online/student";
    private static final String LOGIN_URL = BASE_URL + "/login.php";
    private static final String ENROLLMENT_URL = BASE_URL + "/myEnrollment.php";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36";

    /**
     * Authenticates with ARMS using a temporary local CookieManager to ensure thread safety.
     */
    public AuthResultDto authenticate(String username, String password, String campus, String type) {
        CookieManager localCookieManager = new CookieManager();
        HttpClient client = HttpClient.newBuilder()
                .cookieHandler(localCookieManager)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        try {
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(URI.create(LOGIN_URL))
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();

            HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
            String token = extractToken(getResponse.body());
            
            if (token.isEmpty()) return null;

            String form = String.format(
                "campus=%s&type=%s&username=%s&password=%s&action=login&token=%s&remember=off",
                campus != null ? campus : "siocon",
                type != null ? type : "1",
                username, password, token
            );

            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(URI.create(LOGIN_URL))
                    .header("User-Agent", USER_AGENT)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(BodyPublishers.ofString(form))
                    .build();

            HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
            String body = postResponse.body();

            if (body.contains("Welcome")) {
                String fullName = extractFullName(body);
                String cookies = localCookieManager.getCookieStore().getCookies().stream()
                        .map(c -> c.getName() + "=" + c.getValue())
                        .collect(Collectors.joining("; "));
                
                return new AuthResultDto(cookies, fullName);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public UserProfileDto fetchUserProfile(String externalCookies, String name, String studentId) {
        try {
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENROLLMENT_URL))
                    .header("User-Agent", USER_AGENT)
                    .header("Cookie", externalCookies)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Document doc = Jsoup.parse(response.body());
            Element firstRow = doc.selectFirst("#tableMyEnrollment tr");
            
            String program = "N/A";
            String year = "N/A";

            if (firstRow != null) {
                Elements cells = firstRow.select("td");
                if (cells.size() >= 3) {
                    String semSy = cells.get(1).text();
                    program = cells.get(2).text();
                    if (semSy.contains(" - SY ")) {
                        year = semSy.split(" - SY ")[1];
                    }
                }
            }
            return new UserProfileDto(name, studentId, program, year, false);
        } catch (Exception e) {
            return new UserProfileDto(name, studentId, "Error", "Error", false);
        }
    }

    public List<EnrollmentDto> fetchEnrollments(String externalCookies) {
        try {
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENROLLMENT_URL))
                    .header("User-Agent", USER_AGENT)
                    .header("Cookie", externalCookies)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return parseEnrollments(response.body());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<EnrollmentDto> parseEnrollments(String html) {
        List<EnrollmentDto> enrollments = new ArrayList<>();
        if (html == null) return enrollments;
        Document doc = Jsoup.parse(html);
        Elements rows = doc.select("#tableMyEnrollment tr");
        for (Element row : rows) {
            Elements cells = row.select("td");
            if (cells.size() < 4) continue;
            String semSy = cells.get(1).text();
            String program = cells.get(2).text();
            Element actionLink = cells.get(3).selectFirst("a");
            String enrollmentId = "";
            if (actionLink != null) {
                String href = actionLink.attr("href");
                if (href.contains("enrollmentId=")) enrollmentId = href.split("enrollmentId=")[1];
            }
            String semester = semSy;
            String schoolYear = "";
            if (semSy.contains(" - SY ")) {
                String[] parts = semSy.split(" - SY ");
                semester = parts[0];
                schoolYear = parts[1];
            }
            enrollments.add(new EnrollmentDto(semester, schoolYear, program, enrollmentId));
        }
        return enrollments;
    }

    private String extractFullName(String html) {
        Document doc = Jsoup.parse(html);
        Element h3 = doc.selectFirst("h3");
        if (h3 != null && h3.text().contains("Welcome")) {
            return h3.text().replace("Welcome", "").trim();
        }
        return null;
    }

    private String extractToken(String html) {
        if (html == null) return "";
        Document doc = Jsoup.parse(html);
        Element tokenElement = doc.selectFirst("input[name=token]");
        return tokenElement != null ? tokenElement.val() : "";
    }
}
