package com.spring.servlet;

import com.alibaba.fastjson.JSON;
import com.spring.annotation.*;
import com.spring.core.MethodHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodParameterNamesScanner;

/**
 * 前端控制器DispatcherServlet
 */
public class DispatcherServlet extends HttpServlet {
    //spring配置文件
    private Properties properties = new Properties();
    //存放所有带注解的类
    private List<String> classNameList = new ArrayList<>();
    //IOC容器,通过类型注入
    private Map<String, Object> IOCByType = new HashMap<>();
    //当通过类型找不到对应实例时，通过名称注入(名称相同时会覆盖之前的值，这里就不处理了)
    private Map<String, Object> IOCByName = new HashMap<>();
    //url 到controller方法的映射
    private Map<String, MethodHandler> urlHandler = new HashMap<>();

    /**
     * 处理Get请求
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * 处理Post请求
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doHandler(request, response);
    }

    /**
     * 初始化
     *
     * @throws ServletException
     */
    @Override
    public void init() throws ServletException {
        try {
            System.out.println("------servlet开始初始化--------");
            //1、加载配置文件 spring-config.properties,获取扫描路径
            doLoadConfig();
            //2、扫描配置的路径下的带有注解的类
            doScanner(properties.getProperty("base-package"));
            //3、初始化所有的类，被放入到IOC容器中
            doPutIoc();
            //4、实现@Autowried自动注入
            doAutowried();
            //5、初始化HandlerMapping，根据url映射不同的controller方法
            doMapping();
            System.out.println("--------servlet初始化完成----------");
        } catch (Exception e) {
            System.out.println("spring框架加载失败!");
            e.printStackTrace();
        }
    }

    /**
     * 加载配置文件 spring-config.properties,获取扫描路径
     */
    private void doLoadConfig() {
        //ServletConfig:代表当前Servlet在web.xml中的配置信息
        ServletConfig config = this.getServletConfig();
        String configLocation = config.getInitParameter("contextConfigLocation");
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(configLocation);
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 2.扫描配置的路径下的带有注解的类
     *
     * @param path
     */
    private void doScanner(String path) {
        //java文件
        if (path.endsWith(".class")) {
            //获取到带有包路径的类名
            String className = path.substring(0, path.lastIndexOf(".class"));
            //扫描的类
            classNameList.add(className);
            return;
        }
        URL url = this.getClass().getClassLoader().getResource("/" + path.replaceAll("\\.", "/"));
        //是包路径，继续迭代
        File file = new File(url.getFile());
        File[] files = file.listFiles();
        for (File f : files) {
            doScanner(path + "." + f.getName());
        }
    }

    /**
     * 3.初始化所有的类，被放入到IOC容器中
     * 默认用类名首字母注入,如果自己定义了beanName，那么优先使用自己定义的beanName
     * 如果是一个接口，使用接口的类型去自动注入
     * 在Spring中同样会分别调用不同的方法 Autowried ByName Autowrited ByType
     *
     * <p>
     * IOC容器key命名规则：
     * 1.默认类名首字母小写
     * 2.使用用户自定义名, 如 @Service("userService")
     * 3.如果service实现了接口, 可以使用接口作为key
     */
    private void doPutIoc() {
        if (classNameList.isEmpty()) {
            return;
        }
        try {
            for (String className : classNameList) {
                //反射获取实例对象
                Class<?> clazz = Class.forName(className);

                //controller,service注解类
                if (clazz.isAnnotationPresent(Controller.class)) {
                    Controller controller = clazz.getAnnotation(Controller.class);
                    String beanName = controller.value().trim();
                    //如果用户没有定义名称，使用名首字母小写
                    if (StringUtils.isBlank(beanName)) {
                        beanName = lowerFirstCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    IOCByName.put(beanName, instance);
                    IOCByType.put(clazz.getName(), instance);
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    Service service = clazz.getAnnotation(Service.class);
                    String beanName = service.value().trim();
                    //如果用户没有定义名称，使用名首字母小写
                    if (StringUtils.isBlank(beanName)) {
                        beanName = lowerFirstCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    IOCByName.put(beanName, instance);
                    IOCByType.put(clazz.getName(), instance);
                    //如果service实现了接口，可以使用接口作为key
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> interf : interfaces) {
                        IOCByName.put(lowerFirstCase(interf.getSimpleName()), instance);
                        IOCByType.put(interf.getName(), instance);
                    }
                }
            }

            for (Map.Entry<String, Object> iocEntry : IOCByName.entrySet()) {
                String mapKey = iocEntry.getKey();
                Object mapValue = iocEntry.getValue();
                System.out.println("IOC类:" + mapValue.getClass().getName() + " IOC实例名:" + mapKey);
            }

            for (Map.Entry<String, Object> iocEntry : IOCByType.entrySet()) {
                String mapKey = iocEntry.getKey();
                Object mapValue = iocEntry.getValue();
                System.out.println("IOC类:" + mapValue.getClass().getName() + " IOC实例名:" + mapKey);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 4.实现@Autowried自动注入
     */
    private void doAutowried() {
        if (IOCByName.isEmpty() && IOCByType.isEmpty()) {
            return;
        }
        for (Entry<String, Object> entry : IOCByType.entrySet()) {
            //获取变量
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                //private、protected修饰的变量可访问
                field.setAccessible(true);

                if (!field.isAnnotationPresent(Autowried.class)) {
                    continue;
                }

                Object instance;
                String beanName = field.getType().getName();
                String simpleName = lowerFirstCase(field.getType().getSimpleName());
                String fieldName = field.getName();
                //首先根据属性名称注入，如果没有实例，再根据再根据Type名称以及Type全称注入，否则抛出异常
                if (IOCByName.containsKey(fieldName)) {
                    instance = IOCByName.get(fieldName);
                } else if (IOCByName.containsKey(simpleName)) {
                    instance = IOCByName.get(simpleName);
                } else if (IOCByType.containsKey(beanName)) {
                    instance = IOCByType.get(beanName);
                } else {
                    throw new RuntimeException("Not find class to autowire");
                }
                try {
                    //向obj对象的这个Field设置新值value,实现依赖注入
                    field.set(entry.getValue(), instance);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 5.初始化HandlerMapping，根据url映射不同的controller方法
     */
    private void doMapping() {
        if (IOCByType.isEmpty() && IOCByName.isEmpty()) {
            return;
        }
        for (Entry<String, Object> entry : IOCByType.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            //判断是否是controller
            if (!clazz.isAnnotationPresent(Controller.class)) {
                continue;
            }
            String startUrl = "/";
            //判断controller类上是否有RequestMapping注解，如果有则拼接url
            if (clazz.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                String value = requestMapping.value();
                if (!StringUtils.isBlank(value)) {
                    startUrl += value;
                }
            }
            //遍历controller类中RequestMapping注解修饰的方法，添加到urlHandler中,完成url到方法的映射
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(RequestMapping.class)) {
                    continue;
                }
                RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                String url = startUrl + "/" + annotation.value().trim();
                //解决多个/重叠的问题
                url = url.replaceAll("/+", "/");

                MethodHandler methodHandler = new MethodHandler();
                //放入方法
                methodHandler.setMethod(method);
                try {
                    //放入方法所在的controller
                    methodHandler.setObject(entry.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //放入方法的参数列表
                List<String> params = doParamHandler(method);

                methodHandler.setParams(params);
                urlHandler.put(url, methodHandler);
            }

            for (Map.Entry<String, MethodHandler> methodHandlerEntry : urlHandler.entrySet()) {
                String mapKey = methodHandlerEntry.getKey();
                MethodHandler mapValue = methodHandlerEntry.getValue();
                System.out.println("Controller类:" + mapValue.getObject().getClass().getName() + " 方法:" + mapValue.getMethod().getName() + " 路由映射:" + mapKey);
            }
        }
    }

    /**
     * 处理请求，执行相应的方法
     *
     * @param request
     * @param response
     * @throws IOException
     */
    private void doHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean jsonResult = false;
        String uri = request.getRequestURI();
        PrintWriter writer = response.getWriter();
        //没有映射的url，返回404
        if (!urlHandler.containsKey(uri)) {
            writer.write("404 Not Found");
            return;
        }
        //获取url对应的method包装类
        MethodHandler methodHandler = urlHandler.get(uri);
        //处理url的method
        Method method = methodHandler.getMethod();
        //method所在的controller
        Object object = methodHandler.getObject();
        //method的参数列表
        List<String> params = methodHandler.getParams();

        //如果controller或这个方法有ResponseBody修饰，返回json
        if (object.getClass().isAnnotationPresent(ResponseBody.class) || method.isAnnotationPresent(ResponseBody.class)) {
            jsonResult = true;
        }
        List<Object> args = new ArrayList<>();
        for (String param : params) {
            //从request中获取参数，然后放入参数列表
            Object parameter = request.getParameter(param);
            args.add(parameter);
        }

        try {
            //执行方法，处理，返回结果
            Object result = method.invoke(object, args.toArray());
            //返回json(使用阿里的fastJson)
            if (jsonResult) {
                writer.write(JSON.toJSONString(result));
            } else { //返回视图
                doResolveView((String) result, request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //方法执行异常，返回500
            writer.write("500 Internal Server Error");
        }
    }

    /**
     * 8、视图解析，返回视图
     *
     * @param indexView
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    private void doResolveView(String indexView, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //视图前缀
        String prefix = properties.getProperty("view.prefix");
        //视图后缀
        String suffix = properties.getProperty("view.suffix");
        String view = (prefix + indexView + suffix).trim().replaceAll("/+", "/");
        request.getRequestDispatcher(view).forward(request, response);
    }

    /**
     * 处理字符串首字母小写
     *
     * @param str
     * @return
     */
    private String lowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        //ascii码计算
        chars[0] += 32;
        return String.valueOf(chars);
    }

    /**
     * 处理method的参数
     * 在Java 8之前的版本，代码编译为class文件后，方法参数的类型是固定的，但参数名称却丢失了
     * 这和动态语言严重依赖参数名称形成了鲜明对比。
     * 现在Java 8开始在class文件中保留参数名，给反射带来了极大的便利。
     * 使用reflections包，jdk7和jdk8都可用
     **/
    private List<String> doParamHandler(Method method) {
        //使用reflections进行参数名的获取
        Reflections reflections = new Reflections(new MethodParameterNamesScanner());
        //参数名与顺序对应
        return reflections.getMethodParamNames(method);
    }
}
