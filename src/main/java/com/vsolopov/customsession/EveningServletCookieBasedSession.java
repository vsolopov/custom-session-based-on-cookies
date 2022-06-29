package com.vsolopov.customsession;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@WebServlet("/evening/cookiebased")
public class EveningServletCookieBasedSession extends HttpServlet {

    private static final String GOOD_EVENING_PARAMETRIZED_MESSAGE = "Good evening, %s";
    private static final String JSESSIONID_COOKIE = "JSESSIONID";
    private static final String SET_COOKIE_HEADER = "Set-Cookie";
    private static final String COOKIE_HEADER = "Cookie";
    private static final String NAME_PARAMETER = "name";
    private static final String DEFAULT_NAME = "Buddy";

    private final Map<String, Map<String, String>> session = new ConcurrentHashMap<>();


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        var name = request.getParameter(NAME_PARAMETER);

        Map<String, String> sessionMap = getSession(request, response);

        if (name != null) sessionMap.put(NAME_PARAMETER, name);
        if (name == null) name = sessionMap.get(NAME_PARAMETER);
        if (name == null) name = DEFAULT_NAME;

        response.setStatus(200);
        try (var writer = response.getWriter()) {
            writer.printf(GOOD_EVENING_PARAMETRIZED_MESSAGE, name);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> getSession(HttpServletRequest request, HttpServletResponse response) {
        var cookie = request.getHeader(COOKIE_HEADER);
        Map<String, String> cookieMap = new HashMap<>();

        if (cookie != null) {
            cookieMap = Arrays.stream(cookie.split(";"))
                    .collect(Collectors.toMap(value ->
                                            value.split("=")[0].strip(),
                                    value -> value.split("=")[1].strip()
                            )
                    );
        }

        Map<String, String> sessionMap = new HashMap<>();
        String sessionId;

        if (cookieMap.containsKey(JSESSIONID_COOKIE)) {
            sessionId = cookieMap.get(JSESSIONID_COOKIE);
            sessionMap = session.computeIfAbsent(sessionId, k -> new HashMap<>());

        } else {
            sessionId = UUID.randomUUID().toString();
            session.put(sessionId, sessionMap);
        }
        response.setHeader(SET_COOKIE_HEADER, JSESSIONID_COOKIE + "=" + sessionId + ";");
        return sessionMap;
    }

}
