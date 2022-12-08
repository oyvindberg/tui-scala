use jni::{
    errors::{Result as JniResult},
    objects::{JClass, JObject, JValue},
    JNIEnv,
};

use crossterm::event;

pub fn record<'a>(env: JNIEnv<'a>, class: &str, params_sig: &str, args: &[JValue]) -> JniResult<JObject<'a>> {
    return env
        .new_object(
            env.find_class(class)?,
            format!("({})V", params_sig),
            args,
        )
}

pub fn enum_value<'a>(env: JNIEnv<'a>, cls_name: &str, name: &str) -> JniResult<JObject<'a>> {
    let cls: JClass = env.find_class(cls_name)?;
    let sig = format!("L{};", cls_name);
    return env.get_static_field(cls, name, sig)?.l();
}

pub fn xy(env: JNIEnv, x: u16, y: u16) -> JniResult<JObject> {
    return record(
        env,
        "tui/crossterm/Xy",
        "II",
        &[JValue::Int(x.into()), JValue::Int(y.into())],
    );
}

pub fn media_key_code(env: JNIEnv, e: event::MediaKeyCode) -> JniResult<JObject> {
    const CLASS_NAME: &'static str = "tui/crossterm/MediaKeyCode";

    return match e {
        event::MediaKeyCode::Play => enum_value(env, CLASS_NAME, "Play"),
        event::MediaKeyCode::Pause => enum_value(env, CLASS_NAME, "Pause"),
        event::MediaKeyCode::PlayPause => enum_value(env, CLASS_NAME, "PlayPause"),
        event::MediaKeyCode::Reverse => enum_value(env, CLASS_NAME, "Reverse"),
        event::MediaKeyCode::Stop => enum_value(env, CLASS_NAME, "Stop"),
        event::MediaKeyCode::FastForward => enum_value(env, CLASS_NAME, "FastForward"),
        event::MediaKeyCode::Rewind => enum_value(env, CLASS_NAME, "Rewind"),
        event::MediaKeyCode::TrackNext => enum_value(env, CLASS_NAME, "TrackNext"),
        event::MediaKeyCode::TrackPrevious => enum_value(env, CLASS_NAME, "TrackPrevious"),
        event::MediaKeyCode::Record => enum_value(env, CLASS_NAME, "Record"),
        event::MediaKeyCode::LowerVolume => enum_value(env, CLASS_NAME, "LowerVolume"),
        event::MediaKeyCode::RaiseVolume => enum_value(env, CLASS_NAME, "RaiseVolume"),
        event::MediaKeyCode::MuteVolume => enum_value(env, CLASS_NAME, "MuteVolume"),
    };
}

pub fn key_code(env: JNIEnv, e: event::KeyCode) -> JniResult<JObject> {
    fn nullary<'a>(env: JNIEnv<'a>, name: &str) -> JniResult<JObject<'a>> {
        return record(env, &format!("tui/crossterm/KeyCode${}", name), "", &[]);
    }

    match e {
        event::KeyCode::Backspace => nullary(env, "Backspace"),
        event::KeyCode::Enter => nullary(env, "Enter"),
        event::KeyCode::Left => nullary(env, "Left"),
        event::KeyCode::Right => nullary(env, "Right"),
        event::KeyCode::Up => nullary(env, "Up"),
        event::KeyCode::Down => nullary(env, "Down"),
        event::KeyCode::Home => nullary(env, "Home"),
        event::KeyCode::End => nullary(env, "End"),
        event::KeyCode::PageUp => nullary(env, "PageUp"),
        event::KeyCode::PageDown => nullary(env, "PageDown"),
        event::KeyCode::Tab => nullary(env, "Tab"),
        event::KeyCode::BackTab => nullary(env, "BackTab"),
        event::KeyCode::Delete => nullary(env, "Delete"),
        event::KeyCode::Insert => nullary(env, "Insert"),
        event::KeyCode::Null => nullary(env, "Null"),
        event::KeyCode::Esc => nullary(env, "Esc"),
        event::KeyCode::CapsLock => nullary(env, "CapsLock"),
        event::KeyCode::ScrollLock => nullary(env, "ScrollLock"),
        event::KeyCode::NumLock => nullary(env, "NumLock"),
        event::KeyCode::PrintScreen => nullary(env, "PrintScreen"),
        event::KeyCode::Pause => nullary(env, "Pause"),
        event::KeyCode::Menu => nullary(env, "Menu"),
        event::KeyCode::KeypadBegin => nullary(env, "KeypadBegin"),
        event::KeyCode::Modifier(_) => nullary(env, "Modifier"),
        event::KeyCode::Char(c) => {
            let utf16 = c as u16; // todo
            return record(
                env,
                "tui/crossterm/KeyCode$Char",
                "C",
                &[JValue::Char(utf16)],
            );
        }
        event::KeyCode::F(n) => {
            return record(
                env,
                "tui/crossterm/KeyCode$F",
                "I",
                &[JValue::Int(n.into())],
            );
        }
        event::KeyCode::Media(m) => {
            return record(
                env,
                "tui/crossterm/KeyCode$Media",
                "Ltui/crossterm/MediaKeyCode;",
                &[JValue::Object(media_key_code(env, m)?)],
            );
        }
    }
}

pub fn key_modifiers(env: JNIEnv, e: event::KeyModifiers) -> JniResult<JObject> {
    return record(
        env,
        "tui/crossterm/KeyModifiers",
        "I",
        &[JValue::Int(e.bits().into())],
    );
}

pub fn key_event_kind(env: JNIEnv, e: event::KeyEventKind) -> JniResult<JObject> {
    const CLASS_NAME: &'static str = "tui/crossterm/KeyEventKind";
    return match e {
        event::KeyEventKind::Press => enum_value(env, CLASS_NAME, "Press"),
        event::KeyEventKind::Repeat => enum_value(env, CLASS_NAME, "Repeat"),
        event::KeyEventKind::Release => enum_value(env, CLASS_NAME, "Release"),
    };
}

pub fn key_event_state(env: JNIEnv, e: event::KeyEventState) -> JniResult<JObject> {
    return record(
        env,
        "tui/crossterm/KeyEventState",
        "I",
        &[JValue::Int(e.bits().into())],
    );
}

pub fn key_event(env: JNIEnv, e: event::KeyEvent) -> JniResult<JObject> {
    let args = &[
        JValue::Object(key_code(env, e.code)?),
        JValue::Object(key_modifiers(env, e.modifiers)?),
        JValue::Object(key_event_kind(env, e.kind)?),
        JValue::Object(key_event_state(env, e.state)?),
    ];

    return record(
        env,
        "tui/crossterm/KeyEvent",
        "Ltui/crossterm/KeyCode;Ltui/crossterm/KeyModifiers;Ltui/crossterm/KeyEventKind;Ltui/crossterm/KeyEventState;",
        args,
    );
}

pub fn mouse_button(env: JNIEnv, e: event::MouseButton) -> JniResult<JObject> {
    const CLASS_NAME: &'static str = "tui/crossterm/MouseButton";
    return match e {
        event::MouseButton::Left => enum_value(env, CLASS_NAME, "Left"),
        event::MouseButton::Right => enum_value(env, CLASS_NAME, "Right"),
        event::MouseButton::Middle => enum_value(env, CLASS_NAME, "Middle"),
    };
}

pub fn mouse_event_kind(env: JNIEnv, e: event::MouseEventKind) -> JniResult<JObject> {
    return match e {
        event::MouseEventKind::Down(mb) => record(
            env,
            "tui/crossterm/MouseEventKind$Down",
            "Ltui/crossterm/MouseButton;",
            &[JValue::Object(mouse_button(env, mb)?)],
        ),
        event::MouseEventKind::Up(mb) => record(
            env,
            "tui/crossterm/MouseEventKind$Up",
            "Ltui/crossterm/MouseButton;",
            &[JValue::Object(mouse_button(env, mb)?)],
        ),
        event::MouseEventKind::Drag(mb) => record(
            env,
            "tui/crossterm/MouseEventKind$Drag",
            "Ltui/crossterm/MouseButton;",
            &[JValue::Object(mouse_button(env, mb)?)],
        ),
        event::MouseEventKind::Moved => record(env, "tui/crossterm/MouseEventKind$Moved", "", &[]),
        event::MouseEventKind::ScrollDown => {
            record(env, "tui/crossterm/MouseEventKind$ScrollDown", "", &[])
        }
        event::MouseEventKind::ScrollUp => {
            record(env, "tui/crossterm/MouseEventKind$ScrollUp", "", &[])
        }
    };
}

pub fn mouse_event(env: JNIEnv, e: event::MouseEvent) -> JniResult<JObject> {
    let args = &[
        JValue::Object(mouse_event_kind(env, e.kind)?),
        JValue::Int(e.column.into()),
        JValue::Int(e.row.into()),
        JValue::Object(key_modifiers(env, e.modifiers)?),
    ];
    return record(
        env,
        "tui/crossterm/MouseEvent",
        "Ltui/crossterm/MouseEventKind;IILtui/crossterm/KeyModifiers;",
        args,
    );
}

pub fn event(env: JNIEnv, e: event::Event) -> JniResult<JObject> {
    return match e {
        event::Event::FocusGained => record(env, "tui/crossterm/Event$FocusGained", "", &[]),
        event::Event::FocusLost => record(env, "tui/crossterm/Event$FocusLost", "", &[]),
        event::Event::Key(ke) => record(
            env,
            "tui/crossterm/Event$Key",
            "Ltui/crossterm/KeyEvent;",
            &[JValue::Object(key_event(env, ke)?)],
        ),
        event::Event::Mouse(me) => record(
            env,
            "tui/crossterm/Event$Mouse",
            "Ltui/crossterm/MouseEvent;",
            &[JValue::Object(mouse_event(env, me)?)],
        ),
        event::Event::Paste(str) => record(
            env,
            "tui/crossterm/Event$Paste",
            "Ljava/lang/String;",
            &[JValue::Object(env.new_string(str)?.into())],
        ),

        event::Event::Resize(x, y) => record(
            env,
            "tui/crossterm/Event$Resize",
            "II",
            &[JValue::Int(x.into()), JValue::Int(y.into())],
        ),
    };
}
