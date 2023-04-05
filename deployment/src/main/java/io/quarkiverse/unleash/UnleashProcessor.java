package io.quarkiverse.unleash;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.jandex.*;
import org.jboss.jandex.Type;

import io.getunleash.Unleash;
import io.getunleash.Variant;
import io.quarkiverse.unleash.runtime.*;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanGizmoAdaptor;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageConfigBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.pkg.steps.NativeBuild;
import io.quarkus.gizmo.*;
import io.quarkus.runtime.ApplicationConfig;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.util.HashUtil;

public class UnleashProcessor {

    public static final String FEATURE_NAME = "unleash";

    public static final DotName DN_VARIANT = DotName.createSimple(Variant.class);
    public static final DotName DN_STRING = DotName.createSimple(String.class);

    public static final Set<DotName> IGNORE = Set.of(DN_VARIANT, DN_STRING);
    public static final DotName DN_FEATURE_VARIANT = DotName.createSimple(FeatureVariant.class);

    @BuildStep
    @Record(RUNTIME_INIT)
    void configureRuntimeProperties(UnleashRecorder recorder, UnleashRuntimeTimeConfig runtimeConfig,
            ApplicationConfig appConfig, LaunchModeBuildItem launchMode) {
        recorder.initializeProducers(runtimeConfig, appConfig, launchMode.getLaunchMode() == LaunchMode.DEVELOPMENT);
    }

    @BuildStep(onlyIf = NativeBuild.class)
    void nativeImageConfiguration(BuildProducer<ReflectiveClassBuildItem> reflective) {
        reflective.produce(new ReflectiveClassBuildItem(true, true, true,
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
                io.getunleash.repository.ToggleCollection.class.getName()));
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
                .addBeanClasses(UnleashService.class, FeatureToggle.class, FeatureToggleProducer.class,
                        UnleashResourceProducer.class, ToggleVariantProducer.class, ToggleVariantStringProducer.class)
                .build();
    }

    @BuildStep
    void generateProducer(CombinedIndexBuildItem combinedIndex,
            BuildProducer<GeneratedBeanBuildItem> generatedBeans) {

        IndexView index = combinedIndex.getIndex();
        Collection<AnnotationInstance> ais = index.getAnnotations(DN_FEATURE_VARIANT);

        Set<DotName> names = new HashSet<>();

        for (AnnotationInstance ano : ais) {
            FieldInfo field = ano.target().asField();

            Type type = field.type();
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

            classCreator.addAnnotation(Singleton.class);

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
