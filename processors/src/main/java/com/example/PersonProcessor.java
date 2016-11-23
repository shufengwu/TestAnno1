package com.example;

import com.google.auto.service.AutoService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;

@AutoService(Processor.class)
/**
 * 每一个注解处理器类都必须有一个空的构造函数，默认不写就行;
 */
public class PersonProcessor extends AbstractProcessor {

    // 元素操作的辅助类
    Elements elementUtils;

    /*
    *init()方法会被注解处理工具调用，并输入ProcessingEnviroment参数。
    *ProcessingEnviroment提供很多有用的工具类Elements, Types 和 Filer
    * @param processingEnvironment 提供给 processor 用来访问工具框架的环境
    */

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        // 元素操作的辅助类
        elementUtils = processingEnv.getElementUtils();
    }

    /**
     * 这相当于每个处理器的主函数main()，你在这里写你的扫描、评估和处理注解的代码，以及生成Java文件。
     * 输入参数RoundEnviroment，可以让你查询出包含特定注解的被注解元素
     * @param set   请求处理的注解类型
     * @param roundEnvironment  有关当前和以前的信息环境
     * @return  如果返回 true，则这些注解已声明并且不要求后续 Processor 处理它们；
     *           如果返回 false，则这些注解未声明并且可能要求后续 Processor 处理它们
     */

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        // 获得被该注解声明的元素
        Set<? extends Element> elememts = roundEnvironment
                .getElementsAnnotatedWith(Person.class);
        TypeElement classElement = null;// 声明类元素
        List<VariableElement> fields = null;// 声明一个存放成员变量的列表
        // 存放二者
        Map<String, List<VariableElement>> maps = new HashMap<String, List<VariableElement>>();
        // 遍历
        for (Element ele : elememts) {
            // 判断该元素是否为类
            if (ele.getKind() == ElementKind.CLASS) {
                classElement = (TypeElement) ele;
                maps.put(classElement.getQualifiedName().toString(),
                        fields = new ArrayList<VariableElement>());

            } else if (ele.getKind() == ElementKind.FIELD) // 判断该元素是否为成员变量
            {
                VariableElement varELe = (VariableElement) ele;
                // 获取该元素封装类型
                TypeElement enclosingElement = (TypeElement) varELe
                        .getEnclosingElement();
                // 拿到key
                String key = enclosingElement.getQualifiedName().toString();
                fields = maps.get(key);
                if (fields == null) {
                    maps.put(key, fields = new ArrayList<VariableElement>());
                }
                fields.add(varELe);
            }
        }

        for (String key : maps.keySet()) {
            if (maps.get(key).size() == 0) {
                TypeElement typeElement = elementUtils.getTypeElement(key);
                List<? extends Element> allMembers = elementUtils
                        .getAllMembers(typeElement);
                if (allMembers.size() > 0) {
                    maps.get(key).addAll(ElementFilter.fieldsIn(allMembers));
                }
            }
        }
        generateCodes(maps);
        return true;
    }

    private void generateCodes(Map<String, List<VariableElement>> maps) {
        File dir = new File("d://apt_test");
        if (!dir.exists())
            dir.mkdirs();
        // 遍历map
        for (String key : maps.keySet()) {

            // 创建文件
            File file = new File(dir, key.replaceAll("\\.", "_") + ".json");
            try {
                /**
                 * 编写json文件内容
                 */
                FileWriter fw = new FileWriter(file);
                fw.append("{").append("class:").append("\"" + key + "\"")
                        .append(",\n ");
                fw.append("fields:\n {\n");
                List<VariableElement> fields = maps.get(key);
                for (int i = 0; i < fields.size(); i++) {
                    VariableElement field = fields.get(i);
                    fw.append("  ").append(field.getSimpleName()).append(":")
                            .append("\"" + field.asType().toString() + "\"");
                    if (i < fields.size() - 1) {
                        fw.append(",");
                        fw.append("\n");
                    }
                }
                fw.append("\n }\n");
                fw.append("}");
                fw.flush();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 这里必须指定，这个注解处理器是注册给哪个注解的。注意，它的返回值是一个字符串的集合，包含本处理器想要处理的注解类型的合法全称
     * @return  注解器所支持的注解类型集合，如果没有这样的类型，则返回一个空集合
     */

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<String>();
        annotataions.add(Person.class.getCanonicalName());
        return annotataions;
    }

    /**
     * 指定使用的Java版本，通常这里返回SourceVersion.latestSupported()，默认返回SourceVersion.RELEASE_6
     * @return  使用的Java版本
     */

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}


