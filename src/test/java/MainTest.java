import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class MainTest {

    private static final String clazzName = "java.lang.Integer";

    @Test
    public void testClassExistence() {
        getClassByName(clazzName);
    }

    @Test
    public void testMethodInClassExistence() {
        Class<?> clazz = getClassByName(clazzName);
        getMethodFromClassByName(clazz, "decode", 1);
    }

    @Test
    public void testMethodReturnsType() {
        Class<?> clazz = getClassByName(clazzName);
        Method decode = getMethodFromClassByName(clazz, "decode", 1);
        if(decode.getReturnType() != Integer.class)
            throw new AssertionError("Метод должен возвращать int");
    }

    @Test
    public void testMethodArgs() {
        Class<?> clazz = getClassByName(clazzName);
        Method decode = getMethodFromClassByName(clazz, "decode", 1);
        if(decode.getParameterTypes()[0] != String.class)
            throw new AssertionError("Метод должен принимать на вход один параметр типа String");
    }

    @Test
    public void testMethodModifiers() {
        Class<?> clazz = getClassByName(clazzName);
        Method decode = getMethodFromClassByName(clazz, "decode", 1);
        int modifiers = decode.getModifiers();
        if(!Modifier.isPublic(modifiers))
            throw new AssertionError("Метод decode должен быть публичным");
        if(!Modifier.isStatic(modifiers))
            throw new AssertionError("Метод decode должен быть статическим");
    }

    @Test
    public void testEmptyStringParameter(){
        String inputParameter = "";
        Class<?> clazz = getClassByName(clazzName);
        Method decode = getMethodFromClassByName(clazz, "decode", 1);
        InvocationTargetException e = Assertions.assertThrows(InvocationTargetException.class, () -> decode.invoke(null, inputParameter));
        Assertions.assertEquals(NumberFormatException.class, e.getCause().getClass());
    }

    @Test
    public void testSpamParameterInput(){
        String inputParameter = "Somebody once told me";
        Class<?> clazz = getClassByName(clazzName);
        Method decode = getMethodFromClassByName(clazz, "decode", 1);
        InvocationTargetException e = Assertions.assertThrows(InvocationTargetException.class, () -> decode.invoke(null, inputParameter));
        Assertions.assertEquals(NumberFormatException.class, e.getCause().getClass());
    }

    @Test
    public void testSpacedParameterInput(){
        String[] inputParameters = new String[] { "     ", " ", "   5", "5    ", "    5        " };
        Class<?> clazz = getClassByName(clazzName);
        Method decode = getMethodFromClassByName(clazz, "decode", 1);
        for(String inputParameter : inputParameters) {
            InvocationTargetException e = Assertions.assertThrows(InvocationTargetException.class, () -> decode.invoke(null, inputParameter));
            Assertions.assertEquals(NumberFormatException.class, e.getCause().getClass());
        }
    }

    @Test
    public void testZeroInput() throws InvocationTargetException, IllegalAccessException {
        Class<?> clazz = getClassByName(clazzName);
        Method decode = getMethodFromClassByName(clazz, "decode", 1);
        Assertions.assertEquals(0, decode.invoke(null, "+0"));
        Assertions.assertEquals(0, decode.invoke(null, "0"));
        Assertions.assertEquals(0, decode.invoke(null, "-0"));
    }

    @Test
    public void testMaxInt() throws InvocationTargetException, IllegalAccessException {
        Class<?> clazz = getClassByName(clazzName);
        Method decode = getMethodFromClassByName(clazz, "decode", 1);
        Assertions.assertEquals(Integer.MAX_VALUE, decode.invoke(null, String.valueOf(Integer.MAX_VALUE)));
    }

    @Test
    public void testMinInt() throws InvocationTargetException, IllegalAccessException {
        Class<?> clazz = getClassByName(clazzName);
        Method decode = getMethodFromClassByName(clazz, "decode", 1);
        Assertions.assertEquals(Integer.MIN_VALUE, decode.invoke(null, String.valueOf(Integer.MIN_VALUE)));
    }

    @Test
    public void testPositiveNegativeSigns() throws InvocationTargetException, IllegalAccessException {
        Class<?> clazz = getClassByName(clazzName);
        Method decode = getMethodFromClassByName(clazz, "decode", 1);
        Assertions.assertEquals(1, decode.invoke(null, "+1"));
        Assertions.assertEquals(1, decode.invoke(null, "1"));
        Assertions.assertEquals(-1, decode.invoke(null, "-1"));
    }

    @Test
    public void testHexInput() throws InvocationTargetException, IllegalAccessException {
        Class<?> clazz = getClassByName(clazzName);
        Method decode = getMethodFromClassByName(clazz, "decode", 1);
        Assertions.assertEquals(1, decode.invoke(null, "0x1"));
        Assertions.assertEquals(16, decode.invoke(null, "0x10"));
        Assertions.assertEquals(16, decode.invoke(null, "0x010"));
        Assertions.assertEquals(16, decode.invoke(null, "0X10"));
        Assertions.assertEquals(16, decode.invoke(null, "#10"));
        Assertions.assertEquals(-16, decode.invoke(null, "-0x10"));
        Assertions.assertEquals(-16, decode.invoke(null, "-#10"));
    }

    @Test
    public void testOctInput() throws InvocationTargetException, IllegalAccessException {
        Class<?> clazz = getClassByName(clazzName);
        Method decode = getMethodFromClassByName(clazz, "decode", 1);
        Assertions.assertEquals(8, decode.invoke(null, "010"));
        Assertions.assertEquals(16, decode.invoke(null, "020"));
        Assertions.assertEquals(16, decode.invoke(null, "0020"));
        Assertions.assertEquals(-8, decode.invoke(null, "-010"));
    }

    @Test
    public void testFloatingPointNumbers(){
        String[] inputParameters = new String[] { "1.5", "1,5", "1/5" };
        Class<?> clazz = getClassByName(clazzName);
        Method decode = getMethodFromClassByName(clazz, "decode", 1);
        for(String inputParameter : inputParameters) {
            InvocationTargetException e = Assertions.assertThrows(InvocationTargetException.class, () -> decode.invoke(null, inputParameter));
            Assertions.assertEquals(NumberFormatException.class, e.getCause().getClass());
        }
    }

    private Class<?> getClassByName(String name) {
        Class<?> result;
        try {
            result = Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Не найден нужный класс для тестирования", e);
        }
        return result;
    }

    private Method getMethodFromClassByName(Class<?> testingClass, String methodName, int argsCount) {
        Method[] methods = Arrays.stream(testingClass.getDeclaredMethods())
                .filter(m -> m.getName().equals(methodName))
                .filter(m -> m.getParameterCount() == argsCount)
                .toArray(Method[]::new);
        if(methods.length != 1) {
            throw new AssertionError("Не найден нужный метод для тестирования");
        }
        return methods[0];
    }

}