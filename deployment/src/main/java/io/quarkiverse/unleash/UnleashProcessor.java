package io.quarkiverse.unleash;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

import io.quarkiverse.unleash.runtime.FeatureToggleProducer;
import io.quarkiverse.unleash.runtime.UnleashRecorder;
import io.quarkiverse.unleash.runtime.UnleashRuntimeTimeConfig;
import io.quarkiverse.unleash.runtime.UnleashService;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageConfigBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.pkg.steps.NativeBuild;
import io.quarkus.runtime.ApplicationConfig;

public class UnleashProcessor {

    public static final String FEATURE_NAME = "unleash";

    @BuildStep
    @Record(RUNTIME_INIT)
    void configureRuntimeProperties(UnleashRecorder recorder, UnleashRuntimeTimeConfig runtimeConfig,
            ApplicationConfig appConfig) {
        recorder.initializeProducers(runtimeConfig, appConfig);
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
                .addBeanClasses(UnleashService.class, FeatureToggle.class, FeatureToggleProducer.class)
                .build();
    }
}
