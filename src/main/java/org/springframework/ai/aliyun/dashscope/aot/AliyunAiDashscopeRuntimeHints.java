package org.springframework.ai.aliyun.dashscope.aot;

import org.springframework.ai.aliyun.dashscope.AliyunAiDashscopeChatOptions;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import static org.springframework.ai.aot.AiRuntimeHints.findJsonAnnotatedClassesInPackage;

public class AliyunAiDashscopeRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        var mcs = MemberCategory.values();
        for (var tr : findJsonAnnotatedClassesInPackage(AliyunAiDashscopeChatOptions.class)) {
            hints.reflection().registerType(tr, mcs);
        }
    }

}
