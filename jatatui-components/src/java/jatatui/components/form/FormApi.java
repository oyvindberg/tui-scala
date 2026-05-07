package jatatui.components.form;

import jatatui.react.Context;
import jatatui.react.RenderContext;
import java.util.Map;
import java.util.Optional;

/// Form runtime — held in Context by the [FormProvider] and read by [#useField] in descendants.
///
/// Field values are typed as `Object` (heterogeneous fields). [FieldApi] surfaces `Object`
/// directly; cast at the call site, or use a wrapper.
public interface FormApi {

  /// Current value for `field`, or [Optional#empty] if not set.
  Optional<Object> getValue(String field);

  /// Set the value for `field`. Triggers validation + re-render.
  void setValue(String field, Object value);

  /// Validation error for `field`, or empty if valid.
  Optional<String> getError(String field);

  /// All current values (immutable snapshot).
  Map<String, Object> values();

  /// All current errors (immutable snapshot).
  Map<String, String> errors();

  /// True if any field has a validation error.
  boolean hasErrors();

  /// Run `onSubmit` if `validate` returns no errors. Sets `submitting` to true while running.
  void submit();

  /// Reset all fields to their initial values.
  void reset();

  /// True between [#submit] dispatch and `onSubmit` completion.
  boolean isSubmitting();

  /// Default no-op (when no provider is mounted).
  FormApi NO_OP =
      new FormApi() {
        @Override
        public Optional<Object> getValue(String field) {
          return Optional.empty();
        }

        @Override
        public void setValue(String field, Object value) {}

        @Override
        public Optional<String> getError(String field) {
          return Optional.empty();
        }

        @Override
        public Map<String, Object> values() {
          return Map.of();
        }

        @Override
        public Map<String, String> errors() {
          return Map.of();
        }

        @Override
        public boolean hasErrors() {
          return false;
        }

        @Override
        public void submit() {}

        @Override
        public void reset() {}

        @Override
        public boolean isSubmitting() {
          return false;
        }
      };

  Context<FormApi> CONTEXT = Context.create(NO_OP);

  /// Read the form API from context.
  static FormApi useForm(RenderContext ctx) {
    return ctx.useContext(CONTEXT);
  }

  /// Read the typed view of a single field. Cast `value` / `setValue` at the call site.
  @SuppressWarnings("unchecked")
  static <T> FieldApi<T> useField(RenderContext ctx, String name, T defaultValue) {
    FormApi api = useForm(ctx);
    T value = (T) api.getValue(name).orElse(defaultValue);
    return new FieldApi<>(value, v -> api.setValue(name, v), api.getError(name));
  }
}
