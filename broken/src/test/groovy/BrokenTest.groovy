import net.bytebuddy.agent.ByteBuddyAgent
import net.bytebuddy.agent.builder.AgentBuilder
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.utility.JavaModule
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicInteger

import static net.bytebuddy.matcher.ElementMatchers.*

class BrokenTest extends Specification {

  AtomicInteger instErrorCount = new AtomicInteger();

  def "test resource"() {
    setup:
    def instrumentation = ByteBuddyAgent.install()
    AgentBuilder agentBuilder = new AgentBuilder.Default()
      .disableClassFormatChanges()
      .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
      .with(new AgentBuilder.Listener() {
      @Override
      void onError(
        final String typeName,
        final ClassLoader classLoader, final JavaModule module, final boolean loaded, final Throwable throwable) {
        instErrorCount.incrementAndGet()
        System.out.println("Failed to handle " + typeName + " for transformation: " + throwable.getMessage())
        throwable.printStackTrace()
      }

      @Override
      void onTransformation(
        final TypeDescription typeDescription,
        final ClassLoader classLoader, final JavaModule module, final boolean loaded, final DynamicType dynamicType) {
        System.out.println("Transformed " + typeDescription + " -- " + classLoader)
      }

      @Override
      void onIgnored(
        final TypeDescription typeDescription,
        final ClassLoader classLoader, final JavaModule module, final boolean loaded) {}

      @Override
      void onComplete(
        final String typeName, final ClassLoader classLoader, final JavaModule module, final boolean loaded) {}

      @Override
      void onDiscovery(
        final String typeName, final ClassLoader classLoader, final JavaModule module, final boolean loaded) {}
    })

      .ignore(any(), not(isSystemClassLoader()))

    agentBuilder = agentBuilder.type(named("BrokenClass"))
      .transform(
      new AgentBuilder.Transformer.ForAdvice().advice(
        named("someMethod"), SomeAdvice.class.getName())
    )

    def activeTransformer = agentBuilder.installOn(instrumentation)

    def test = new BrokenClass()

    expect:
    test.someMethod("test") == "INSTRUMENTED"
    instErrorCount.get() == 0

    cleanup:
    instrumentation.removeTransformer(activeTransformer)
  }
}
