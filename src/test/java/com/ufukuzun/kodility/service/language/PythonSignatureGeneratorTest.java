package com.ufukuzun.kodility.service.language;

import com.ufukuzun.kodility.domain.challenge.Challenge;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class PythonSignatureGeneratorTest {

    private PythonSignatureGenerator signatureGenerator = new PythonSignatureGenerator();

    @Test
    public void shouldGeneratePatternWithoutParameters() {
        Challenge challenge = new Challenge();

        assertThat(signatureGenerator.generate(challenge), equalTo("def solution():\n"));
    }

    @Test
    public void shouldGeneratePatternWithMultipleParameters() {
        Challenge challenge = new Challenge();

        List<Class> inputTypes = new ArrayList<Class>();
        inputTypes.add(Integer.class);
        inputTypes.add(Integer.class);
        challenge.setInputTypes(inputTypes);
        challenge.setOutputType(Integer.class);

        assertThat(signatureGenerator.generate(challenge), equalTo("def solution(a, b):\n"));
    }


}
