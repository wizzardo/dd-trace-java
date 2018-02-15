import net.bytebuddy.asm.Advice;

class SomeAdvice {
  @Advice.OnMethodExit
  static void method(@Advice.Return(readOnly = false) String result) {
    result = "INSTRUMENTED";
  }
}
