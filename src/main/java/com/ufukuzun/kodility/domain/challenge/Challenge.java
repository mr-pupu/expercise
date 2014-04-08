package com.ufukuzun.kodility.domain.challenge;

import com.ufukuzun.kodility.enums.Lingo;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Challenge {

    @Id
    private String id;

    private Map<Lingo, String> descriptions = new HashMap<Lingo, String>();

//    private List<Solution> solutions = new ArrayList<Solution>();

    private List<Class> inputTypes = new ArrayList<Class>();
    private Class<?> outputType;
    private List<TestCase> testCases = new ArrayList<TestCase>();

    private List<List<String>> inputs = new ArrayList<List<String>>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<Lingo, String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(Map<Lingo, String> descriptions) {
        this.descriptions = descriptions;
    }

//    public List<Solution> getSolutions() {
//        return solutions;
//    }
//
//    public void setSolutions(List<Solution> solutions) {
//        this.solutions = solutions;
//    }

    public List<List<String>> getInputs() {
        return inputs;
    }

    public void setInputs(List<List<String>> inputs) {
        this.inputs = inputs;
    }

//    public List<ProgrammingLanguage> getProgrammingLanguages() {
//        List<ProgrammingLanguage> programmingLanguages = new ArrayList<ProgrammingLanguage>();
//        for (Solution solution : solutions) {
//            programmingLanguages.add(solution.getProgrammingLanguage());
//        }
//        return programmingLanguages;
//    }

    public String getDescriptionFor(String lingoShortName) {
        Lingo lingo = Lingo.getLingo(lingoShortName);
        return descriptions.get(lingo);
    }

//    public Solution getSolutionFor(ProgrammingLanguage programmingLanguage) {
//        for (Solution solution : solutions) {
//            if (solution.getProgrammingLanguage() == programmingLanguage) {
//                return solution;
//            }
//        }
//        return null;
//    }

    //


    public List<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }

    public List<Class> getInputTypes() {
        return inputTypes;
    }

    public void setInputTypes(List<Class> inputTypes) {
        this.inputTypes = inputTypes;
    }

    public Class<?> getOutputType() {
        return outputType;
    }

    public void setOutputType(Class<?> outputType) {
        this.outputType = outputType;
    }

    public void addTestCase(TestCase testCase) {
        testCases.add(testCase);
    }
}
