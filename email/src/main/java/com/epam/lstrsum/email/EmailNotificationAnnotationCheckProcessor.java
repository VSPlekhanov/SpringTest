package com.epam.lstrsum.email;

import com.epam.lstrsum.email.template.MailTemplate;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("com.epam.lstrsum.email.EmailNotification")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class EmailNotificationAnnotationCheckProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element element : annotatedElements) {
                ExecutableElement method = (ExecutableElement) element;

                TypeMirror returnType = method.getReturnType();
                TypeMirror annotationParameterMailTemplateGeneric = getAnnotationParameterMailTemplateGeneric(method);

                if (returnType instanceof NoType) {
                    printError("EmailNotification annotation should not annotate method with void return type.", method);
                } else if (annotationParameterMailTemplateGeneric == null) {
                    printError("Wrong EmailNotification parameter. Should implement MailTemplate.", method);
                } else if (!isSameType(returnType, annotationParameterMailTemplateGeneric) &&
                        !isSubtype(returnType, annotationParameterMailTemplateGeneric)) {
                    printError("EmailNotification parameter should implement MailTemplate with same type as method returns.", method);
                }
            }
        }
        return true;
    }

    private boolean isSubtype(TypeMirror returnType, TypeMirror annotationParameterMailTemplateGeneric) {
        return processingEnv.getTypeUtils().isSubtype(returnType, annotationParameterMailTemplateGeneric);
    }

    private boolean isSameType(TypeMirror returnType, TypeMirror annotationParameterMailTemplateGeneric) {
        return processingEnv.getTypeUtils().isSameType(returnType, annotationParameterMailTemplateGeneric);
    }

    private TypeMirror getAnnotationParameterMailTemplateGeneric(ExecutableElement method) {
        EmailNotification currentAnnotation = method.getAnnotation(EmailNotification.class);
        TypeMirror annotationParameterTypeMirror = null;

        try {
            currentAnnotation.template();
            printError("Unexpected condition: method.getAnnotation(EmailNotification.class).template() does not throws exception.", method);
        } catch (MirroredTypeException e) {
            annotationParameterTypeMirror = e.getTypeMirror();
        }

        TypeElement annotationParameterTypeElement = (TypeElement) processingEnv.getTypeUtils().asElement(annotationParameterTypeMirror);

        List<? extends TypeMirror> annotationParameterInterfaces = annotationParameterTypeElement.getInterfaces();

        TypeMirror annotationParameterMailTemplateGenericType = null;

        for (TypeMirror interfase : annotationParameterInterfaces) {
            TypeElement mailTemplateElement = processingEnv.getElementUtils().getTypeElement(MailTemplate.class.getName());
            TypeElement interfaceElement = (TypeElement) processingEnv.getTypeUtils().asElement(interfase);

            if (mailTemplateElement.equals(interfaceElement) && interfase instanceof DeclaredType) {
                DeclaredType declaredInt = (DeclaredType) interfase;
                List<? extends TypeMirror> typeArguments = declaredInt.getTypeArguments();
                if (!typeArguments.isEmpty()) {
                    annotationParameterMailTemplateGenericType = typeArguments.get(0);
                }
            }
        }

        return annotationParameterMailTemplateGenericType;
    }

    private void printError(String msg, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, element);
    }
}

