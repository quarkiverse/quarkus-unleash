package io.quarkiverse.unleash;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;
import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

import io.quarkiverse.unleash.runtime.UnleashProducer;
import io.quarkiverse.unleash.runtime.UnleashRecorder;
import io.quarkiverse.unleash.runtime.UnleashRuntimeTimeConfig;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.pkg.steps.NativeBuild;

public class UnleashProcessor {

    private static final String UNLEASH = "unleash";

    @BuildStep
    @Record(RUNTIME_INIT)
    void configureRuntimeProperties(UnleashRecorder recorder, UnleashRuntimeTimeConfig runtimeConfig,
            BeanContainerBuildItem beanContainer) {
        recorder.initializeProducers(beanContainer.getValue(), runtimeConfig);
    }

    @Record(STATIC_INIT)
    @BuildStep
    void build(UnleashRecorder recorder,
            BuildProducer<AdditionalBeanBuildItem> additionalBeanBuildItemBuildItem,
            BuildProducer<FeatureBuildItem> featureProducer) {
        featureProducer.produce(new FeatureBuildItem(UNLEASH));

        additionalBeanBuildItemBuildItem.produce(AdditionalBeanBuildItem.unremovableOf(UnleashProducer.class));
    }

    @BuildStep(onlyIf = NativeBuild.class)
    @Record(STATIC_INIT)
    void nativeImageConfiguration(UnleashRecorder recorder,
            BuildProducer<ReflectiveClassBuildItem> reflective) {
        reflective.produce(new ReflectiveClassBuildItem(true, true, true,
                no.finn.unleash.metric.ClientRegistration.class.getName(),
                no.finn.unleash.metric.ClientMetrics.class.getName(),
                no.finn.unleash.repository.ToggleCollection.class.getName()));
    }

}
