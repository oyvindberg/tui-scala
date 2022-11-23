use jni::{
    objects::{JObject, JString},
    strings::{JNIString, JavaStr},
    JNIEnv,
};
use std::ops::BitOr;
use std::convert::TryInto;

use crossterm::{cursor, event, style, terminal};

fn get_name<'a>(env: JNIEnv<'a>, enum_value: JObject<'a>) -> JString<'a> {
    let object = env
        .call_method(enum_value, "name", "()Ljava/lang/String;", &[])
        .unwrap()
        .l()
        .unwrap();

    return object.into();
}

fn int_field<F>(env: JNIEnv, obj: JObject, name: F) -> u8
    where
        F: Into<JNIString>,
{
    return env
        .get_field(obj, name, "I")
        .unwrap()
        .i()
        .unwrap()
        .try_into()
        .unwrap();
}

pub fn clear_type(env: JNIEnv, enum_value: JObject) -> terminal::ClearType {
    let javastr: JavaStr = env.get_string(get_name(env, enum_value)).unwrap();

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

    return from_str(&javastr.to_string_lossy());
}

pub fn optional_color(env: JNIEnv, optional_object: JObject) -> Option<style::Color> {
    let is_empty = env
        .call_method(optional_object, "isEmpty", "()Z", &[])
        .unwrap()
        .z()
        .unwrap();

    return if is_empty {
        None
    } else {
        let value = env
            .call_method(optional_object, "get", "()Ljava/lang/Object;", &[])
            .unwrap()
            .l()
            .unwrap();
        Some(color(env, value))
    };
}

pub fn color(env: JNIEnv, record_object: JObject) -> style::Color {
    // let's save ourselves typing out all those is_instance_of checks
    let stringified_obj = env
        .call_method(record_object, "toString", "()Ljava/lang/String;", &[])
        .unwrap()
        .l()
        .unwrap();

    let stringified_javastr: JavaStr = env.get_string(stringified_obj.into()).unwrap();
    let str = stringified_javastr.to_string_lossy();
    // Blue[]
    // Rgb[r=1, g=2, b=3]
    let x: Vec<&str> = str.split("[").collect();

    match x[0] {
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
            r: int_field(env, record_object, "r"),
            g: int_field(env, record_object, "g"),
            b: int_field(env, record_object, "b"),
        },
        "AnsiValue" => style::Color::AnsiValue(int_field(env, record_object, "color")),
        other => {
            panic!("unknown color: {}", other);
        }
    }
}

pub fn attribute(env: JNIEnv, attribute_enum_value: JObject) -> style::Attribute {
    let java_str = env.get_string(get_name(env, attribute_enum_value)).unwrap();

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

    return from_str(&java_str.to_string_lossy());
}

pub fn attributes(env: JNIEnv, attributes_obj: JObject) -> style::Attributes {
    let attributes_list_obj = env
        .call_method(attributes_obj, "attributes", "()Ljava/util/List;", &[])
        .unwrap()
        .l()
        .unwrap();

    let list = env.get_list(attributes_list_obj).unwrap();

    let attributes: style::Attributes = list
        .iter()
        .unwrap()
        .map(|attribute_enum_value| attribute(env, attribute_enum_value))
        .fold(style::Attributes::default(), |acc, item| acc.bitor(item));

    return attributes;
}

pub fn cursor_shape(env: JNIEnv, enum_value: JObject) -> cursor::CursorShape {
    let java_str = env.get_string(get_name(env, enum_value)).unwrap();

    fn from_str(str: &str) -> cursor::CursorShape {
        match str {
            "UnderScore" => cursor::CursorShape::UnderScore,
            "Line" => cursor::CursorShape::Line,
            "Block" => cursor::CursorShape::Block,
            other => panic!("not a valid CursorShape: {}", other),
        }
    }
    return from_str(&java_str.to_string_lossy());
}

pub fn keyboard_enhancement_flags(
    env: JNIEnv,
    obj: JObject,
) -> event::KeyboardEnhancementFlags {
    let bits = int_field(env, obj, "bits");
    return event::KeyboardEnhancementFlags::from_bits_truncate(bits);
}
