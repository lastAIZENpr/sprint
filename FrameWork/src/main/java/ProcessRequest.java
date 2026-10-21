import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.servlet.annotation.MultipartConfig;

import util.Bit;
import util.Session;
import util.SessionManager;
import util.MethodExecutor;
import util.ModelAndView;
import util.JsonConverter;
import util.UrlMethod;
import annotation.WebApi;

@MultipartConfig
public class ProcessRequest extends HttpServlet {

    private HashMap<UrlMethod, Method> urlMap;
    private String prefix;
    private String surfix;

    @SuppressWarnings("unchecked")
    @Override
    public void init() throws ServletException {
        ServletContext context = getServletContext();
        this.urlMap = (HashMap<UrlMethod, Method>) context.getAttribute("urlMap");
        this.prefix = context.getInitParameter("view-prefix");
        this.surfix = context.getInitParameter("view-suffix");
    }

    private Bit extractBit(String paramName, HttpServletRequest req) throws ServletException, IOException {
        Part part = req.getPart(paramName);
        if (part == null || part.getSize() == 0) {
            return null;
        }
        
        Bit bit = new Bit();
        bit.setData(part.getInputStream().readAllBytes());
        bit.setFilename(part.getSubmittedFileName());
        bit.setContentType(part.getContentType());
        
        return bit;
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // Gestion de session
        String sessionId = null;
        javax.servlet.http.Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (javax.servlet.http.Cookie cookie : cookies) {
                if ("SESSION_ID".equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                    break;
                }
            }
        }
        
        SessionManager sessionManager = SessionManager.getInstance();
        Session session = sessionManager.getSession(sessionId);
        
        // Créer ou mettre à jour le cookie de session
        javax.servlet.http.Cookie sessionCookie = new javax.servlet.http.Cookie("SESSION_ID", session.getSessionId());
        sessionCookie.setHttpOnly(true);
        sessionCookie.setPath("/");
        res.addCookie(sessionCookie);

        String url = req.getRequestURI();
        String route = url.substring(req.getContextPath().length());
        if (route.isEmpty()) {
            route = "/";
        }

        if (route.endsWith(".html")) {
            String cheminPhysique = getServletContext().getRealPath(route);
            File fichier = new File(cheminPhysique);

            if (fichier.exists()) {
                res.setContentType("text/html;charset=UTF-8");
                Files.copy(fichier.toPath(), res.getOutputStream());
                return;
            } else {
                res.sendError(404, "Fichier introuvable");
                return;
            }
        }

        String httpMethod = req.getMethod();
        UrlMethod cle = new UrlMethod(route, httpMethod);

        Method method = (urlMap != null) ? urlMap.get(cle) : null;

        if (method == null) {
            res.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = res.getWriter();
            out.println("Aucune methode trouvee pour l'URL : " + url + " et la Methode : " + httpMethod);
            out.println("");
            out.println("URLs disponibles :");

            if (urlMap != null) {
                for (UrlMethod u : urlMap.keySet()) {
                    Method m = urlMap.get(u);
                    out.println(u.getMethod() + " " + u.getUrl() + "    " + m.getDeclaringClass().getName() + "."
                            + m.getName() + "()");
                }
            }
            return;
        }

        try {
            Object obj = MethodExecutor.execute(method, session);

            if (obj instanceof ModelAndView) {
                ModelAndView mv = (ModelAndView) obj;
                Map<String, Object> map = mv.getModel();

                for (Map.Entry<String, Object> mm : map.entrySet()) {
                    req.setAttribute(mm.getKey(), mm.getValue());
                }
                String path = this.prefix + mv.getView() + this.surfix;
                RequestDispatcher requestDispatcher = req.getRequestDispatcher(path);
                requestDispatcher.forward(req, res);
                return;
            }

            boolean isWebApi = method.getDeclaringClass().isAnnotationPresent(WebApi.class);

            if (isWebApi) {
                res.setContentType("application/json;charset=UTF-8");
                PrintWriter out = res.getWriter();
                out.println(JsonConverter.toJson(obj));
            } else {
                res.setContentType("text/plain;charset=UTF-8");
                PrintWriter out = res.getWriter();
                out.println("URL     : " + url);
                out.println("Methode : " + method.getName() + "()");
                out.println("Execution de :" + method);
                out.println("Resultat : " + obj);
            }

        } catch (Exception e) {
            res.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = res.getWriter();
            out.println("Erreur lors de l'execution du methode :" + e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        processRequest(req, res);
    }
}
