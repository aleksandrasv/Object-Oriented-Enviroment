package edu.uic.cs474.a1.solution;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import edu.uic.cs474.a1.OverloadOverrideFinder;

import java.lang.reflect.Method;
import java.util.*;

public class A1Solution implements OverloadOverrideFinder {
    Object o = new Object();
    @Override
    public Map<String, Map<String, Integer>> getOverloads(Map<String, ClassOrInterfaceDeclaration> classes) {

        Map<String,Map<String,Integer>> result = new HashMap<>();
        // loop through the classes
        for(Map.Entry<String, ClassOrInterfaceDeclaration> e : classes.entrySet()){

            Map<String, Integer> map = new HashMap<>();
            result.put(e.getKey(), map);

            //checkIfOverloaded(e.getValue(), classes);
            // Loop through the methods
            for (MethodDeclaration m: e.getValue().getMethods()){
                map.put(m.getNameAsString(), map.getOrDefault(m.getNameAsString(),0) + 1);
            }

            Set<List<String>> inheritedMethods = findInheritedMethods(e.getValue(),classes);

            // add special inherited methods
            inheritedMethods.add(List.of("hashCode"));
            inheritedMethods.add(List.of("toString"));
            inheritedMethods.add(List.of("equals", "java.lang.Object"));

            for (List<String> m: inheritedMethods){
                //look at the arguments
                for (MethodDeclaration declared: e.getValue().getMethods()){
                    if (m.equals(turnMethodIntoList(declared))){
                        break;
                    }
                    if (declared.getNameAsString().equals(m.get(0))){
                        map.put(m.get(0), map.get(m.get(0)) + 1);
                        break;
                    }
                }
            }

            {
                Set<String > notOverloadedMethods = new HashSet<>();
                for (String k: map.keySet()) {
                    if (map.get(k) == 1)
                        notOverloadedMethods.add(k);
                }

                for (String k: notOverloadedMethods)
                    map.remove(k);
            }

        }

        return result;
    }

    @Override
    public Map<List<String>, Set<String>> getOverrides(Map<String, ClassOrInterfaceDeclaration> classes) {
        Map <List<String>, Set<String>> result = new HashMap<>();

        for(Map.Entry<String, ClassOrInterfaceDeclaration> e : classes.entrySet()){

            Set<List<String>> inheritedMethods = findInheritedMethods(e.getValue(), classes);
            // add special inherited methods
            inheritedMethods.add(List.of("hashCode"));
            inheritedMethods.add(List.of("toString"));
            inheritedMethods.add(List.of("equals", "java.lang.Object"));

//
            Set<List<String>> declaredMethods = new HashSet<>();

            for (MethodDeclaration declared: e.getValue().getMethods())
                declaredMethods.add(turnMethodIntoList(declared));

            for(List<String> declaration: declaredMethods){

                 if (inheritedMethods.contains(declaration)){
                     List<String> methodAsList = declaration;

                     Set<String>classesThatOverrideThisMethod = result.getOrDefault(methodAsList, new HashSet<>());

                     classesThatOverrideThisMethod.add(e.getKey());
                     result.put(methodAsList, classesThatOverrideThisMethod);
                 }
            }
         }
        return result;
    }

    private Set<List<String>> findInheritedMethods (ClassOrInterfaceDeclaration c,  Map <String, ClassOrInterfaceDeclaration> classes){
        Set<List<String>> result = new HashSet<>();
        result.addAll(findImplemented(c,classes));

        findInheritedMethodsRec(c,classes,result);

        return result;
    }


    private void findInheritedMethodsRec (ClassOrInterfaceDeclaration c,  Map <String, ClassOrInterfaceDeclaration> classes,Set<List<String>> result){
        for (ClassOrInterfaceType t: c.getExtendedTypes()){
            c = classes.get(t.getNameAsString());
            result.addAll(findImplemented(c,classes));

            for (MethodDeclaration m: c.getMethods()){
                if (m.getModifiers().contains(Modifier.privateModifier())|| m.getModifiers().contains(Modifier.staticModifier()))
                    continue;
                result.add(turnMethodIntoList(m));
            }

            findInheritedMethodsRec(c,classes,result);
        }
    }

    private Set<List<String>> findImplemented (ClassOrInterfaceDeclaration c,  Map <String, ClassOrInterfaceDeclaration> classes){
        Set<List<String>> result = new HashSet<>();
        for (ClassOrInterfaceType t: c.getImplementedTypes()){
            ClassOrInterfaceDeclaration interfaceDeclaration = classes.get(t.getNameAsString());
            for (MethodDeclaration m: interfaceDeclaration.getMethods()){
                result.add(turnMethodIntoList(m));
            }

            findInheritedMethodsRec(interfaceDeclaration,classes,result);
        }


        return result;
    }

    private List<String> turnMethodIntoList (MethodDeclaration m){
        List <String> result = new LinkedList<>();

        result.add(m.getNameAsString());

        for (Parameter p: m.getParameters())
            result.add(p.getTypeAsString());

        return result;
    }

}
