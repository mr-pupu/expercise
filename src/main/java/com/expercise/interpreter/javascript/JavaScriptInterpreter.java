package com.expercise.interpreter.javascript;

import com.expercise.domain.challenge.Challenge;
import com.expercise.domain.challenge.TestCase;
import com.expercise.enums.DataType;
import com.expercise.interpreter.*;
import com.expercise.interpreter.typechecker.javascript.JavaScriptTypeChecker;
import com.expercise.service.challenge.model.ChallengeEvaluationContext;
import com.expercise.utils.JsonUtils;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class JavaScriptInterpreter extends Interpreter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaScriptInterpreter.class);

    @Autowired
    private JavaScriptTypeChecker javaScriptTypeChecker;

    @Autowired
    private JavaScriptOutputWriter javaScriptOutputWriter;

    @Override
    protected void interpretInternal(ChallengeEvaluationContext context) throws InterpreterException {
        ScriptEngine javaScriptEngine = getScriptEngine();

        evaluateSourceCode(context.getSource(), javaScriptEngine);

        Challenge challenge = context.getChallenge();

        for (TestCaseWithResult eachTestCaseWithResult : context.getTestCaseWithResults()) {
            TestCase eachTestCase = eachTestCaseWithResult.getTestCaseUnderTest();
            Object resultValue = makeFunctionCallAndGetResultValue(javaScriptEngine, challenge, eachTestCase);
            typeCheck(resultValue, eachTestCase.getOutputType());
            processTestCase(eachTestCaseWithResult, resultValue);
        }
        context.decideInterpreterResult();
    }

    private void typeCheck(Object resultValue, DataType outputType) throws InterpreterException {
        if (!javaScriptTypeChecker.typeCheck(resultValue, outputType)) {
            throw new InterpreterException(InterpreterResult.typeErrorFailedResult());
        }
    }

    private ScriptEngine getScriptEngine() {
        NashornScriptEngine javaScriptEngine = (NashornScriptEngine) new NashornScriptEngineFactory().getScriptEngine(new String[]{"-strict", "--no-java", "--no-syntax-extensions"});
        javaScriptEngine.getContext().setWriter(javaScriptOutputWriter);
        javaScriptEngine.getContext().setErrorWriter(javaScriptOutputWriter);
        javaScriptEngine.put(ScriptEngine.FILENAME, "solution.js");
        return javaScriptEngine;
    }

    private void evaluateSourceCode(String sourceCode, ScriptEngine javaScriptEngine) throws InterpreterException {
        try {
            javaScriptEngine.eval(sourceCode);
        } catch (ScriptException e) {
            throw new InterpreterException(InterpreterResult.syntaxErrorFailedResult(e.getMessage()));
        }
    }

    private Object makeFunctionCallAndGetResultValue(ScriptEngine javaScriptEngine, Challenge challenge, TestCase testCase) throws InterpreterException {
        Object evaluationResult;

        try {
            Object[] convertedInputValues = challenge.getConvertedInputValues(testCase.getInputs()).toArray();
            boolean hasSolutionTopLevelFunction = javaScriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).containsKey("solution");
            if (hasSolutionTopLevelFunction) {
                evaluationResult = ((Invocable) javaScriptEngine).invokeFunction("solution", convertedInputValues);
            } else {
                evaluationResult = OutputMessageAggregator.getOutputMessage();
            }
        } catch (ScriptException e) {
            LOGGER.debug("ScriptException while interpreting", e);
            throw new InterpreterException(InterpreterResult.createFailedResult(e.getMessage()));
        } catch (Exception e) {
            LOGGER.debug("Exception while interpreting", e);
            throw new InterpreterException(InterpreterResult.createFailedResult());
        }

        if (evaluationResult == null) {
            throw new InterpreterException(InterpreterResult.noResultFailedResult());
        }

        return evaluationResult;
    }

    private void processTestCase(TestCaseWithResult testCaseWithResult, Object resultValue) {
        TestCaseResult testCaseResult = TestCaseResult.FAILED;

        DataType outputType = testCaseWithResult.getTestCaseUnderTest().getOutputType();
        Object expectedJavaObject = outputType.toJavaObject(testCaseWithResult.getTestCaseUnderTest().getOutput());
        if (outputType == DataType.Integer) {
            int evaluationResultAsInteger = ((Number) resultValue).intValue();
            int expectedValue = (int) Double.parseDouble(expectedJavaObject.toString());
            testCaseResult = evaluationResultAsInteger == expectedValue
                    ? TestCaseResult.PASSED
                    : TestCaseResult.FAILED;
            testCaseWithResult.setActualValue(String.valueOf(evaluationResultAsInteger));
        } else if (outputType == DataType.Text) {
            String evaluationResultAsString = resultValue.toString().trim();
            testCaseResult = evaluationResultAsString.equals(expectedJavaObject)
                    ? TestCaseResult.PASSED
                    : TestCaseResult.FAILED;
            testCaseWithResult.setActualValue(DataType.toLiteral(evaluationResultAsString));
        } else if (outputType == DataType.Array) {
            Collection<Object> resultCollection = unwrapScriptObjectMirrorType(((ScriptObjectMirror) resultValue).values());
            testCaseResult = resultCollection.equals(expectedJavaObject)
                    ? TestCaseResult.PASSED
                    : TestCaseResult.FAILED;
            testCaseWithResult.setActualValue(JsonUtils.format(convertCollectionToLiteral(resultCollection).toString()));
        }

        testCaseWithResult.setTestCaseResult(testCaseResult);
        testCaseWithResult.setResultMessage(OutputMessageAggregator.getOutputMessage());

    }

    private Collection<Object> unwrapScriptObjectMirrorType(Collection<Object> source) {
        return source.stream().map(obj -> {
            if (obj instanceof ScriptObjectMirror) {
                return unwrapScriptObjectMirrorType(((ScriptObjectMirror) obj).values());
            } else {
                return obj;
            }
        }).collect(Collectors.toList());
    }

    private Collection<Object> convertCollectionToLiteral(Collection<Object> source) {
        return source.stream().map(obj -> {
            if (obj instanceof Collection) {
                return convertCollectionToLiteral((Collection<Object>) obj);
            } else {
                return DataType.toLiteral(obj);
            }
        }).collect(Collectors.toList());
    }

}
