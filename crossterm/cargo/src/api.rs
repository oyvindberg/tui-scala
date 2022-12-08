use std::convert::TryInto;
use std::io::Write;
use std::time::Duration;

use crossterm::{cursor, event, queue, style, terminal};
use jni::{
    JNIEnv,
    objects::{JClass, JObject, JString},
    sys::{jboolean, jint, jobject},
};

use crate::{
    jni_from_jvm,
    jni_to_jvm,
    jvm_unwrapper::JvmUnwrapper,
    unify_errors::UnifyErrors
};

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_terminalSize(
    env: JNIEnv,
    _class: JClass,
) -> jobject {
    let xy = terminal::size().unify_errors().and_then(|(x, y)| jni_to_jvm::xy(env, x, y).unify_errors());
    return xy.jvm_unwrap(env).into_raw();
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_cursorPosition(
    env: JNIEnv,
    _class: JClass,
) -> jobject {
    let (x, y) = cursor::position().jvm_unwrap(env);
    let result = jni_to_jvm::xy(env, x, y);
    return result.jvm_unwrap(env).into_raw();
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_flush(env: JNIEnv, _class: JClass) {
    std::io::stdout().flush().jvm_unwrap(env);
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_disableRawMode(
    env: JNIEnv,
    _class: JClass,
) {
    terminal::disable_raw_mode().jvm_unwrap(env)
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enableRawMode(env: JNIEnv, _class: JClass) {
    terminal::enable_raw_mode().jvm_unwrap(env)
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_poll(
    env: JNIEnv,
    _class: JClass,
    timeout: JObject,
) -> jboolean {
    let secs_jlong = env.get_field(timeout, "secs", "J").and_then(|x| x.j()).jvm_unwrap(env);
    let nanos_jint = env.get_field(timeout, "nanos", "I").and_then(|x| x.i()).jvm_unwrap(env);
    let duration = Duration::new(
        secs_jlong.try_into().unwrap_or_default(),
        nanos_jint.try_into().unwrap_or_default(),
    );
    let res = event::poll(duration).jvm_unwrap(env);
    return res.into();
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_read(
    env: JNIEnv,
    _class: JClass,
) -> jobject {
    let e = event::read().unify_errors().and_then(|e| jni_to_jvm::event(env, e).unify_errors());
    return e.jvm_unwrap(env).into_raw();
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueTerminalClear(
    env: JNIEnv,
    _class: JClass,
    clear_type_enum_value: JObject,
) {
    jni_from_jvm::clear_type(env, clear_type_enum_value)
        .unify_errors()
        .and_then(|clear_type| queue!(std::io::stdout(), terminal::Clear(clear_type)).unify_errors())
        .jvm_unwrap(env);
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueEventPushKeyboardEnhancementFlags(
    env: JNIEnv,
    _class: JClass,
    keyboard_enchancement_obj: JObject,
) {
    jni_from_jvm::keyboard_enhancement_flags(env, keyboard_enchancement_obj)
        .unify_errors()
        .and_then(|flags| queue!(std::io::stdout(),event::PushKeyboardEnhancementFlags(flags)).unify_errors())
        .jvm_unwrap(env)
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorSetCursorShape(
    env: JNIEnv,
    _class: JClass,
    cursor_shape_enum: JObject,
) {
    jni_from_jvm::cursor_shape(env, cursor_shape_enum)
        .unify_errors()
        .and_then(|cursor_shape| queue!(std::io::stdout(), cursor::SetCursorShape(cursor_shape)).unify_errors())
        .jvm_unwrap(env);
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueStyleSetAttribute(
    env: JNIEnv,
    _class: JClass,
    attribute_enum_value: JObject,
) {
    jni_from_jvm::attribute(env, attribute_enum_value)
        .unify_errors()
        .and_then(|attribute| queue!(std::io::stdout(), style::SetAttribute(attribute)).unify_errors())
        .jvm_unwrap(env)
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueStyleSetAttributes(
    env: JNIEnv,
    _class: JClass,
    attributes_obj: JObject,
) {
    let attributes: style::Attributes = jni_from_jvm::attributes(env, attributes_obj).jvm_unwrap(env);
    queue!(std::io::stdout(), style::SetAttributes(attributes)).jvm_unwrap(env)
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueStyleSetBackgroundColor(
    env: JNIEnv,
    _class: JClass,
    color_record_object: JObject,
) {
    jni_from_jvm::color(env, color_record_object)
        .unify_errors()
        .and_then(|color| queue!(std::io::stdout(), style::SetBackgroundColor(color)).unify_errors())
        .jvm_unwrap(env)
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueStyleSetColors(
    env: JNIEnv,
    _class: JClass,
    foreground: JObject,
    background: JObject,
) {
    let foreground = jni_from_jvm::optional_color(env, foreground).jvm_unwrap(env);
    let background = jni_from_jvm::optional_color(env, background).jvm_unwrap(env);

    let colors = style::Colors {
        foreground,
        background,
    };
    queue!(std::io::stdout(), style::SetColors(colors)).jvm_unwrap(env)
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueStyleSetForegroundColor(
    env: JNIEnv,
    _class: JClass,
    color_record_object: JObject,
) {
    jni_from_jvm::color(env, color_record_object)
        .unify_errors()
        .and_then(|color| queue!(std::io::stdout(), style::SetForegroundColor(color)).unify_errors())
        .jvm_unwrap(env)
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueStyleSetStyle(
    env: JNIEnv,
    _class: JClass,
    // The foreground color. Optional<Color>
    foreground_color: JObject,
    // The background color. Optional<Color>
    background_color: JObject,
    // The underline color. Optional<Color>
    underline_color: JObject,
    // List of attributes. Attributes
    attributes: JObject,
) {
    let content_style = style::ContentStyle {
        foreground_color: jni_from_jvm::optional_color(env, foreground_color).jvm_unwrap(env),
        background_color: jni_from_jvm::optional_color(env, background_color).jvm_unwrap(env),
        underline_color: jni_from_jvm::optional_color(env, underline_color).jvm_unwrap(env),
        attributes: jni_from_jvm::attributes(env, attributes).jvm_unwrap(env),
    };
    queue!(std::io::stdout(), style::SetStyle(content_style)).jvm_unwrap(env)
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueStyleSetUnderlineColor(
    env: JNIEnv,
    _class: JClass,
    color_record_object: JObject,
) {
    jni_from_jvm::color(env, color_record_object)
        .unify_errors()
        .and_then(|color| queue!(std::io::stdout(), style::SetUnderlineColor(color)).unify_errors())
        .jvm_unwrap(env)
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueStylePrint(
    env: JNIEnv,
    _class: JClass,
    value: JString,
) {
    let input: String = env
        .get_string(value)
        .expect("enqueue_style_print: Couldn't get java string!")
        .into();

    queue!(std::io::stdout(), style::Print(input)).jvm_unwrap(env)
}

// events
#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorDisableBlinking(env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), cursor::DisableBlinking).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorEnableBlinking(env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), cursor::EnableBlinking).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorHide(env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), cursor::Hide).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorMoveDown(env: JNIEnv, _class: JClass, num_rows: jint) { queue!(std::io::stdout(), cursor::MoveDown(num_rows as u16)).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorMoveLeft(env: JNIEnv, _class: JClass, num_cols: jint) { queue!(std::io::stdout(), cursor::MoveLeft(num_cols as u16)).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorMoveRight(env: JNIEnv, _class: JClass, num_cols: jint) { queue!(std::io::stdout(), cursor::MoveRight(num_cols as u16)).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorMoveTo(env: JNIEnv, _class: JClass, x: jint, y: jint) { queue!(std::io::stdout(), cursor::MoveTo(x as u16, y as u16)).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorMoveToColumn(env: JNIEnv, _class: JClass, col: jint) { queue!(std::io::stdout(), cursor::MoveToColumn(col as u16)).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorMoveToNextLine(env: JNIEnv, _class: JClass, num_lines: jint) { queue!(std::io::stdout(), cursor::MoveToNextLine(num_lines as u16)).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorMoveToPreviousLine(env: JNIEnv, _class: JClass, num_lines: jint) { queue!(std::io::stdout(), cursor::MoveToPreviousLine(num_lines as u16)).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorMoveToRow(env: JNIEnv, _class: JClass, row: jint) { queue!(std::io::stdout(), cursor::MoveToRow(row as u16)).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorMoveUp(env: JNIEnv, _class: JClass, num_rows: jint) { queue!(std::io::stdout(), cursor::MoveUp(num_rows as u16)).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorRestorePosition(env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), cursor::RestorePosition).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorSavePosition(env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), cursor::SavePosition).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorShow(env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), cursor::Show).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueEventDisableBracketedPaste(env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), event::DisableBracketedPaste).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueEventDisableFocusChange(env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), event::DisableFocusChange).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueEventDisableMouseCapture(env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), event::DisableMouseCapture).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueEventEnableBracketedPaste(env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), event::EnableBracketedPaste).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueEventEnableFocusChange(env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), event::EnableFocusChange).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueEventEnableMouseCapture(env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), event::EnableMouseCapture).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueEventPopKeyboardEnhancementFlags(env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), event::PopKeyboardEnhancementFlags).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueStyleResetColor(env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), style::ResetColor).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueTerminalDisableLineWrap(env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), terminal::DisableLineWrap).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueTerminalEnableLineWrap(env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), terminal::EnableLineWrap).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueTerminalEnterAlternateScreen(env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), terminal::EnterAlternateScreen).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueTerminalLeaveAlternateScreen(env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), terminal::LeaveAlternateScreen).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueTerminalScrollDown(env: JNIEnv, _class: JClass, value: u16) { queue!(std::io::stdout(), terminal::ScrollDown(value)).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueTerminalScrollUp(env: JNIEnv, _class: JClass, value: u16) { queue!(std::io::stdout(), terminal::ScrollUp(value)).jvm_unwrap(env) }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueTerminalSetSize(env: JNIEnv, _class: JClass, x: u16, y: u16) { queue!(std::io::stdout(), terminal::SetSize(x, y)).jvm_unwrap(env) }
