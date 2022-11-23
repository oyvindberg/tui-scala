use std::convert::TryInto;
use std::io::Write;
use std::time::Duration;

use crossterm::{cursor, event, queue, style, terminal};
use jni::JNIEnv;
use jni::objects::{JClass, JObject, JString};
use jni::sys::{jboolean, jint, jobject};

use crate::{jni_from_jvm, jni_to_jvm};

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_terminalSize(
    env: JNIEnv,
    _class: JClass,
) -> jobject {
    let (x, y) = terminal::size().unwrap();
    return jni_to_jvm::xy(env, x, y).into_raw();
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_cursorPosition(
    env: JNIEnv,
    _class: JClass,
) -> jobject {
    let (x, y) = cursor::position().unwrap();
    return jni_to_jvm::xy(env, x, y).into_raw();
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_flush(_env: JNIEnv, _class: JClass) {
    std::io::stdout().flush().unwrap();
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_disableRawMode(
    _env: JNIEnv,
    _class: JClass,
) {
    terminal::disable_raw_mode().unwrap()
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enableRawMode(_env: JNIEnv, _class: JClass) {
    terminal::enable_raw_mode().unwrap()
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_poll(
    env: JNIEnv,
    _class: JClass,
    timeout: JObject,
) -> jboolean {
    let secs_jlong = env.get_field(timeout, "secs", "J").unwrap().j().unwrap();
    let nanos_jint = env.get_field(timeout, "nanos", "I").unwrap().i().unwrap();
    let duration = Duration::new(
        secs_jlong.try_into().unwrap_or_default(),
        nanos_jint.try_into().unwrap_or_default(),
    );
    let res = event::poll(duration).unwrap();
    return res.into();
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_read(
    env: JNIEnv,
    _class: JClass,
) -> jobject {
    let e = event::read().unwrap();
    return jni_to_jvm::event(env, e).into_raw();
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueTerminalClear(
    env: JNIEnv,
    _class: JClass,
    clear_type_enum_value: JObject,
) {
    let clear_type = jni_from_jvm::clear_type(env, clear_type_enum_value);

    queue!(std::io::stdout(), terminal::Clear(clear_type)).unwrap()
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueEventPushKeyboardEnhancementFlags(
    env: JNIEnv,
    _class: JClass,
    keyboard_enchancement_obj: JObject,
) {
    let flags = jni_from_jvm::keyboard_enhancement_flags(env, keyboard_enchancement_obj);
    queue!(
        std::io::stdout(),
        event::PushKeyboardEnhancementFlags(flags)
    )
        .unwrap()
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorSetCursorShape(
    env: JNIEnv,
    _class: JClass,
    cursor_shape_enum: JObject,
) {
    let cursor_shape = jni_from_jvm::cursor_shape(env, cursor_shape_enum);
    queue!(std::io::stdout(), cursor::SetCursorShape(cursor_shape)).unwrap();
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueStyleSetAttribute(
    env: JNIEnv,
    _class: JClass,
    attribute_enum_value: JObject,
) {
    let attribute = jni_from_jvm::attribute(env, attribute_enum_value);

    queue!(std::io::stdout(), style::SetAttribute(attribute)).unwrap()
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueStyleSetAttributes(
    env: JNIEnv,
    _class: JClass,
    attributes_obj: JObject,
) {
    let attributes: style::Attributes = jni_from_jvm::attributes(env, attributes_obj);
    queue!(std::io::stdout(), style::SetAttributes(attributes)).unwrap()
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueStyleSetBackgroundColor(
    env: JNIEnv,
    _class: JClass,
    color_record_object: JObject,
) {
    let color = jni_from_jvm::color(env, color_record_object);

    queue!(std::io::stdout(), style::SetBackgroundColor(color)).unwrap()
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueStyleSetColors(
    env: JNIEnv,
    _class: JClass,
    foreground: JObject,
    background: JObject,
) {
    let foreground = jni_from_jvm::optional_color(env, foreground);
    let background = jni_from_jvm::optional_color(env, background);

    let colors = style::Colors {
        foreground,
        background,
    };
    queue!(std::io::stdout(), style::SetColors(colors)).unwrap()
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueStyleSetForegroundColor(
    env: JNIEnv,
    _class: JClass,
    color_record_object: JObject,
) {
    let color = jni_from_jvm::color(env, color_record_object);

    queue!(std::io::stdout(), style::SetForegroundColor(color)).unwrap()
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
        foreground_color: jni_from_jvm::optional_color(env, foreground_color),
        background_color: jni_from_jvm::optional_color(env, background_color),
        underline_color: jni_from_jvm::optional_color(env, underline_color),
        attributes: jni_from_jvm::attributes(env, attributes),
    };
    queue!(std::io::stdout(), style::SetStyle(content_style)).unwrap()
}

#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueStyleSetUnderlineColor(
    env: JNIEnv,
    _class: JClass,
    color_record_object: JObject,
) {
    let color = jni_from_jvm::color(env, color_record_object);

    queue!(std::io::stdout(), style::SetUnderlineColor(color)).unwrap()
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

    queue!(std::io::stdout(), style::Print(input)).unwrap()
}

// events
#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorDisableBlinking(_env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), cursor::DisableBlinking).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorEnableBlinking(_env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), cursor::EnableBlinking).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorHide(_env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), cursor::Hide).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorMoveDown(_env: JNIEnv, _class: JClass, num_rows: jint) { queue!(std::io::stdout(), cursor::MoveDown(num_rows as u16)).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorMoveLeft(_env: JNIEnv, _class: JClass, num_cols: jint) { queue!(std::io::stdout(), cursor::MoveLeft(num_cols as u16)).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorMoveRight(_env: JNIEnv, _class: JClass, num_cols: jint) { queue!(std::io::stdout(), cursor::MoveRight(num_cols as u16)).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorMoveTo(_env: JNIEnv, _class: JClass, x: jint, y: jint) { queue!(std::io::stdout(), cursor::MoveTo(x as u16, y as u16)).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorMoveToColumn(_env: JNIEnv, _class: JClass, col: jint) { queue!(std::io::stdout(), cursor::MoveToColumn(col as u16)).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorMoveToNextLine(_env: JNIEnv, _class: JClass, num_lines: jint) { queue!(std::io::stdout(), cursor::MoveToNextLine(num_lines as u16)).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorMoveToPreviousLine(_env: JNIEnv, _class: JClass, num_lines: jint) { queue!(std::io::stdout(), cursor::MoveToPreviousLine(num_lines as u16)).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorMoveToRow(_env: JNIEnv, _class: JClass, row: jint) { queue!(std::io::stdout(), cursor::MoveToRow(row as u16)).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorMoveUp(_env: JNIEnv, _class: JClass, num_rows: jint) { queue!(std::io::stdout(), cursor::MoveUp(num_rows as u16)).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorRestorePosition(_env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), cursor::RestorePosition).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorSavePosition(_env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), cursor::SavePosition).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueCursorShow(_env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), cursor::Show).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueEventDisableBracketedPaste(_env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), event::DisableBracketedPaste).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueEventDisableFocusChange(_env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), event::DisableFocusChange).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueEventDisableMouseCapture(_env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), event::DisableMouseCapture).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueEventEnableBracketedPaste(_env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), event::EnableBracketedPaste).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueEventEnableFocusChange(_env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), event::EnableFocusChange).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueEventEnableMouseCapture(_env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), event::EnableMouseCapture).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueEventPopKeyboardEnhancementFlags(_env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), event::PopKeyboardEnhancementFlags).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueStyleResetColor(_env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), style::ResetColor).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueTerminalDisableLineWrap(_env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), terminal::DisableLineWrap).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueTerminalEnableLineWrap(_env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), terminal::EnableLineWrap).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueTerminalEnterAlternateScreen(_env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), terminal::EnterAlternateScreen).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueTerminalLeaveAlternateScreen(_env: JNIEnv, _class: JClass) { queue!(std::io::stdout(), terminal::LeaveAlternateScreen).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueTerminalScrollDown(_env: JNIEnv, _class: JClass, value: u16) { queue!(std::io::stdout(), terminal::ScrollDown(value)).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueTerminalScrollUp(_env: JNIEnv, _class: JClass, value: u16) { queue!(std::io::stdout(), terminal::ScrollUp(value)).unwrap() }

#[rustfmt::skip]
#[no_mangle]
pub extern "system" fn Java_tui_crossterm_CrosstermJni_enqueueTerminalSetSize(_env: JNIEnv, _class: JClass, x: u16, y: u16) { queue!(std::io::stdout(), terminal::SetSize(x, y)).unwrap() }
