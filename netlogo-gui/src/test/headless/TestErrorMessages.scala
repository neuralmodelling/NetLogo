// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

// Most testing of error messages can go in test/commands/*.txt.  Some tests are here because
// they go beyond the capabilities of the txt-based stuff.  (In the long run, perhaps
// that framework should be extended so these tests could be done in it.)  - ST 3/18/08, 8/21/13

import org.scalatest.{ FunSuite, BeforeAndAfterEach }
import org.nlogo.core.{ CompilerException, Model }
import org.nlogo.nvm.{ ArgumentTypeException, EngineException }

class TestErrorMessages extends FunSuite with AbstractTestLanguage with BeforeAndAfterEach {
  override def beforeEach() {
    init()
    openModel(Model("globals [glob1] breed [ frogs frog ] frogs-own [ age spots ]"))
  }
  override def afterEach() { workspace.dispose() }
  test("perspectiveChangeWithOf") {
    testCommand("create-frogs 3 [ set spots turtle ((who + 1) mod count turtles) ]")
    testCommand("ask frog 2 [ die ]")
    val ex = intercept[EngineException] {
      testCommand("ask turtle 0 [ __ignore [who] of frogs with [age = ([age] of [spots] of self)]]")
    }
    // is the error message correct?
    assertResult("That frog is dead.")(ex.getMessage)
    // is the error message attributed to the right agent? frog 2 is dead,
    // but it's frog 1 that actually encountered the error
    assertResult("frog 1")(ex.context.agent.toString)
  }
  test("argumentTypeException") {
    testCommand("set glob1 [1.4]")
    val ex = intercept[ArgumentTypeException] {
      testCommand("__ignore 0 < position 5 item 0 glob1")
    }
    assertResult("POSITION expected input to be a string or list but got the number 1.4 instead.")(ex.getMessage)
    assertResult("POSITION")(ex.instruction.token.text.toUpperCase)
  }
  test("breedOwnRedeclaration") {
    val ex = intercept[CompilerException] {
      compiler.compileProgram(
        "breed [hunters hunter] hunters-own [fear] hunters-own [loathing]",
        workspace.world.newProgram(java.util.Collections.emptyList[String]),
        workspace.getExtensionManager, workspace.getCompilationEnvironment)
    }
    assertResult("Redeclaration of HUNTERS-OWN")(ex.getMessage)
  }

}