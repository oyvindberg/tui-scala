use std::convert::TryInto;
use std::io::Write;
use std::ops::BitOr;

use crossterm::{cursor, event, QueueableCommand, style, terminal};
use jni::{
    errors::Result as JniResult,
    JNIEnv,
    objects::{JObject, JString},
    strings::{JavaStr, JNIString},
    sys::jint,
};

use crate::unify_errors::{UnifiedError, UnifiedResult, UnifyErrors};

fn get_name<'a>(env: JNIEnv<'a>, enum_value: JObject<'a>) -> JniResult<JString<'a>> {
    let object = env
        .call_method(enum_value, "name", "()Ljava/lang/String;", &[])?
        .l()?;

    Ok(object.into())
}

fn int_field<F>(env: JNIEnv, obj: JObject, name: F) -> JniResult<jint> where F: Into<JNIString> {
    Ok(env.get_field(obj, name, "I")?.i()?)
}

// one doesn't simply...
fn as_rust_string(env: JNIEnv, _1: JObject) -> JniResult<String> {
    let _2: JString = _1.into();
    let _3: JavaStr = env.get_string(_2)?;
    let _4: String = _3.to_string_lossy().into();
    Ok(_4)
}

fn str_field<F>(env: JNIEnv, obj: JObject, name: F) -> JniResult<String> where F: Into<JNIString> {
    let object: JObject = env.get_field(obj, name, "Ljava/lang/String;")?.l()?;
    as_rust_string(env, object)
}

fn u16_field<F>(env: JNIEnv, obj: JObject, name: F) -> UnifiedResult<u16> where F: Into<JNIString> {
    let i = int_field(env, obj, name).unify_errors()?;
    <i32 as TryInto<u16>>::try_into(i).map_err(|_| UnifiedError::NotU16(i))
}

fn object_field<'a, F, T>(env: JNIEnv<'a>, obj: JObject<'a>, name: F, ty: T) -> JniResult<JObject<'a>> where F: Into<JNIString>, T: Into<JNIString> + AsRef<str> {
    Ok(env.get_field(obj, name, ty)?.l()?)
}

pub fn to_string(env: JNIEnv, obj: JObject) -> JniResult<String> {
    as_rust_string(env, env.call_method(obj, "toString", "()Ljava/lang/String;", &[])?.l()?)
}

pub fn clear_type(env: JNIEnv, enum_value: JObject) -> JniResult<terminal::ClearType> {
    let javastr: JavaStr = env.get_string(get_name(env, enum_value)?)?;

    pub fn from_str(str: &str) -> terminal::ClearType {
        match str {
            "All" => terminal::ClearType::All,
            "Purge" => terminal::ClearType::Purge,
            "FromCursorDown" => terminal::ClearType::FromCursorDown,
            "FromCursorUp" => terminal::ClearType::FromCursorUp,
            "CurrentLine" => terminal::ClearType::CurrentLine,
            "UntilNewLine" => terminal::ClearType::UntilNewLine,
            other => panic!("not a valid ClearType: {}", other),
        }
    }

    Ok(from_str(&javastr.to_string_lossy()))
}

pub fn optional_color(env: JNIEnv, optional_object: JObject) -> JniResult<Option<style::Color>> {
    let is_empty = env
        .call_method(optional_object, "isEmpty", "()Z", &[])?
        .z()?;

    return if is_empty {
        Ok(None)
    } else {
        let value = env
            .call_method(optional_object, "get", "()Ljava/lang/Object;", &[])?
            .l()?;
        Ok(Some(color(env, value)?))
    };
}

pub fn color(env: JNIEnv, record_object: JObject) -> JniResult<style::Color> {
    let str = to_string(env, record_object)?;
    // Blue[]
    // Rgb[r=1, g=2, b=3]
    let x: Vec<&str> = str.split("[").collect();

    let res = match x[0] {
        "Reset" => style::Color::Reset,
        "Black" => style::Color::Black,
        "DarkGrey" => style::Color::DarkGrey,
        "Red" => style::Color::Red,
        "DarkRed" => style::Color::DarkRed,
        "Green" => style::Color::Green,
        "DarkGreen" => style::Color::DarkGreen,
        "Yellow" => style::Color::Yellow,
        "DarkYellow" => style::Color::DarkYellow,
        "Blue" => style::Color::Blue,
        "DarkBlue" => style::Color::DarkBlue,
        "Magenta" => style::Color::Magenta,
        "DarkMagenta" => style::Color::DarkMagenta,
        "Cyan" => style::Color::Cyan,
        "DarkCyan" => style::Color::DarkCyan,
        "White" => style::Color::White,
        "Grey" => style::Color::Grey,
        "Rgb" => style::Color::Rgb {
            r: int_field(env, record_object, "r")?.try_into().unwrap(),
            g: int_field(env, record_object, "g")?.try_into().unwrap(),
            b: int_field(env, record_object, "b")?.try_into().unwrap(),
        },
        "AnsiValue" => style::Color::AnsiValue(int_field(env, record_object, "color")?.try_into().unwrap()),
        other => {
            panic!("unknown color: {}", other);
        }
    };

    Ok(res)
}

pub fn attribute(env: JNIEnv, attribute_enum_value: JObject) -> JniResult<style::Attribute> {
    let java_str = env.get_string(get_name(env, attribute_enum_value)?)?;

    // fully aware that all this should have been a bitmask :(
    fn from_str(str: &str) -> style::Attribute {
        match str {
            "Reset" => style::Attribute::Reset,
            "Bold" => style::Attribute::Bold,
            "Dim" => style::Attribute::Dim,
            "Italic" => style::Attribute::Italic,
            "Underlined" => style::Attribute::Underlined,
            "DoubleUnderlined" => style::Attribute::DoubleUnderlined,
            "Undercurled" => style::Attribute::Undercurled,
            "Underdotted" => style::Attribute::Underdotted,
            "Underdashed" => style::Attribute::Underdashed,
            "SlowBlink" => style::Attribute::SlowBlink,
            "RapidBlink" => style::Attribute::RapidBlink,
            "Reverse" => style::Attribute::Reverse,
            "Hidden" => style::Attribute::Hidden,
            "CrossedOut" => style::Attribute::CrossedOut,
            "Fraktur" => style::Attribute::Fraktur,
            "NoBold" => style::Attribute::NoBold,
            "NormalIntensity" => style::Attribute::NormalIntensity,
            "NoItalic" => style::Attribute::NoItalic,
            "NoUnderline" => style::Attribute::NoUnderline,
            "NoBlink" => style::Attribute::NoBlink,
            "NoReverse" => style::Attribute::NoReverse,
            "NoHidden" => style::Attribute::NoHidden,
            "NotCrossedOut" => style::Attribute::NotCrossedOut,
            "Framed" => style::Attribute::Framed,
            "Encircled" => style::Attribute::Encircled,
            "OverLined" => style::Attribute::OverLined,
            "NotFramedOrEncircled" => style::Attribute::NotFramedOrEncircled,
            "NotOverLined" => style::Attribute::NotOverLined,
            other => panic!("not a valid attribute: {}", other),
        }
    }

    return Ok(from_str(&java_str.to_string_lossy()));
}

pub fn attributes(env: JNIEnv, attributes_obj: JObject) -> JniResult<style::Attributes> {
    let attributes_list_obj = env
        .call_method(attributes_obj, "attributes", "()Ljava/util/List;", &[])?
        .l()?;

    return attributes_list(env, attributes_list_obj);
}

pub fn attributes_list(env: JNIEnv, attributes_list_obj: JObject) -> JniResult<style::Attributes> {
    let list = env.get_list(attributes_list_obj)?;

    let attributes: style::Attributes = list
        .iter()?
        .map(|attribute_enum_value| attribute(env, attribute_enum_value).unwrap())
        .fold(style::Attributes::default(), |acc, item| acc.bitor(item));

    return Ok(attributes);
}

pub fn cursor_shape(env: JNIEnv, enum_value: JObject) -> JniResult<cursor::CursorShape> {
    let java_str = env.get_string(get_name(env, enum_value)?)?;

    fn from_str(str: &str) -> cursor::CursorShape {
        match str {
            "UnderScore" => cursor::CursorShape::UnderScore,
            "Line" => cursor::CursorShape::Line,
            "Block" => cursor::CursorShape::Block,
            other => panic!("not a valid CursorShape: {}", other),
        }
    }
    return Ok(from_str(&java_str.to_string_lossy()));
}

pub fn keyboard_enhancement_flags(
    env: JNIEnv,
    obj: JObject,
) -> JniResult<event::KeyboardEnhancementFlags> {
    let bits = int_field(env, obj, "bits")?;
    return Ok(event::KeyboardEnhancementFlags::from_bits_truncate(bits.try_into().unwrap()));
}

pub fn queue_commands<W: Write>(w: &mut W, env: JNIEnv, list_obj: JObject) -> UnifiedResult<()> {
    let list = env.get_list(list_obj).unify_errors()?;
    for obj in list.iter().unify_errors()? {
        queue_command(w, env, obj)?
    }
    return Ok(());
}

/// it's clearly weird for this to queue directly.
/// That is because it seems to be impossible to return `impl Command` from a function, because
/// it is apparently not "object safe" so dynamic dispatch cannot be used.
pub fn queue_command<W: Write>(w: &mut W, env: JNIEnv, obj: JObject) -> UnifiedResult<()> {
    let str = to_string(env, obj).unify_errors()?;
    // eprintln!("{str:?}");
    let parts: Vec<&str> = str.split("[").collect();
    let class_name = parts[0];

    match class_name {
        "MoveTo" => {
            let x = u16_field(env, obj, "x")?;
            let y = u16_field(env, obj, "y")?;
            w.queue(cursor::MoveTo(x, y)).unify_errors()?
        }
        "MoveToNextLine" => {
            let num_lines = u16_field(env, obj, "num_lines")?;
            w.queue(cursor::MoveToNextLine(num_lines)).unify_errors()?
        }
        "MoveToPreviousLine" => {
            let num_lines = u16_field(env, obj, "num_lines")?;
            w.queue(cursor::MoveToPreviousLine(num_lines)).unify_errors()?
        }
        "MoveToColumn" => {
            let num_lines = u16_field(env, obj, "num_lines")?;
            w.queue(cursor::MoveToColumn(num_lines)).unify_errors()?
        }
        "MoveToRow" => {
            let row = u16_field(env, obj, "row")?;
            w.queue(cursor::MoveToRow(row)).unify_errors()?
        }
        "MoveUp" => {
            let num_rows = u16_field(env, obj, "num_rows")?;
            w.queue(cursor::MoveUp(num_rows)).unify_errors()?
        }
        "MoveRight" => {
            let num_columns = u16_field(env, obj, "num_columns")?;
            w.queue(cursor::MoveRight(num_columns)).unify_errors()?
        }
        "MoveDown" => {
            let num_rows = u16_field(env, obj, "num_rows")?;
            w.queue(cursor::MoveDown(num_rows)).unify_errors()?
        }
        "MoveLeft" => {
            let num_columns = u16_field(env, obj, "num_columns")?;
            w.queue(cursor::MoveLeft(num_columns)).unify_errors()?
        }
        "SavePosition" => {
            w.queue(cursor::SavePosition).unify_errors()?
        }
        "RestorePosition" => {
            w.queue(cursor::RestorePosition).unify_errors()?
        }
        "Hide" => {
            w.queue(cursor::Hide).unify_errors()?
        }
        "Show" => {
            w.queue(cursor::Show).unify_errors()?
        }
        "EnableBlinking" => {
            w.queue(cursor::EnableBlinking).unify_errors()?
        }
        "DisableBlinking" => {
            w.queue(cursor::DisableBlinking).unify_errors()?
        }
        "SetCursorShape" => {
            let x = cursor_shape(env, object_field(env, obj, "cursor_shape", "Ltui/crossterm/CursorShape;").unify_errors()?).unify_errors()?;
            w.queue(cursor::SetCursorShape(x)).unify_errors()?
        }
        "EnableMouseCapture" => {
            w.queue(event::EnableMouseCapture).unify_errors()?
        }
        "DisableMouseCapture" => {
            w.queue(event::DisableMouseCapture).unify_errors()?
        }
        "PushKeyboardEnhancementFlags" => {
            let x = keyboard_enhancement_flags(env, object_field(env, obj, "flags", "Ltui/crossterm/KeyboardEnhancementFlags;").unify_errors()?).unify_errors()?;
            w.queue(event::PushKeyboardEnhancementFlags(x)).unify_errors()?
        }
        "PopKeyboardEnhancementFlags" => {
            w.queue(event::PopKeyboardEnhancementFlags).unify_errors()?
        }
        "EnableFocusChange" => {
            w.queue(event::EnableFocusChange).unify_errors()?
        }
        "DisableFocusChange" => {
            w.queue(event::DisableFocusChange).unify_errors()?
        }
        "EnableBracketedPaste" => {
            w.queue(event::EnableBracketedPaste).unify_errors()?
        }
        "DisableBracketedPaste" => {
            w.queue(event::DisableBracketedPaste).unify_errors()?
        }
        "SetForegroundColor" => {
            let x = color(env, object_field(env, obj, "color", "Ltui/crossterm/Color;").unify_errors()?).unify_errors()?;
            w.queue(style::SetForegroundColor(x)).unify_errors()?
        }
        "SetBackgroundColor" => {
            let x = color(env, object_field(env, obj, "color", "Ltui/crossterm/Color;").unify_errors()?).unify_errors()?;
            w.queue(style::SetBackgroundColor(x)).unify_errors()?
        }
        "SetUnderlineColor" => {
            let x = color(env, object_field(env, obj, "color", "Ltui/crossterm/Color;").unify_errors()?).unify_errors()?;
            w.queue(style::SetUnderlineColor(x)).unify_errors()?
        }
        "SetColors" => {
            let foreground = optional_color(env, object_field(env, obj, "foreground", "Ltui/crossterm/Color;").unify_errors()?).unify_errors()?;
            let background = optional_color(env, object_field(env, obj, "background", "Ltui/crossterm/Color;").unify_errors()?).unify_errors()?;
            let set_colors = style::SetColors(style::Colors { foreground, background });
            w.queue(set_colors).unify_errors()?
        }
        "SetAttribute" => {
            let x = attribute(env, object_field(env, obj, "attribute", "Ltui/crossterm/Attribute;").unify_errors()?).unify_errors()?;
            w.queue(style::SetAttribute(x)).unify_errors()?
        }
        "SetAttributes" => {
            let x = attributes_list(env, object_field(env, obj, "attributes", "Ljava/util/List;").unify_errors()?).unify_errors()?;
            w.queue(style::SetAttributes(x)).unify_errors()?
        }
        "SetStyle" => {
            let foreground_color = optional_color(env, object_field(env, obj, "foreground_color", "Ltui/crossterm/Color;").unify_errors()?).unify_errors()?;
            let background_color = optional_color(env, object_field(env, obj, "background_color", "Ltui/crossterm/Color;").unify_errors()?).unify_errors()?;
            let underline_color = optional_color(env, object_field(env, obj, "underline_color", "Ltui/crossterm/Color;").unify_errors()?).unify_errors()?;
            let attributes = attributes_list(env, object_field(env, obj, "attributes", "Ljava/util/List;").unify_errors()?).unify_errors()?;
            let content_style = style::ContentStyle { foreground_color, background_color, underline_color, attributes };
            w.queue(style::SetStyle(content_style)).unify_errors()?
        }
        "ResetColor" => {
            w.queue(style::ResetColor).unify_errors()?
        }
        "DisableLineWrap" => {
            w.queue(terminal::DisableLineWrap).unify_errors()?
        }
        "EnableLineWrap" => {
            w.queue(terminal::EnableLineWrap).unify_errors()?
        }
        "EnterAlternateScreen" => {
            w.queue(terminal::EnterAlternateScreen).unify_errors()?
        }
        "LeaveAlternateScreen" => {
            w.queue(terminal::LeaveAlternateScreen).unify_errors()?
        }
        "ScrollUp" => {
            let num_rows = u16_field(env, obj, "num_rows")?;
            w.queue(terminal::ScrollUp(num_rows)).unify_errors()?
        }
        "ScrollDown" => {
            let num_rows = u16_field(env, obj, "num_rows")?;
            w.queue(terminal::ScrollDown(num_rows)).unify_errors()?
        }
        "Clear" => {
            let x = clear_type(env, object_field(env, obj, "clear_type", "Ltui/crossterm/ClearType;").unify_errors()?).unify_errors()?;
            w.queue(terminal::Clear(x)).unify_errors()?
        }
        "SetSize" => {
            let columns = u16_field(env, obj, "columns")?;
            let rows = u16_field(env, obj, "rows")?;
            w.queue(terminal::SetSize(columns, rows)).unify_errors()?
        }
        "Print" => {
            let value = str_field(env, obj, "value").unify_errors()?;
            w.queue(style::Print(value)).unify_errors()?
        }
        other => panic!("Not a valid Command: {}", other),
    };
    Ok(())
}
