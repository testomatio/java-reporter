package io.testomat.methodloader.junit;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class MethodBodyParser {

    public List<MethodDeclaration> getMethodDeclarations(final String filepath) {
        CompilationUnit cu;
        try {
            cu = StaticJavaParser.parse(Paths.get(filepath));
        } catch (IOException e) {
            throw new ParsingException("Could not parse file " + filepath, e);
        }
        return cu.getChildNodes().stream()
                .filter(MethodDeclaration.class::isInstance)
                .map(node -> (MethodDeclaration) node)
                .collect(Collectors.toList());
    }

    //    public String getParticularMethodBody(List<MethodDeclaration> methods,
    //    String methodName) {
    //        return methods.stream()
    //                .filter(methodDeclaration
    //                        -> methodDeclaration.getNameAsString().equalsIgnoreCase(methodName))
    //                .map(Node::removeComment)
    //                .map(Node::toString)
    //                .findFirst()
    //                .orElseThrow(() -> new ParsingException(
    //                        "Failed to get method body for method name: " + methodName));
    //    }
}
