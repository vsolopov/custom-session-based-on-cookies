package com.vsolopov.customsession;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/evening")
public class EveningServlet extends HttpServlet {
    private static final String GOOD_EVENING_PARAMETRIZED_MESSAGE = "Good evening, %s";
    private static final String NAME_PARAMETER = "name";
    private static final String DEFAULT_NAME = "Buddy";


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var name = req.getParameter(NAME_PARAMETER);

        if (name != null) {
            req.getSession().setAttribute(NAME_PARAMETER, name);

        } else {
            var result = (String) req.getSession().getAttribute(NAME_PARAMETER);
            name = result != null ? result : DEFAULT_NAME;
        }

        resp.setStatus(200);

        try (var writer = resp.getWriter()) {
            writer.printf(GOOD_EVENING_PARAMETRIZED_MESSAGE, name);
            writer.flush();

        } catch (IOException e) {
            resp.sendError(500, e.getMessage());
        }
    }

}
