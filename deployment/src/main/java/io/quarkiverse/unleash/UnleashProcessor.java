package io.quarkiverse.unleash;

import static io.quarkus.arc.deployment.ValidationPhaseBuildItem.ValidationErrorBuildItem;
import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;

import io.getunleash.Unleash;
import io.getunleash.Variant;
import io.getunleash.event.UnleashSubscriber;
import io.quarkiverse.unleash.runtime.*;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanGizmoAdaptor;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageConfigBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ServiceProviderBuildItem;
import io.quarkus.deployment.pkg.steps.NativeBuild;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.ClassOutput;
import io.quarkus.gizmo.FieldCreator;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import io.quarkus.runtime.util.HashUtil;

public class UnleashProcessor {

    public static final String FEATURE_NAME = "unleash";

    public static final DotName DN_VARIANT = DotName.createSimple(Variant.class);
    public static final DotName DN_STRING = DotName.createSimple(String.class);

    public static final Set<DotName> IGNORE = Set.of(DN_VARIANT, DN_STRING);
    public static final DotName DN_FEATURE_VARIANT = DotName.createSimple(FeatureVariant.class);

    private static final DotName DN_FEATURE_TOGGLE = DotName.createSimple(FeatureToggle.class);
    private static final DotName DN_PRIMITIVE_BOOLEAN = DotName.createSimple(boolean.class);

    @BuildStep
    @Record(RUNTIME_INIT)
    SyntheticBeanBuildItem createUnleashSyntheticBean(UnleashRecorder unleashRecorder) {
        return SyntheticBeanBuildItem.configure(Unleash.class)
                .scope(ApplicationScoped.class)
                .supplier(unleashRecorder.getSupplier())
                .setRuntimeInit()
                .done();
    }

    @BuildStep(onlyIf = NativeBuild.class)
    void nativeImageConfiguration(BuildProducer<ReflectiveClassBuildItem> reflective,
            BuildProducer<ServiceProviderBuildItem> services) {
        reflective.produce(ReflectiveClassBuildItem.builder(
                io.getunleash.metric.ClientRegistration.class.getName(),
                io.getunleash.metric.ClientMetrics.class.getName(),
                io.getunleash.ActivationStrategy.class.getName(),
                io.getunleash.Constraint.class.getName(),
                io.getunleash.variant.VariantDefinition.class.getName(),
                io.getunleash.variant.VariantOverride.class.getName(),
                io.getunleash.Variant.class.getName(),
                io.getunleash.variant.Payload.class.getName(),
                io.getunleash.Operator.class.getName(),
                io.getunleash.FeatureToggle.class.getName(),
                io.getunleash.repository.ToggleCollection.class.getName())
                .constructors(true)
                .methods(true)
                .fields(true)
                .build());

        services.produce(new ServiceProviderBuildItem("java.net.ContentHandlerFactory",
                "sun.awt.www.content.MultimediaContentHandlers"));
    }

    @BuildStep
    void feature(BuildProducer<FeatureBuildItem> featureProducer,
            BuildProducer<ExtensionSslNativeSupportBuildItem> ssl) {
        ssl.produce(new ExtensionSslNativeSupportBuildItem(FEATURE_NAME));
        featureProducer.produce(new FeatureBuildItem(FEATURE_NAME));
    }

    @BuildStep
    NativeImageConfigBuildItem buildNativeImage() {
        NativeImageConfigBuildItem.Builder builder = NativeImageConfigBuildItem.builder();
        builder.addRuntimeInitializedClass(io.getunleash.strategy.GradualRolloutRandomStrategy.class.getName());
        builder.addRuntimeInitializedClass(io.getunleash.DefaultUnleash.class.getName());
        return builder.build();
    }

    @BuildStep
    AdditionalBeanBuildItem additionalBeans() {
        return AdditionalBeanBuildItem.builder()
                .setUnremovable()
                .addBeanClasses(UnleashLifecycleManager.class, FeatureToggle.class, FeatureToggleProducer.class,
                        UnleashResourceProducer.class, ToggleVariantProducer.class, ToggleVariantStringProducer.class,
                        UnleashSubscriber.class, QuarkusUnleashContextProvider.class, UnleashContextProducer.class)
                .build();
    }

    @BuildStep
    void validateAnnotations(CombinedIndexBuildItem combinedIndex,
            BuildProducer<ValidationErrorBuildItem> validationErrors) {
        List<Throwable> throwables = new ArrayList<>();

        for (AnnotationInstance annotation : combinedIndex.getIndex().getAnnotations(DN_FEATURE_TOGGLE)) {
            AnnotationTarget target = annotation.target();
            String name = annotation.value("name").asString();
            switch (target.kind()) {
                case FIELD -> {
                    ClassInfo declaringClass = target.asField().declaringClass();
                    if (DN_PRIMITIVE_BOOLEAN.equals(target.asField().type().name())) {
                        throwables.add(new BooleanFeatureToggleException(declaringClass));
                    }
                    if (name == null || name.isEmpty()) {
                        throwables.add(new EmptyAnnotationNameException(declaringClass));
                    }
                }
                case METHOD_PARAMETER -> {
                    ClassInfo declaringClass = target.asMethodParameter().method().declaringClass();
                    if (DN_PRIMITIVE_BOOLEAN.equals(target.asMethodParameter().type().name())) {
                        throwables.add(new BooleanFeatureToggleException(declaringClass));
                    }
                    if (name == null || name.isEmpty()) {
                        throwables.add(new EmptyAnnotationNameException(declaringClass));
                    }
                }
                default -> {
                    // No validation required for all other target kinds.
                }
            }
        }

        for (AnnotationInstance annotation : combinedIndex.getIndex().getAnnotations(DN_FEATURE_VARIANT)) {
            AnnotationTarget target = annotation.target();
            String name = annotation.value("name").asString();
            switch (target.kind()) {
                case FIELD -> {
                    if (name == null || name.isEmpty()) {
                        ClassInfo declaringClass = target.asField().declaringClass();
                        throwables.add(new EmptyAnnotationNameException(declaringClass));
                    }
                }
                case METHOD_PARAMETER -> {
                    if (name == null || name.isEmpty()) {
                        ClassInfo declaringClass = target.asMethodParameter().method().declaringClass();
                        throwables.add(new EmptyAnnotationNameException(declaringClass));
                    }
                }
                default -> {
                    // No validation required for all other target kinds.
                }
            }
        }

        validationErrors.produce(new ValidationErrorBuildItem(throwables.toArray(new Throwable[0])));
    }

    @BuildStep
    void generateProducer(CombinedIndexBuildItem combinedIndex,
            BuildProducer<GeneratedBeanBuildItem> generatedBeans) {

        IndexView index = combinedIndex.getIndex();
        Collection<AnnotationInstance> ais = index.getAnnotations(DN_FEATURE_VARIANT);

        Set<DotName> names = new HashSet<>();

        for (AnnotationInstance ano : ais) {

            Type type;
            switch (ano.target().kind()) {
                case FIELD -> {
                    FieldInfo field = ano.target().asField();
                    type = field.type();
                }
                case METHOD_PARAMETER -> {
                    MethodParameterInfo methodParameter = ano.target().asMethodParameter();
                    type = methodParameter.type();
                }
                default -> {
                    /*
                     * @FeatureVariant is allowed on methods because of the associated producer.
                     * When it is used that way, it shouldn't lead to bytecode generation in the current build step.
                     */
                    continue;
                }
            }

            ParameterizedType pt = type.asParameterizedType();
            Type vt = pt.arguments().get(0);

            if (!IGNORE.contains(vt.name())) {
                names.add(vt.name());
            }
        }

        if (!names.isEmpty()) {
            ClassOutput beansClassOutput = new GeneratedBeanGizmoAdaptor(generatedBeans);
            generateProducerClass(beansClassOutput, names);
        }

    }

    static void generateProducerClass(ClassOutput classOutput, Set<DotName> names) {
        try (ClassCreator classCreator = ClassCreator.builder().classOutput(classOutput)
                .className("io.quarkiverse.unleash.runtime.ToggleVariantObjectProducer")
                .superClass(AbstractVariantProducer.class.getName())
                .build()) {

            FieldCreator unleash = classCreator
                    .getFieldCreator("unleash", Unleash.class.getName())
                    .setModifiers(Modifier.PUBLIC); // done to prevent warning during the build
            unleash.addAnnotation(Inject.class);

            FieldCreator mapper = classCreator
                    .getFieldCreator("unleashJsonMapper", UnleashJsonMapper.class.getName())
                    .setModifiers(Modifier.PUBLIC); // done to prevent warning during the build
            mapper.addAnnotation(Inject.class);

            for (DotName name : names) {

                String hash = "_" + HashUtil.sha1(name.toString());

                try (MethodCreator methodCreator = classCreator.getMethodCreator(
                        "produce" + name.withoutPackagePrefix() + hash, name.toString(), InjectionPoint.class.getName())) {
                    methodCreator.addAnnotation(Produces.class);
                    methodCreator.addAnnotation(FeatureVariant.class).add("name", "ignored");

                    ResultHandle unleashObj = methodCreator.readInstanceField(unleash.getFieldDescriptor(),
                            methodCreator.getThis());
                    ResultHandle mapperObj = methodCreator.readInstanceField(mapper.getFieldDescriptor(),
                            methodCreator.getThis());

                    ResultHandle res = methodCreator.invokeSpecialMethod(
                            MethodDescriptor.ofMethod(AbstractVariantProducer.class, "getVariantJsonObject",
                                    Object.class,
                                    InjectionPoint.class, Class.class, Unleash.class, UnleashJsonMapper.class),
                            methodCreator.getThis(), methodCreator.getMethodParam(0),
                            methodCreator.loadClass(name.toString()), unleashObj, mapperObj);

                    methodCreator.returnValue(res);
                }
            }
        }
    }
}
