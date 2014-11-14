package io.hawt.web;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import io.hawt.system.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns the username associated with the current session, if any. Also returns some associated details related to authentication
 */
public class UserServlet extends HttpServlet {

    private static final transient Logger LOG = LoggerFactory.getLogger(UserServlet.class);

    public static final String KEYCLOAK_ENABLED = "keycloakEnabled";

    private boolean keycloakEnabled;

    @Override
    public void init() throws ServletException {
        ConfigManager config = (ConfigManager) getServletContext().getAttribute("ConfigManager");

        String keycloakEnabled = config.get(KEYCLOAK_ENABLED, "false");
        this.keycloakEnabled = Boolean.parseBoolean(keycloakEnabled);
        LOG.info("Keycloak integration " + (this.keycloakEnabled ? "enabled" : "disabled"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        final PrintWriter out = resp.getWriter();

        Boolean authenticationEnabled = (Boolean) getServletContext().getAttribute("authenticationEnabled");
        if (authenticationEnabled == null) {
            authenticationEnabled = true;
        }

        String username = getUsername(req, authenticationEnabled);
        String jsonResponse = String.format("{ \"username\": \"%s\", \"keycloakEnabled\": %s }", username, keycloakEnabled);
        out.write(jsonResponse);
        out.flush();
        out.close();
    }

    protected String getUsername(HttpServletRequest req, boolean authenticationEnabled) {
        if (!authenticationEnabled) {
            return "user";
        }

        HttpSession session = req.getSession(false);

        if (session != null) {
            String username = (String) session.getAttribute("user");
            return username != null ? username : "";
        } else {
            return "";
        }
    }
}
