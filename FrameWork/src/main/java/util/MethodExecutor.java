package util;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class MethodExecutor {
    public static Object execute(Method method) throws Exception {
        return execute(method, null);
    }
    
    public static Object execute(Method method, Session session) throws Exception {
        if (method == null) {
            System.err.println("Methode ne peut pas etre null");
            return null;
        }
        
        Class<?> classController = method.getDeclaringClass();
        Object constructor = classController.getDeclaredConstructor().newInstance();
        
        // Vérifier si la méthode a des paramètres
        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) {
            return method.invoke(constructor);
        }
        
        // Préparer les arguments
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Class<?> paramType = parameters[i].getType();
            if (paramType.equals(Session.class)) {
                args[i] = session;
            }
            // On peut ajouter d'autres types ici (HttpServletRequest, HttpServletResponse, etc.)
        }
        
        return method.invoke(constructor, args);
    }
}