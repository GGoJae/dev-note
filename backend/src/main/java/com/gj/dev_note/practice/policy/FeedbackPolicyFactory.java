package com.gj.dev_note.practice.policy;

import com.gj.dev_note.practice.domain.FeedbackMode;
import org.springframework.stereotype.Component;

@Component
public class FeedbackPolicyFactory {

    public FeedbackPolicy get(FeedbackMode mode) {
        return switch (mode) {
            case SECTION_END -> new SectionEndPolicy();
            case UNTIL_CORRECT -> new UntilCorrectPolicy();
        };
    }
}
