package me.kuwg.clarity.interpreter;

import me.kuwg.clarity.register.Register;

import static me.kuwg.clarity.library.objects.VoidObject.VOID_OBJECT;

final class ExemptionHandler {
    private boolean isExempt;
    private String exemptMessage;
    private Integer exemptLine;

    void checkExemption() {
        if (this.isExempt) {
            if (this.exemptLine != null) Register.throwException(this.exemptMessage, this.exemptLine);
            else Register.throwException(this.exemptMessage);
        }
    }

    Object except(final String message, final int line) {
        if (this.isExempt) {
            if (this.exemptLine != null) Register.throwException(this.exemptMessage, this.exemptLine);
            else Register.throwException(this.exemptMessage);
        }

        this.isExempt = true;
        this.exemptMessage = message;
        this.exemptLine = line;
        return VOID_OBJECT;
    }

    void except(final String message) {
        if (this.isExempt) {
            if (this.exemptLine != null) Register.throwException(this.exemptMessage, this.exemptLine);
            else Register.throwException(this.exemptMessage);
        }

        this.isExempt = true;
        this.exemptMessage = message;
        this.exemptLine = null;
    }

    @SuppressWarnings("ConstantValue")
    boolean changeIfGet() {
        return this.isExempt && !(this.isExempt = false);
    }

    String exemptMessage() {
        return this.exemptMessage;
    }
}
